package com.libyear.sourcing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

internal class MavenCentralTest {

  @Test
  fun apacheCommonsText_solr() {
    val adapter = SolrSearchAdapter.forMavenCentral()

    val created = adapter.retrieveUpdate(Fixtures.apacheCommonsTextArtifact, "1.9")

    assertThat(created).extracting { it?.lag }.isEqualTo(Duration.ofMillis(28169022000))
  }

  @Test
  fun apacheCommonsText_xml() {
    val adapter = HttpUrlAdapter()

    val created = adapter.retrieveUpdate(StubRepository("urlRepo", "https://repo1.maven.org/maven2/"), Fixtures.apacheCommonsTextArtifact, "1.9")

    assertThat(created).extracting { it?.lag }.isEqualTo(Duration.ofMillis(28169023000))
  }

  @Test
  fun notExistingArtifact() {
    val adapter = SolrSearchAdapter.forMavenCentral()

    val created = adapter.get(Fixtures.notExistingArtifact, Fixtures.stubRepository)

    assertThat(created).isEmpty()
  }
}
