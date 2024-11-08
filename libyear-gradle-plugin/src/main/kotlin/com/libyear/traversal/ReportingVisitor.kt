package com.libyear.traversal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.libyear.sourcing.VersionOracle
import com.libyear.util.formatApproximate
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.internal.artifacts.result.ResolvedComponentResultInternal
import org.gradle.api.logging.Logger
import java.io.File
import java.time.Duration

private data class ReportingInfo(
  val module: ModuleVersionIdentifier,
  val lag: Duration,
  val latestVersion: String
)

class ReportingVisitor(
  logger: Logger,
  private val ageOracle: VersionOracle
) : DependencyVisitor(logger) {

  private val collected = mutableListOf<ReportingInfo>()
  private val missingInfo = mutableListOf<ModuleVersionIdentifier>()
  private val errors = mutableListOf<ModuleVersionIdentifier>()

  private val totalAge: Duration get() = collected.map { it.lag }.fold(Duration.ZERO, Duration::plus)

  override fun canContinue() = true

  override fun visitResolvedComponentResult(component: ResolvedComponentResult) {
    val module = component.moduleVersion ?: return
    val repositoryName = extractRepositoryName(component) ?: return
    val age = ageOracle.get(module, repositoryName)

    age.onSuccess { info ->
      val update = info.update
      if (update != null) {
        collected += ReportingInfo(module, update.lag, update.nextVersion)
      } else {
        missingInfo += module
      }
    }.onFailure {
      errors += module
      logger.error("""Cannot determine dependency age for "$module" and repository "$repositoryName" (reason: ${it::class.simpleName}: ${it.message}).""")
    }
  }

  private fun extractRepositoryName(result: ResolvedComponentResult): String? {
    if (result is ResolvedComponentResultInternal) {
      return result.repositoryName
    }
    return null
  }

  fun print() {
    if (missingInfo.isNotEmpty()) {
      logger.lifecycle("Dependencies with no update information available:")
      missingInfo.forEach { module ->
        logger.lifecycle(" -> ${module.group}:${module.name}:${module.version}")
      }
    }

    if (errors.isNotEmpty()) {
      logger.lifecycle("Dependencies with errors during age determination:")
      errors.forEach { module ->
        logger.lifecycle(" -> ${module.group}:${module.name}:${module.version}")
      }
    }

    if (missingInfo.isNotEmpty() || errors.isNotEmpty()) {
      logger.lifecycle("") // Blank line
    }

    logger.lifecycle("Collected ${totalAge.formatApproximate()} worth of libyears from ${collected.size} dependencies:")
    collected.sortedWith(byAgeAndModule()).forEach { dep ->
      logger.lifecycle(" -> ${dep.lag.formatApproximate().padEnd(10)} from ${dep.module.module} (${dep.module.version} => ${dep.latestVersion})")
    }
  }

  fun saveReportToJson(project: Project) {
    val report = mapOf(
      "collected" to collected.map {
        mapOf(
          "module" to it.module,
          "lag_days" to it.lag.toDays()
        )
      },
      "missing_info" to missingInfo,
      "errors" to errors
    )
    val objectMapper = ObjectMapper().registerKotlinModule()
    val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report)
    val reportFile = File(project.buildDir, "reports/libyear/libyear.json")
    reportFile.parentFile.mkdirs()
    reportFile.writeText(json)
  }

  private fun byAgeAndModule(): Comparator<ReportingInfo> = compareByDescending<ReportingInfo> { it.lag }.thenComparing(ReportingInfo::module, byModule())

  private fun byModule(): Comparator<ModuleVersionIdentifier> = compareBy(
    ModuleVersionIdentifier::getGroup,
    ModuleVersionIdentifier::getName,
    ModuleVersionIdentifier::getVersion
  )
}
