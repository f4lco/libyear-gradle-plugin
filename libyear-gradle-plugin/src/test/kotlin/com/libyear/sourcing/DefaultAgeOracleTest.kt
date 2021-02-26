package com.libyear.sourcing

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.junit.jupiter.api.Test
import java.time.Instant

class DefaultAgeOracleTest {

  @Test
  fun testEmpty() {
    val oracle = DefaultAgeOracle(
      now = Instant.now(),
      defaultAdapter = DummyAdapter,
      adapters = mapOf(),
      repositories = listOf(Fixtures.stubRepository).associateBy { it.name }
    )

    val age = oracle.get(Fixtures.apacheCommonsTextArtifact, Fixtures.stubRepository.name)

    assertThat(age).isNull()
  }

  private object DummyAdapter : VersionInfoAdapter {
    override fun getArtifactCreated(m: ModuleVersionIdentifier, repository: ArtifactRepository) = null
  }
}
