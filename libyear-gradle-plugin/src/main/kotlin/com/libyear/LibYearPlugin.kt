package com.libyear

import com.libyear.adapters.SolrSearchAdapter
import com.libyear.adapters.VersionInfoAdapter
import com.libyear.traversal.AgeOracle
import com.libyear.traversal.DependencyTraversal
import com.libyear.traversal.ValidatingVisitor
import com.libyear.validator.DependencyValidator
import com.libyear.validator.LoggingValidator
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.logging.Logger
import java.time.Duration
import java.time.Instant

class LibYearPlugin : Plugin<Project> {
  companion object {
    const val EXTENSION_NAME: String = "libyear"
  }

  override fun apply(project: Project) {
    project.extensions.create(EXTENSION_NAME, LibYearExtension::class.java)

    if (project.gradle.startParameter.isOffline) {
      project.logger.lifecycle("libyear dependency check disabled because Gradle is in offline mode")
      return
    }

    project.configurations.all { configureConfiguration(project, it) }
  }

  private fun configureConfiguration(project: Project, configuration: Configuration) {
    configuration.incoming.afterResolve { checkDependencies(project, it) }
  }

  private fun checkDependencies(
    project: Project,
    resolvableDependencies: ResolvableDependencies,
  ) {

    val extension = project.extensions.getByName(EXTENSION_NAME) as LibYearExtension
    val ageOracle = createOracle(extension)
    val validator = createValidator(project, extension)
    val visitor = ValidatingVisitor(project.logger, ageOracle, validator)
    DependencyTraversal.visit(resolvableDependencies.resolutionResult.root, visitor)
    maybeReportFailure(project.logger, validator)
  }

  private fun createValidator(project: Project, extension: LibYearExtension): DependencyValidator {
    return LoggingValidator(project.logger, extension.validator.create())
  }

  private fun createOracle(extension: LibYearExtension): AgeOracle {
    val adapter = createAdapter(extension)
    val now: Instant = extension.clock.instant()
    return { m ->
      // FIXME nullity
      val created = adapter.getArtifactCreated(m)
      Duration.between(created, now)
    }
  }

  private fun createAdapter(extension: LibYearExtension): VersionInfoAdapter {
    return SolrSearchAdapter.forMavenCentral()
  }

  private fun maybeReportFailure(
    logger: Logger,
    validator: DependencyValidator,
  ) {

    if (validator.isValid()) return

    val collected = validator.violators()
    if (collected.isEmpty()) return

    logger.lifecycle("Too many libyears encountered! These are the main culprits:")
    for (dep in collected) {
      logger.lifecycle(" -> ${dep.age.formatApproximate().padEnd(10)} from ${dep.version}")
    }

    val sum = collected.map { it.age }.reduce(Duration::plus)
    val exceededBy = sum - validator.threshold()
    throw GradleException(
      "Libyears threshold of ${
      validator.threshold().formatApproximate()
      } exceeded by ${exceededBy.formatApproximate()}"
    )
  }
}
