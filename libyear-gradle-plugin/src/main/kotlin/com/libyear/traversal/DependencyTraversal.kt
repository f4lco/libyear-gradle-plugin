package com.libyear.traversal

import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ComponentResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

class DependencyTraversal private constructor(
  private val visitor: DependencyVisitor,
  private val ignoreTransitive: Boolean
) {

  private val seen = mutableSetOf<ComponentIdentifier>()

  private fun visit(component: ComponentResult, isRoot: Boolean = false) {
    if (!seen.add(component.id)) return

    visitor.visitComponentResult(component)
    if (component !is ResolvedComponentResult) return

    val nextComponents = mutableListOf<ComponentResult>()
    for (dependency in component.dependencies) {
      visitor.visitDependencyResult(dependency)
      if (!visitor.canContinue()) return

      if (dependency is ResolvedDependencyResult) {
        if (ignoreTransitive && !isRoot) {
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
      root: ResolvedComponentResult,
      visitor: DependencyVisitor,
      ignoreTransitive: Boolean = false
    ): Unit = DependencyTraversal(visitor, ignoreTransitive).visit(root, isRoot = true)
  }
}
