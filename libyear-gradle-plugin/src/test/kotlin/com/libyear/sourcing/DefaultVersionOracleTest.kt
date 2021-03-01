package com.libyear.sourcing

import io.vavr.control.Try
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.junit.jupiter.api.Test

class DefaultVersionOracleTest {

  @Test
  fun testEmpty() {
    val oracle = DefaultVersionOracle(
      defaultAdapter = DummyAdapter,
      adapters = mapOf(),
      repositories = listOf(Fixtures.stubRepository).associateBy { it.name }
    )

    val age = oracle.get(Fixtures.apacheCommonsTextArtifact, Fixtures.stubRepository.name)

    assertThat(age).isEmpty()
  }

  private object DummyAdapter : VersionInfoAdapter {
    override fun get(m: ModuleVersionIdentifier, repository: ArtifactRepository): Try<DependencyInfo> {
      return Try.failure(UnsupportedOperationException())
    }
  }
}
