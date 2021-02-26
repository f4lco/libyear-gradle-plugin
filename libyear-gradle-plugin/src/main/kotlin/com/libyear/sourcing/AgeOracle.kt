package com.libyear.sourcing

import io.vavr.control.Try
import org.gradle.api.artifacts.ModuleVersionIdentifier
import java.time.Duration

interface AgeOracle {
  fun get(
    module: ModuleVersionIdentifier,
    repositoryName: String
  ): Try<Duration>
}
