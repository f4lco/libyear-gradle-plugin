package com.libyear.sourcing

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DefaultRepositoryLayoutTest {

  @Test
  internal fun testApacheCommonsUrl() {
    val baseUrl = "https://repo.example.org".toHttpUrl()

    val url = DefaultRepositoryLayout.getArtifactUrl(baseUrl, Fixtures.apacheCommonsTextArtifact)

    assertThat(url).isEqualTo("https://repo.example.org/org/apache/commons/commons-text/1.9/commons-text-1.9.jar".toHttpUrl())
  }
}
