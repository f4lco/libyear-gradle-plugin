package com.libyear.validator

import java.time.Duration

data class CumulativeAgeValidatorSpec(
  val maxAge: Duration
) : DependencyValidatorSpec {

  override fun create() = CumulativeAgeValidator(this)
}

class CumulativeAgeValidator(val spec: CumulativeAgeValidatorSpec) : DependencyValidator {

  private val collected = mutableListOf<DependencyInfo>()

  fun cumulativeAge(): Duration = collected.map { it.age }.fold(Duration.ZERO, Duration::plus)

  override fun add(dep: DependencyInfo) {
    if (dep.age > Duration.ZERO) {
      collected.add(dep)
    }
  }

  override fun isValid() = cumulativeAge() <= spec.maxAge

  override fun threshold() = spec.maxAge

  override fun violators() = collected.sortedByDescending { it.age }

  override fun toString(): String {
    return "CumulativeAgeValidator(spec=$spec, duration=${cumulativeAge()})"
  }
}
