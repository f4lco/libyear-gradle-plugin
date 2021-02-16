package com.libyear.traversal

import com.libyear.sourcing.AgeOracle
import com.libyear.validator.DependencyInfo
import com.libyear.validator.DependencyValidator
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.internal.artifacts.result.ResolvedComponentResultInternal
import org.gradle.api.logging.Logger

class ValidatingVisitor(
  logger: Logger,
  private val ageOracle: AgeOracle,
  private val validator: DependencyValidator,
) : DependencyVisitor(logger) {

  override fun canContinue() = validator.isValid()

  override fun visitResolvedDependencyResult(result: ResolvedDependencyResult) {
    val module = result.selected.moduleVersion ?: return
    val repositoryName = extractRepositoryName(result) ?: return
    val age = ageOracle.get(module, repositoryName) ?: return
    validator.add(DependencyInfo(module, age))
  }

  private fun extractRepositoryName(result: ResolvedDependencyResult): String? {
    return when (val dependency = result.selected) {
      is ResolvedComponentResultInternal -> dependency.repositoryName
      else -> null
    }
  }
}
