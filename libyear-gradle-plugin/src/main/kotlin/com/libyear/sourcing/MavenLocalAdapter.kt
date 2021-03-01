package com.libyear.sourcing

import io.vavr.control.Try
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Because the Maven local repository is typically local to the development machine and not generally reproducible,
 * the plugin does not source artifact creation dates from this type of repository.
 */
class MavenLocalAdapter : VersionInfoAdapter {

  private val once = AtomicBoolean()

  override fun get(m: ModuleVersionIdentifier, repository: ArtifactRepository): Try<DependencyInfo> {
    if (once.compareAndSet(false, true)) {
      LOG.warn("Extracting artifact creation dates from the Maven local repository is unreliable and therefore not supported.")
    }
    return Try.failure(UnsupportedOperationException("Maven local repository not supported"))
  }

  companion object {
    private val LOG = LoggerFactory.getLogger(MavenLocalAdapter::class.java)
  }
}
