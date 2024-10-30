package com.libyear.traversal

import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ComponentResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.logging.Logger
import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

class DependencyTraversal private constructor(
  private val logger: Logger,
  private val visitor: DependencyVisitor,
  private val maxTransitiveDepth: Int?,
  excludeModules: Set<String>,
  includeModules: Set<String>
) {
  private val excludePatterns = excludeModules.map { it to it.wildcardToRegex() }
  private val includePatterns = includeModules.map { it to it.wildcardToRegex() }

  private val seen = mutableSetOf<ComponentIdentifier>()

  private fun visit(component: ComponentResult, depth: Int = 0) {
    if (!seen.add(component.id)) return

    visitor.visitComponentResult(component)
    if (component !is ResolvedComponentResult) return

    val nextComponents = mutableListOf<ComponentResult>()
    for (dependency in component.dependencies) {
      visitor.visitDependencyResult(dependency)
      if (!visitor.canContinue()) return

      if (dependency is ResolvedDependencyResult) {
        if (shouldIncludeModule(dependency.selected.moduleVersion?.name.toString(), depth)) {
          nextComponents.add(dependency.selected)
        }
      }
    }

    for (nextComponent in nextComponents) {
      visit(nextComponent, depth + 1)
      if (!visitor.canContinue()) break
    }
  }

  @VisibleForTesting
  fun shouldIncludeModule(
    module: String,
    depth: Int
  ): Boolean {
    if (maxTransitiveDepth != null && depth > maxTransitiveDepth) {
      return false
    }

    // Inclusions supersede exclusions
    val matchedInclusion = includePatterns.firstOrNull { pattern -> pattern.second.matches(module) }
    val matchedExclusion = excludePatterns.firstOrNull { pattern -> pattern.second.matches(module) }

    if(matchedInclusion != null) {
      logger.info("Including $module because it matches ${matchedInclusion.first}")
    } else if(matchedExclusion != null) {
      logger.info("Excluding $module because it matches ${matchedExclusion.first}")
      return false
    }

    return true
  }

  companion object {

    fun visit(
      logger: Logger,
      root: ResolvedComponentResult,
      visitor: DependencyVisitor,
      maxTransitiveDepth: Int? = null,
      excludeModules: Set<String> = emptySet(),
      includeModules: Set<String> = emptySet()
    ): Unit = DependencyTraversal(
      logger,
      visitor,
      maxTransitiveDepth,
      excludeModules,
      includeModules
    ).visit(root, depth = 0)

    @VisibleForTesting
    fun String.wildcardToRegex(): Regex {
      val globsToRegex = this.replace(".", "\\.").replace("*", ".*")
      return "^${globsToRegex}$".toRegex(RegexOption.IGNORE_CASE)
    }
  }
}
