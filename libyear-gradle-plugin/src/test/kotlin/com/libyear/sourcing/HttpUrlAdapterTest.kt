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
import java.time.Duration
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
    server.enqueue(
      MockResponse().setBody(
        """
      <metadata>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-text</artifactId>
      <versioning>
      <latest>1.9</latest>
      <release>1.9</release>
      <versions>
      <version>1.0-beta-1</version>
      <version>1.0</version>
      <version>1.1</version>
      <version>1.2</version>
      <version>1.3</version>
      <version>1.4</version>
      <version>1.5</version>
      <version>1.6</version>
      <version>1.7</version>
      <version>1.8</version>
      <version>1.9</version>
      </versions>
      <lastUpdated>20200724213155</lastUpdated>
      </versioning>
      </metadata>
        """.trimIndent()
      )
    )

    val headersCurrent = Headers.headersOf().newBuilder().add("Last-Modified", now.minus(5, ChronoUnit.HOURS)).build()
    server.enqueue(MockResponse().setHeaders(headersCurrent))

    val headersLatest = Headers.headersOf().newBuilder().add("Last-Modified", now).build()
    server.enqueue(MockResponse().setHeaders(headersLatest))

    val created = adapter.get(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).first().isEqualTo(
      DependencyInfo(Fixtures.apacheCommonsTextArtifact, DependencyUpdate(nextVersion = "1.9", lag = Duration.ofHours(5)))
    )
  }

  @Test
  fun testTimeout() {
    // nothing enqueued

    val created = adapter.get(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }

  @Test
  fun serverError() {
    server.enqueue(MockResponse().setResponseCode(500))

    val created = adapter.get(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }

  @Test
  fun notFound() {
    server.enqueue(MockResponse().setResponseCode(404))

    val created = adapter.get(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }

  @Test
  fun headerNotPresent() {
    server.enqueue(MockResponse())

    val created = adapter.get(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }
}
