package com.libyear.sourcing

import io.vavr.control.Try
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

/**
 * Turn artifact creation dates into artifact ages.
 *
 * Adapter selection: the algorithm prefers to select an adapter by name of the repository Gradle has
 * sourced the artifact from, see [adapters]. If none is present, [defaultAdapter] is queried.
 */
class DefaultAgeOracle(
  private val now: Instant,
  private val defaultAdapter: VersionInfoAdapter,
  private val adapters: Map<String, VersionInfoAdapter>,
  private val repositories: Map<String, ArtifactRepository>
) : AgeOracle {

  override fun get(module: ModuleVersionIdentifier, repositoryName: String): Try<Duration> {
    val adapter = selectAdapter(repositoryName)
    val repository = repositories[repositoryName] ?: throw IllegalArgumentException("Cannot find repository $repositoryName")
    val created = adapter.getArtifactCreated(module, repository)
    return created.map { Duration.between(it, now) }
  }

  private fun selectAdapter(repositoryName: String): VersionInfoAdapter {
    val adapter = adapters[repositoryName]

    if (adapter != null) {
      LOG.debug("Using adapter {} for repository {}", adapter::class.simpleName, repositoryName)
      return adapter
    }

    LOG.debug("Falling back to default adapter {} for repository {}", defaultAdapter::class.simpleName, repositoryName)
    return defaultAdapter
  }

  private companion object {
    private val LOG = LoggerFactory.getLogger(DefaultAgeOracle::class.java)
  }
}