package com.libyear.sourcing

import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant

class DefaultAgeOracle(
  private val now: Instant,
  private val defaultAdapter: VersionInfoAdapter,
  private val adapters: Map<String, VersionInfoAdapter>,
  private val repositories: Map<String, ArtifactRepository>
) : AgeOracle {

  override fun get(module: ModuleVersionIdentifier, repositoryName: String): Duration? {
    val adapter = selectAdapter(repositoryName)
    val repository = repositories[repositoryName] ?: throw IllegalArgumentException("Cannot find repository $repositoryName")
    val created = adapter.getArtifactCreated(module, repository)
    return Duration.between(created, now)
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
