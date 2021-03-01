package com.libyear.sourcing

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.Duration

internal class SolrSearchAdapterTest {

  private lateinit var server: MockWebServer
  private lateinit var adapter: SolrSearchAdapter
  private lateinit var repo: MavenArtifactRepository

  @BeforeEach
  fun setUp() {
    server = MockWebServer()
    adapter = SolrSearchAdapter(server.url("artifacts").toString())
    repo = mock(MavenArtifactRepository::class.java)
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
      {
          "response": {
              "docs": [
                  {
                      "a": "commons-text",
                      "ec": [
                          "-sources.jar",
                          "-javadoc.jar",
                          "-test-sources.jar",
                          "-tests.jar",
                          ".jar",
                          ".pom"
                      ],
                      "g": "org.apache.commons",
                      "id": "org.apache.commons:commons-text",
                      "latestVersion": "1.9",
                      "p": "jar",
                      "repositoryId": "central",
                      "text": [
                          "org.apache.commons",
                          "commons-text",
                          "-sources.jar",
                          "-javadoc.jar",
                          "-test-sources.jar",
                          "-tests.jar",
                          ".jar",
                          ".pom"
                      ],
                      "timestamp": 1595364048000,
                      "versionCount": 11
                  }
              ],
              "numFound": 1,
              "start": 0
          },
          "responseHeader": {
              "QTime": 0,
              "params": {
                  "core": "",
                  "fl": "id,g,a,latestVersion,p,ec,repositoryId,text,timestamp,versionCount",
                  "indent": "off",
                  "q": "g:\"org.apache.commons\" AND a:\"commons-text\"",
                  "rows": "",
                  "sort": "score desc,timestamp desc,g asc,a asc",
                  "spellcheck": "true",
                  "spellcheck.count": "5",
                  "start": "",
                  "version": "2.2",
                  "wt": "json"
              },
              "status": 0
          },
          "spellcheck": {
              "suggestions": []
          }
      }
        """.trimIndent()
      )
    )

    server.enqueue(
      MockResponse().setBody(
        """
    {
    "response": {
        "docs": [
            {
                "a": "commons-text",
                "ec": [
                    "-javadoc.jar",
                    "-sources.jar",
                    "-test-sources.jar",
                    ".jar",
                    "-tests.jar",
                    ".pom"
                ],
                "g": "org.apache.commons",
                "id": "org.apache.commons:commons-text:1.8",
                "p": "jar",
                "tags": [
                    "text",
                    "commons",
                    "library",
                    "focused",
                    "apache",
                    "strings",
                    "algorithms",
                    "working"
                ],
                "timestamp": 1567195026000,
                "v": "1.8"
            }
        ],
        "numFound": 1,
        "start": 0
    },
    "responseHeader": {
        "QTime": 0,
        "params": {
            "core": "",
            "fl": "id,g,a,v,p,ec,timestamp,tags",
            "indent": "off",
            "q": "g:\"org.apache.commons\" AND a:\"commons-text\" AND v:\"1.8\"",
            "rows": "",
            "sort": "score desc,timestamp desc,g asc,a asc,v desc",
            "start": "",
            "version": "2.2",
            "wt": "json"
        },
        "status": 0
    }
}
        """.trimIndent()
      )
    )

    server.enqueue(
      MockResponse().setBody(
        """
      {
          "response": {
              "docs": [
                  {
                      "a": "commons-text",
                      "ec": [
                          "-javadoc.jar",
                          "-sources.jar",
                          "-test-sources.jar",
                          ".jar",
                          "-tests.jar",
                          ".pom"
                      ],
                      "g": "org.apache.commons",
                      "id": "org.apache.commons:commons-text:1.9",
                      "p": "jar",
                      "tags": [
                          "text",
                          "commons",
                          "library",
                          "focused",
                          "apache",
                          "strings",
                          "algorithms",
                          "working"
                      ],
                      "timestamp": 1595364048000,
                      "v": "1.9"
                  }
              ],
              "numFound": 1,
              "start": 0
          },
          "responseHeader": {
              "QTime": 0,
              "params": {
                  "core": "",
                  "fl": "id,g,a,v,p,ec,timestamp,tags",
                  "indent": "off",
                  "q": "g:\"org.apache.commons\" AND a:\"commons-text\" AND v:\"1.9\"",
                  "rows": "",
                  "sort": "score desc,timestamp desc,g asc,a asc,v desc",
                  "start": "",
                  "version": "2.2",
                  "wt": "json"
              },
              "status": 0
          }
      }
        """.trimIndent()
      )
    )

    val created = adapter.get(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).first().isEqualTo(
      DependencyInfo(
        Fixtures.apacheCommonsTextArtifact,
        DependencyUpdate(nextVersion = "1.9", lag = Duration.ofMillis(28169022000))
      )
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
  fun noDocumentsResponse() {
    server.enqueue(
      MockResponse().setBody(
        """
    {
        "response": {
            "docs": [],
            "numFound": 0,
            "start": 0
        },
        "responseHeader": {
            "QTime": 0,
            "params": {
                "core": "",
                "fl": "id,g,a,v,p,ec,timestamp,tags",
                "indent": "off",
                "q": "g:\"foo\" AND a:\"bar\" AND v:\"1.9\"",
                "rows": "",
                "sort": "score desc,timestamp desc,g asc,a asc,v desc",
                "start": "",
                "version": "2.2",
                "wt": "json"
            },
            "status": 0
        }
    }
        """.trimIndent()
      )
    )

    val created = adapter.get(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }

  @Test
  fun emptySearchResponse() {
    server.enqueue(MockResponse().setBody(""))

    val created = adapter.get(Fixtures.apacheCommonsTextArtifact, repo)

    assertThat(created).isEmpty()
  }
}
