package com.libyear

import com.libyear.sourcing.DefaultVersionOracle
import com.libyear.sourcing.HttpUrlAdapter
import com.libyear.sourcing.MavenLocalAdapter
import com.libyear.sourcing.SolrSearchAdapter
import com.libyear.sourcing.VersionInfoAdapter
import com.libyear.sourcing.VersionOracle
import com.libyear.traversal.DependencyTraversal
import com.libyear.traversal.ValidatingVisitor
import com.libyear.traversal.ValidationConfig
import com.libyear.util.formatApproximate
import com.libyear.validator.DependencyValidator
import com.libyear.validator.LoggingValidator
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.api.logging.Logger
import org.gradle.invocation.DefaultGradle
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.time.Duration

class LibYearPlugin : Plugin<Project> {
  companion object {
    const val EXTENSION_NAME: String = "libyear"
  }

  override fun apply(project: Project) {
    project.extensions.create<LibYearExtension>(EXTENSION_NAME)

    if (project.gradle.startParameter.isOffline) {
      project.logger.lifecycle("libyear dependency check disabled because Gradle is in offline mode")
      return
    }

    project.configurations.all { configureConfiguration(project, this) }
    project.tasks.register<LibYearReportTask>("reportLibyears")
  }

  private fun configureConfiguration(project: Project, configuration: Configuration) {
    configuration.incoming.afterResolve { checkDependencies(project, configuration, this) }
  }

  private fun checkDependencies(
    project: Project,
    configuration: Configuration,
    resolvableDependencies: ResolvableDependencies
  ) {

    val extension = project.extensions.getByName(EXTENSION_NAME) as LibYearExtension
    if (skip(configuration, extension)) return

    val ageOracle = createOracle(project, extension)
    val validator = createValidator(project, extension)
    val visitor = ValidatingVisitor(project.logger, ageOracle, validator, ValidationConfig(failOnError = extension.failOnError))
    DependencyTraversal.visit(resolvableDependencies.resolutionResult.root, visitor, extension.ignoreTransitive)
    maybeReportFailure(project.logger, validator)
  }

  private fun skip(configuration: Configuration, extension: LibYearExtension): Boolean {
    return configuration.name !in extension.configurations
  }

  private fun createValidator(project: Project, extension: LibYearExtension): DependencyValidator {
    return LoggingValidator(project.logger, extension.validator.create())
  }

  private fun maybeReportFailure(
    logger: Logger,
    validator: DependencyValidator
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

fun createOracle(project: Project, extension: LibYearExtension): VersionOracle =
  DefaultVersionOracle(
    HttpUrlAdapter(),
    collectRepositoryToVersionAdapter(extension),
    collectAllRepositories(project)
  )

private fun collectRepositoryToVersionAdapter(extension: LibYearExtension): Map<String, VersionInfoAdapter> {
  return defaultAdaptersByRepository() + extension.versionAdapters
}

private fun defaultAdaptersByRepository() = mapOf(
  ArtifactRepositoryContainer.DEFAULT_MAVEN_CENTRAL_REPO_NAME to SolrSearchAdapter.forMavenCentral(),
  ArtifactRepositoryContainer.DEFAULT_MAVEN_LOCAL_REPO_NAME to MavenLocalAdapter()
)

private fun collectAllRepositories(project: Project): Map<String, ArtifactRepository> {
  val resolutionManagement = (project.gradle as DefaultGradle).settings.dependencyResolutionManagement

  val repositories = when (resolutionManagement.repositoriesMode.get()) {
    RepositoriesMode.PREFER_PROJECT -> project.repositories.ifEmpty { resolutionManagement.repositories }
    RepositoriesMode.PREFER_SETTINGS, RepositoriesMode.FAIL_ON_PROJECT_REPOS -> resolutionManagement.repositories
  }

  return repositories.associateBy { it.name }
}
