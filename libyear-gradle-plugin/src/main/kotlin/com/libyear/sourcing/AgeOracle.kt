package com.libyear.sourcing

import org.gradle.api.artifacts.ModuleVersionIdentifier
import java.time.Duration

interface AgeOracle {
  fun get(
    module: ModuleVersionIdentifier,
    repositoryName: String,
  ): Duration?
}
