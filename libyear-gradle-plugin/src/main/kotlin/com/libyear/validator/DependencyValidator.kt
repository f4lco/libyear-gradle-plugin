package com.libyear.validator

import org.gradle.api.artifacts.ModuleVersionIdentifier
import java.time.Duration

data class DependencyInfo(
  val version: ModuleVersionIdentifier,
  val age: Duration
)

interface DependencyValidator {

  fun add(dep: DependencyInfo)

  fun isValid(): Boolean

  fun threshold(): Duration

  fun violators(): List<DependencyInfo>
}
