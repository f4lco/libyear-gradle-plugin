package com.libyear.sourcing

import io.vavr.control.Try
import org.gradle.api.artifacts.ModuleVersionIdentifier

interface VersionOracle {
  fun get(
    module: ModuleVersionIdentifier,
    repositoryName: String
  ): Try<DependencyInfo>
}
