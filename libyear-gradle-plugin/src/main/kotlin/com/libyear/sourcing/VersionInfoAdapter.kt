package com.libyear.sourcing

import io.vavr.control.Try
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import java.time.Duration

data class DependencyInfo(
  val module: ModuleVersionIdentifier,
  val update: DependencyUpdate? = null
)

data class DependencyUpdate(
  val nextVersion: String,
  val lag: Duration
)

interface VersionInfoAdapter {

  fun get(m: ModuleVersionIdentifier, repository: ArtifactRepository): Try<DependencyInfo>
}
