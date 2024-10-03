package com.libyear.traversal

import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ComponentResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

class DependencyTraversal private constructor(
  private val visitor: DependencyVisitor,
  private val maxTransitiveDepth: Int?,
  private val excludedPackages: Set<String>
) {

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
        if (maxTransitiveDepth != null && depth > maxTransitiveDepth) {
          continue
        }

        if (excludedPackages.any { selected.moduleVersion.toString().contains(it, ignoreCase = true) }) {
          continue
        }
        
        nextComponents.add(dependency.selected)

      }
    }

    for (nextComponent in nextComponents) {
      visit(nextComponent, depth + 1)
      if (!visitor.canContinue()) break
    }
  }

  companion object {

    fun visit(
      root: ResolvedComponentResult,
      visitor: DependencyVisitor,
      maxTransitiveDepth: Int? = null,
      excludedPackages: Set<String> = setOf()
    ): Unit = DependencyTraversal(visitor, maxTransitiveDepth, excludedPackages).visit(root, depth = 0)
  }
}
