package com.libyear.traversal

import org.gradle.api.artifacts.result.ComponentResult
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedComponentResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.logging.Logger

abstract class DependencyVisitor(
  protected val logger: Logger,
) {

  abstract fun canContinue(): Boolean

  open fun visitComponentResult(component: ComponentResult) {
    when (component) {
      is UnresolvedComponentResult -> visitUnresolvedComponentResult(component)
      is ResolvedComponentResult -> visitResolvedComponentResult(component)
      else -> throw AssertionError("Unknown component type $component")
    }
  }

  protected open fun visitUnresolvedComponentResult(component: UnresolvedComponentResult) {
    logger.warn("Unresolved component {}", component.id)
  }

  protected open fun visitResolvedComponentResult(component: ResolvedComponentResult) {}

  open fun visitDependencyResult(result: DependencyResult) {
    when (result) {
      is UnresolvedDependencyResult -> visitUnresolvedDependencyResult(result)
      is ResolvedDependencyResult -> visitResolvedDependencyResult(result)
      else -> throw AssertionError("Unknown dependency type $result")
    }
  }

  protected open fun visitUnresolvedDependencyResult(result: UnresolvedDependencyResult) {
    logger.warn("Unresolved dependency {}", result.requested.displayName)
  }

  protected open fun visitResolvedDependencyResult(result: ResolvedDependencyResult) {}
}
