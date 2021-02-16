package com.libyear.traversal

import com.libyear.validator.DependencyInfo
import com.libyear.validator.DependencyValidator
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.logging.Logger
import java.time.Duration

typealias AgeOracle = (ModuleVersionIdentifier) -> Duration

class ValidatingVisitor(
  logger: Logger,
  private val ageOracle: AgeOracle,
  private val validator: DependencyValidator,
) : DependencyVisitor(logger) {

  override fun canContinue() = validator.isValid()

  override fun visitResolvedDependencyResult(result: ResolvedDependencyResult) {
    result.selected.moduleVersion?.let { version ->
      val age = ageOracle(version)
      validator.add(DependencyInfo(version, age))
    }
  }
}
