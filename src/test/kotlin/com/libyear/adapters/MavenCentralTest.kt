package com.libyear.adapters

import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier.newId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

class MavenCentralTest {

  @Test
  fun apacheCommonsText() {
    val adapter = SolrSearchAdapter.forMavenCentral()

    val created = adapter.getArtifactCreated(apacheCommonsTextArtifact)

    assertEquals(Instant.ofEpochMilli(1595364048000), created)
  }

  @Test
  fun notExistingArtifact() {
    val adapter = SolrSearchAdapter.forMavenCentral()

    val created = adapter.getArtifactCreated(notExistingArtifact)

    assertNull(created)
  }

  companion object {

    private val apacheCommonsTextArtifact = newId(
      "org.apache.commons",
      "commons-text",
      "1.9",
    )

    private val notExistingArtifact = newId(
      "org.apache.commons",
      "commons-text-not-existing",
      "1.9",
    )
  }
}
