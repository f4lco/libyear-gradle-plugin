package com.libyear.traversal

import com.libyear.sourcing.AgeOracle
import com.libyear.util.formatApproximate
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.internal.artifacts.result.ResolvedComponentResultInternal
import org.gradle.api.logging.Logger
import java.time.Duration

private data class ReportingInfo(
  val module: ModuleVersionIdentifier,
  val age: Duration
)

class ReportingVisitor(
  logger: Logger,
  private val ageOracle: AgeOracle
) : DependencyVisitor(logger) {

  private val collected = mutableListOf<ReportingInfo>()

  private val totalAge: Duration get() = collected.map { it.age }.fold(Duration.ZERO, Duration::plus)

  override fun canContinue() = true

  override fun visitResolvedComponentResult(component: ResolvedComponentResult) {
    val module = component.moduleVersion ?: return
    val repositoryName = extractRepositoryName(component) ?: return
    val age = ageOracle.get(module, repositoryName)

    age.onSuccess {
      collected += ReportingInfo(module, it)
    }.onFailure {
      logger.error("""Cannot determine dependency age for "$module" and repository "$repositoryName" (reason: ${it::class.simpleName}).""")
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
      logger.lifecycle(" -> ${dep.age.formatApproximate().padEnd(10)} from ${dep.module}")
    }
  }

  private fun byAgeAndModule(): Comparator<ReportingInfo> = compareByDescending<ReportingInfo> { it.age }.thenComparing(ReportingInfo::module, byModule())

  private fun byModule(): Comparator<ModuleVersionIdentifier> = compareBy(
    ModuleVersionIdentifier::getGroup,
    ModuleVersionIdentifier::getName,
    ModuleVersionIdentifier::getVersion
  )
}
