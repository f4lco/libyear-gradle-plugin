package com.libyear

import com.libyear.traversal.DependencyTraversal
import com.libyear.traversal.ReportingVisitor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class LibYearReportTask : DefaultTask() {

  @TaskAction
  fun execute() {
    val extension = project.extensions.getByName(LibYearPlugin.EXTENSION_NAME) as LibYearExtension
    extension.configurations
      .map { project.configurations.getByName(it) }
      .forEach {
        val ageOracle = createOracle(project, extension)
        val visitor = ReportingVisitor(project.logger, ageOracle)
        DependencyTraversal.visit(it.incoming.resolutionResult.root, visitor, extension.ignoreTransitive)
        visitor.print()
      }
  }
}
