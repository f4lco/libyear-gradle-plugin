package com.libyear.traversal

import com.libyear.sourcing.AgeOracle
import com.libyear.validator.DependencyInfo
import com.libyear.validator.DependencyValidator
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.internal.artifacts.result.ResolvedComponentResultInternal
import org.gradle.api.logging.Logger

data class ValidationConfig(
  val failOnError: Boolean = true
)

class ValidatingVisitor(
  logger: Logger,
  private val ageOracle: AgeOracle,
  private val validator: DependencyValidator,
  private val config: ValidationConfig
) : DependencyVisitor(logger) {

  override fun canContinue() = validator.isValid()

  override fun visitResolvedDependencyResult(result: ResolvedDependencyResult) {
    val module = result.selected.moduleVersion ?: return
    val repositoryName = extractRepositoryName(result) ?: return
    val age = ageOracle.get(module, repositoryName)

    age.onSuccess {
      validator.add(DependencyInfo(module, it))
    }.onFailure {
      handleFailure(repositoryName, module, it)
    }
  }

  private fun extractRepositoryName(result: ResolvedDependencyResult): String? {
    return when (val dependency = result.selected) {
      is ResolvedComponentResultInternal -> dependency.repositoryName
      else -> null
    }
  }

  private fun handleFailure(repositoryName: String, module: ModuleVersionIdentifier, failure: Throwable) {
    if (config.failOnError) {
      throw GradleException(
        """
        Cannot determine dependency age for "$module" and repository "$repositoryName".
        If errors should be skipped, set failOnError=false in the plugin configuration.
        """.trimIndent(),
        failure
      )
    }
  }
}
