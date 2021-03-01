package com.libyear.traversal

import com.libyear.sourcing.AgeOracle
import com.libyear.validator.DependencyInfo
import com.libyear.validator.DependencyValidator
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.internal.artifacts.result.ResolvedComponentResultInternal
import org.gradle.api.logging.Logger
import org.slf4j.LoggerFactory

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

  override fun visitResolvedComponentResult(component: ResolvedComponentResult) {
    val module = component.moduleVersion ?: return
    val repositoryName = extractRepositoryName(component) ?: return
    val age = ageOracle.get(module, repositoryName)

    age.onSuccess {
      validator.add(DependencyInfo(module, it))
    }.onFailure {
      handleFailure(repositoryName, module, it)
    }
  }

  private fun extractRepositoryName(result: ResolvedComponentResult): String? {
    if (result is ResolvedComponentResultInternal) {
      return result.repositoryName
    }
    return null
  }

  private fun handleFailure(repositoryName: String, module: ModuleVersionIdentifier, failure: Throwable) {
    val baseMessage =
      """Cannot determine dependency age for "$module" and repository "$repositoryName" (reason: ${failure::class.simpleName})."""
    if (config.failOnError) {
      throw GradleException(
        """
        $baseMessage
        If errors should be skipped, set failOnError=false in the plugin configuration.
        """.trimIndent(),
        failure
      )
    }

    // deliberately omitting 'failure' as last parameter: the log message with a full stack trace is far too noisy.
    // If the stack trace is important, set config.failOnError = true and run Gradle with --stacktrace.
    LOG.warn(baseMessage)
  }

  companion object {
    private val LOG = LoggerFactory.getLogger(ValidatingVisitor::class.java)
  }
}
