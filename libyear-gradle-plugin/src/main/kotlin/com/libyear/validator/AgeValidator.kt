package com.libyear.validator

import java.time.Duration

data class AgeValidatorSpec(val maxAge: Duration) : DependencyValidatorSpec {
  override fun create() = AgeValidator(this)
}

class AgeValidator(val spec: AgeValidatorSpec) : DependencyValidator {

  private var violator: DependencyInfo? = null

  override fun add(dep: DependencyInfo) {
    if (dep.age > spec.maxAge) violator = dep
  }

  override fun isValid() = violator == null

  override fun threshold() = spec.maxAge

  override fun violators() = listOfNotNull(violator)

  override fun toString(): String {
    return "AgeValidator(spec=$spec)"
  }
}
