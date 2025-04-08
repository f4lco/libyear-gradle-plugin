package com.libyear.sourcing

import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class RetryableHttpClientTest {

  private lateinit var server: MockWebServer
  private lateinit var client: RetryableHttpClient

  @BeforeEach
  fun setUp() {
    server = MockWebServer()
    server.start()

    // Use a client with shorter delays for testing
    client = RetryableHttpClient(
      maxRetries = 3,
      initialRetryDelayMillis = 50,
      retryBackoffMultiplier = 2
    )
  }

  @AfterEach
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun `successful request returns response`() {
    // Given
    server.enqueue(MockResponse().setResponseCode(200).setBody("Success"))
    val request = Request.Builder().url(server.url("/test")).build()

    // When
    val response = client.executeWithRetry(request)

    // Then
    assertThat(response.isSuccessful).isTrue()
    assertThat(response.body?.string()).isEqualTo("Success")
    assertThat(server.requestCount).isEqualTo(1)
  }

  @Test
  fun `retries on server error and succeeds eventually`() {
    // Given
    server.enqueue(MockResponse().setResponseCode(500))
    server.enqueue(MockResponse().setResponseCode(500))
    server.enqueue(MockResponse().setResponseCode(200).setBody("Success after retry"))
    val request = Request.Builder().url(server.url("/test")).build()

    // When
    val response = client.executeWithRetry(request)

    // Then
    assertThat(response.isSuccessful).isTrue()
    assertThat(response.body?.string()).isEqualTo("Success after retry")
    assertThat(server.requestCount).isEqualTo(3)
  }

  @Test
  fun `throws exception after max retries`() {
    // Given
    server.enqueue(MockResponse().setResponseCode(500))
    server.enqueue(MockResponse().setResponseCode(500))
    server.enqueue(MockResponse().setResponseCode(500))
    server.enqueue(MockResponse().setResponseCode(500))
    val request = Request.Builder().url(server.url("/test")).build()

    // When/Then
    val exception = assertThrows<IOException> {
      client.executeWithRetry(request)
    }

    assertThat(exception.message).contains("Request failed after 3 retries")
    assertThat(server.requestCount).isEqualTo(4) // Initial + 3 retries
  }

  @Test
  fun `uses exponential backoff between retries`() {
    // Given
    server.enqueue(MockResponse().setResponseCode(500))
    server.enqueue(MockResponse().setResponseCode(500))
    server.enqueue(MockResponse().setResponseCode(500))
    server.enqueue(MockResponse().setResponseCode(200).setBody("Success"))

    val client = RetryableHttpClient(
      maxRetries = 3,
      initialRetryDelayMillis = 100,
      retryBackoffMultiplier = 2
    )

    val request = Request.Builder().url(server.url("/test")).build()

    // When
    val startTime = System.currentTimeMillis()
    client.executeWithRetry(request)
    val duration = System.currentTimeMillis() - startTime

    // Then
    // We expect at least: 100ms + 200ms + 400ms = 700ms of delay
    // But we don't want to be too strict with timing assertions
    assertThat(duration).isGreaterThan(600)
  }
}
