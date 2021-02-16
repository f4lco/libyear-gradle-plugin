package com.libyear.sourcing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

internal class MavenCentralTest {

  @Test
  fun apacheCommonsText() {
    val adapter = SolrSearchAdapter.forMavenCentral()

    val created = adapter.getArtifactCreated(Fixtures.apacheCommonsTextArtifact, Fixtures.stubRepository)

    assertThat(created).isEqualTo(Instant.ofEpochMilli(1595364048000))
  }

  @Test
  fun notExistingArtifact() {
    val adapter = SolrSearchAdapter.forMavenCentral()

    val created = adapter.getArtifactCreated(Fixtures.notExistingArtifact, Fixtures.stubRepository)

    assertThat(created).isNull()
  }
}
