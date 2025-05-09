package com.libyear.traversal

import com.libyear.sourcing.DefaultVersionOracle
import com.libyear.traversal.DependencyTraversal.Companion.wildcardToRegex
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DependencyTraversalTest {
  @Test
  fun testVisitsDependencies() {
    val project = ProjectBuilder.builder().build()
    val visitorSpy = spy(ReportingVisitor(project.logger, mock<DefaultVersionOracle>()))

    val rootComponent = mockResolvedComponentResult("root", "component")
    val slf4jComponent = rootComponent.addDependency("org.slf4j", "slf4j")
    val slf4jCoreComponent = rootComponent.addDependency("org.slf4j", "slf4j-core")
    val slf4jCoreSubComponentA = slf4jCoreComponent.addDependency("org.slf4j", "slf4j-core-a")
    val slf4jCoreSubComponentB = slf4jCoreComponent.addDependency("org.slf4j", "slf4j-core-b")

    DependencyTraversal.visit(
      project.logger,
      rootComponent.selected,
      visitorSpy,
      10
    )

    verify(visitorSpy).visitComponentResult(eq(rootComponent.selected))
    verify(visitorSpy).visitComponentResult(eq(slf4jComponent.selected))
    verify(visitorSpy).visitComponentResult(eq(slf4jCoreComponent.selected))
    verify(visitorSpy).visitComponentResult(eq(slf4jCoreSubComponentA.selected))
    verify(visitorSpy).visitComponentResult(eq(slf4jCoreSubComponentB.selected))
  }

  @Test
  fun testIncludedAndExcludedDependencies() {
    val project = ProjectBuilder.builder().build()
    val visitorSpy = spy(ReportingVisitor(project.logger, mock<DefaultVersionOracle>()))

    val includedModules = setOf(
      "org.slf4j*" // Will include all org.slf4j modules
    )
    val excludedModules = setOf(
      "*core-b" // Will NOT include slf4j-core-b
    )

    val rootComponent = mockResolvedComponentResult("root", "component")
    val slf4jComponent = rootComponent.addDependency("org.slf4j", "slf4j")
    val slf4jCoreComponent = rootComponent.addDependency("org.slf4j", "slf4j-core")
    val slf4jCoreSubComponentA = slf4jCoreComponent.addDependency("org.slf4j", "slf4j-core-a")
    val slf4jCoreSubComponentB = slf4jCoreComponent.addDependency("org.slf4j", "slf4j-core-b")

    DependencyTraversal.visit(
      project.logger,
      rootComponent.selected,
      visitorSpy,
      10,
      excludedModules,
      includedModules
    )

    verify(visitorSpy).visitComponentResult(eq(rootComponent.selected))
    // Excluded:
    verify(visitorSpy, never()).visitComponentResult(eq(slf4jCoreSubComponentB.selected))
    // Included
    verify(visitorSpy).visitComponentResult(eq(slf4jComponent.selected))
    verify(visitorSpy).visitComponentResult(eq(slf4jCoreComponent.selected))
    verify(visitorSpy).visitComponentResult(eq(slf4jCoreSubComponentA.selected))
  }

  @Test
  fun testExcludeDependencies() {
    val project = ProjectBuilder.builder().build()
    val visitorSpy = spy(ReportingVisitor(project.logger, mock<DefaultVersionOracle>()))

    val includedModules = emptySet<String>() // empty set means all are included
    val excludedModules = setOf(
      "*slf4j-core*" // Will NOT include core modules
    )

    val rootComponent = mockResolvedComponentResult("root", "component")
    val slf4jComponent = rootComponent.addDependency("org.slf4j", "slf4j")
    val slf4jCoreComponent = rootComponent.addDependency("org.slf4j", "slf4j-core")
    val slf4jCoreSubComponentA = slf4jCoreComponent.addDependency("org.slf4j", "slf4j-core-a")
    val slf4jCoreSubComponentB = slf4jCoreComponent.addDependency("org.slf4j", "slf4j-core-b")

    DependencyTraversal.visit(
      project.logger,
      rootComponent.selected,
      visitorSpy,
      10,
      excludedModules,
      includedModules
    )

    verify(visitorSpy).visitComponentResult(eq(rootComponent.selected))
    // Excluded:
    verify(visitorSpy, never()).visitComponentResult(eq(slf4jCoreComponent.selected))
    verify(visitorSpy, never()).visitComponentResult(eq(slf4jCoreSubComponentA.selected))
    verify(visitorSpy, never()).visitComponentResult(eq(slf4jCoreSubComponentB.selected))
    // Included
    verify(visitorSpy).visitComponentResult(eq(slf4jComponent.selected))
  }

  @Test
  fun testMaxTransitiveDepth() {
    val project = ProjectBuilder.builder().build()
    val visitorSpy = spy(ReportingVisitor(project.logger, mock<DefaultVersionOracle>()))

    val rootComponent = mockResolvedComponentResult("root", "component")
    val slf4jComponent = rootComponent.addDependency("org.slf4j", "slf4j")
    val slf4jCoreComponent = rootComponent.addDependency("org.slf4j", "slf4j-core")
    val slf4jCoreSubComponentA = slf4jCoreComponent.addDependency("org.slf4j", "slf4j-core-a")
    val slf4jCoreSubComponentB = slf4jCoreComponent.addDependency("org.slf4j", "slf4j-core-b")

    DependencyTraversal.visit(
      project.logger,
      rootComponent.selected,
      visitorSpy,
      0
    )

    // Root component doesn't count towards depth, it's just the starting point
    verify(visitorSpy).visitComponentResult(eq(rootComponent.selected))
    // These two nodes should be visited
    verify(visitorSpy).visitComponentResult(eq(slf4jComponent.selected))
    verify(visitorSpy).visitComponentResult(eq(slf4jCoreComponent.selected))
    // We stop traversing after root + 1 depth, these nodes are not visited:
    verify(visitorSpy, never()).visitComponentResult(eq(slf4jCoreSubComponentA.selected))
    verify(visitorSpy, never()).visitComponentResult(eq(slf4jCoreSubComponentB.selected))
  }

  @Test
  fun testWildcardToRegex() {
    assertEquals("com.libyear.*".wildcardToRegex().toString(), "^com\\.libyear\\..*$")
    assertEquals("com.libyear".wildcardToRegex().toString(), "^com\\.libyear$")
    assertEquals("*.libyear".wildcardToRegex().toString(), "^.*\\.libyear$")
    assertEquals("*.libyear.*".wildcardToRegex().toString(), "^.*\\.libyear\\..*$")
    assertEquals("*.libyear.*-core".wildcardToRegex().toString(), "^.*\\.libyear\\..*-core$")
    assertEquals("*.libyear.*-core*".wildcardToRegex().toString(), "^.*\\.libyear\\..*-core.*$")
    assertEquals("**".wildcardToRegex().toString(), "^.*.*$")
  }

  private fun mockResolvedComponentResult(group: String, name: String): ResolvedDependencyResult {
    val componentResult = mock<ResolvedComponentResult>().apply {
      val version = mock<ModuleVersionIdentifier>().apply {
        whenever(this.group).thenReturn(group)
        whenever(this.name).thenReturn(name)
      }
      whenever(this.moduleVersion).thenReturn(version)
      whenever(this.id).thenReturn(ComponentIdentifier { "$group:$name" })
    }
    return mock<ResolvedDependencyResult>().apply {
      whenever(this.selected).thenReturn(componentResult)
    }
  }

  private fun ResolvedDependencyResult.addDependency(group: String, name: String): ResolvedDependencyResult {
    val parent = this@addDependency.selected
    return mockResolvedComponentResult(group, name).apply {
      val newDependencies = (parent.dependencies + this).toMutableSet()
      whenever(parent.dependencies).thenReturn(newDependencies)
    }
  }
}
