package com.libyear.traversal

import org.gradle.api.artifacts.result.ComponentResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

class DependencyTraversal private constructor(
  private val visitor: DependencyVisitor
) {

  private fun visit(component: ComponentResult) {
    visitor.visitComponentResult(component)
    if (component !is ResolvedComponentResult) return

    val nextComponents = mutableListOf<ComponentResult>()
    for (dependency in component.dependencies) {
      visitor.visitDependencyResult(dependency)
      if (!visitor.canContinue()) return

      if (dependency is ResolvedDependencyResult) {
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
    ): Unit = DependencyTraversal(visitor).visit(root)
  }
}
