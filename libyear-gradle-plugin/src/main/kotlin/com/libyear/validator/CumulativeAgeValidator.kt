package com.libyear.validator

import java.time.Duration
import java.util.PriorityQueue

data class CumulativeAgeValidatorSpec(
  val maxAge: Duration
) : DependencyValidatorSpec {

  override fun create() = CumulativeAgeValidator(this)
}

class CumulativeAgeValidator(val spec: CumulativeAgeValidatorSpec) : DependencyValidator {

  private val collected = PriorityQueue(byAgeDescending())

  fun cumulativeAge(): Duration = collected.map { it.age }.fold(Duration.ZERO, Duration::plus)

  override fun add(dep: DependencyInfo) {
    collected.add(dep)
  }

  override fun isValid() = cumulativeAge() <= spec.maxAge

  override fun threshold() = spec.maxAge

  override fun violators(): List<DependencyInfo> {
    return collected.toList()
  }

  override fun toString(): String {
    return "CumulativeAgeValidator(spec=$spec, duration=${cumulativeAge()})"
  }

  companion object {
    private fun byAgeDescending(): Comparator<DependencyInfo> {
      return Comparator.comparing<DependencyInfo, Duration> { it.age }.reversed()
    }
  }
}
