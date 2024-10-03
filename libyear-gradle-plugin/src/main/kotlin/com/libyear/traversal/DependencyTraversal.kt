package com.libyear.traversal

import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ComponentResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.logging.Logger

class DependencyTraversal private constructor(
  private val logger: Logger,
  private val visitor: DependencyVisitor,
  private val ignoreTransitive: Boolean // Step 1: Add flag
) {

  private val seen = mutableSetOf<ComponentIdentifier>()

  private fun visit(component: ComponentResult) {
    if (!seen.add(component.id)) return

    visitor.visitComponentResult(component)
    if (component !is ResolvedComponentResult) return

    val nextComponents = mutableListOf<ComponentResult>()
    for (dependency in component.dependencies) {
      visitor.visitDependencyResult(dependency)
      if (!visitor.canContinue()) return

      if (dependency is ResolvedDependencyResult) {
        if (ignoreTransitive) {
          logger.lifecycle("Ignoring transitive dependency: ${dependency.selected.id}")
          continue
        }
        nextComponents.add(dependency.selected)
      }
    }

    for (nextComponent in nextComponents) {
      visit(nextComponent)
      if (!visitor.canContinue()) break
    }
  }

  companion object {

    fun visit(
      logger: Logger,
      root: ResolvedComponentResult,
      visitor: DependencyVisitor,
      ignoreTransitive: Boolean = false
    ): Unit = DependencyTraversal(logger, visitor, ignoreTransitive).visit(root)
  }
}
