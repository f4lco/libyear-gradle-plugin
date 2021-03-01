package com.libyear.traversal

import com.libyear.sourcing.VersionOracle
import com.libyear.util.formatApproximate
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.internal.artifacts.result.ResolvedComponentResultInternal
import org.gradle.api.logging.Logger
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

  private val totalAge: Duration get() = collected.map { it.lag }.fold(Duration.ZERO, Duration::plus)

  override fun canContinue() = true

  override fun visitResolvedComponentResult(component: ResolvedComponentResult) {
    val module = component.moduleVersion ?: return
    val repositoryName = extractRepositoryName(component) ?: return
    val age = ageOracle.get(module, repositoryName)

    age.onSuccess { info ->
      info.update?.let { update ->
        collected += ReportingInfo(module, update.lag, update.nextVersion)
      }
    }.onFailure {
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
    logger.lifecycle("Collected ${totalAge.formatApproximate()}  worth of libyears from ${collected.size} dependencies:")
    collected.sortedWith(byAgeAndModule()).forEach { dep ->
      logger.lifecycle(" -> ${dep.lag.formatApproximate().padEnd(10)} from ${dep.module.module} (${dep.module.version} => ${dep.latestVersion})")
    }
  }

  private fun byAgeAndModule(): Comparator<ReportingInfo> = compareByDescending<ReportingInfo> { it.lag }.thenComparing(ReportingInfo::module, byModule())

  private fun byModule(): Comparator<ModuleVersionIdentifier> = compareBy(
    ModuleVersionIdentifier::getGroup,
    ModuleVersionIdentifier::getName,
    ModuleVersionIdentifier::getVersion
  )
}
