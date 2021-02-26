package com.libyear.sourcing

import okhttp3.Headers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.time.Instant
import java.time.temporal.ChronoUnit

internal class HttpUrlAdapterTest {

  private val now = Instant.now()

  private lateinit var server: MockWebServer
  private lateinit var adapter: HttpUrlAdapter
  private lateinit var repo: MavenArtifactRepository

  @BeforeEach
  fun setUp() {
    server = MockWebServer()
    adapter = HttpUrlAdapter()
    repo = mock(MavenArtifactRepository::class.java)
    given(repo.getUrl()).willReturn(server.url("artifacts").toUri())
  }

  @AfterEach
  fun tearDown() {
    server.close()
  }

  @Test
  fun testOnline() {
    val headers = Headers.headersOf().newBuilder().add("Last-Modified", now).build()
    server.enqueue(MockResponse().setHeaders(headers))

    val created = adapter.getArtifactCreated(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).first().isEqualTo(now.truncatedTo(ChronoUnit.SECONDS))
  }

  @Test
  fun testTimeout() {
    // nothing enqueued

    val created = adapter.getArtifactCreated(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }

  @Test
  fun serverError() {
    server.enqueue(MockResponse().setResponseCode(500))

    val created = adapter.getArtifactCreated(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }

  @Test
  fun notFound() {
    server.enqueue(MockResponse().setResponseCode(404))

    val created = adapter.getArtifactCreated(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }

  @Test
  fun headerNotPresent() {
    server.enqueue(MockResponse())

    val created = adapter.getArtifactCreated(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }
}
