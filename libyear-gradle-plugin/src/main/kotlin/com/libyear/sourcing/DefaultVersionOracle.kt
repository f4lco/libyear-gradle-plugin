package com.libyear.sourcing

import io.vavr.control.Try
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.slf4j.LoggerFactory

/**
 * Turn artifact creation dates into artifact ages.
 *
 * Adapter selection: the algorithm prefers to select an adapter by name of the repository Gradle has
 * sourced the artifact from, see [adapters]. If none is present, [defaultAdapter] is queried.
 */
class DefaultVersionOracle(
  private val defaultAdapter: VersionInfoAdapter,
  private val adapters: Map<String, VersionInfoAdapter>,
  private val repositories: Map<String, ArtifactRepository>
) : VersionOracle {

  override fun get(module: ModuleVersionIdentifier, repositoryName: String): Try<DependencyInfo> {
    val adapter = selectAdapter(repositoryName)
    val repository = repositories[repositoryName] ?: throw IllegalArgumentException("Cannot find repository $repositoryName")
    return adapter.get(module, repository)
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
    private val LOG = LoggerFactory.getLogger(DefaultVersionOracle::class.java)
  }
}
