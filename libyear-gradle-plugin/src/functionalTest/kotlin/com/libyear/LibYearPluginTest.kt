package com.libyear

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal class LibYearPluginTest {

  @TempDir
  lateinit var project: Path

  @Test
  fun testValidDependencies() {
    setUpProject("valid.gradle.kts")

    val result = withGradleRunner().build()

    assertThat(result.task(":dependencies"))
      .isNotNull()
      .extracting { it?.outcome }.isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test
  fun testInvalidDependencies() {
    setUpProject("invalid.gradle.kts")

    val result = withGradleRunner().buildAndFail()

    assertThat(result.task(":dependencies"))
      .isNotNull()
      .extracting { it?.outcome }.isEqualTo(TaskOutcome.FAILED)
  }

  @Test
  fun testExcludedPackages() {
    setUpProject("excludedPackages.gradle.kts")

    val result = withGradleRunner("reportLibyears").build()
    val output = result.output

    assertThat(output)
      .contains("from 3 dependencies")
      .contains("org.apache.commons:commons-text")
      .contains("org.apache.commons:commons-collections")
    assertThat(output).doesNotContain("unknown.package")
  }

  private fun withGradleRunner(command: String = "dependencies") = GradleRunner.create().apply {
    withProjectDir(project.toFile())
    withArguments(command)
    withPluginClasspath()
  }

  private fun setUpProject(buildFile: String) {
    val sourceUrl = javaClass.getResource(buildFile) ?: throw IllegalArgumentException("$buildFile not found")
    val sourceBuildFile = Paths.get(sourceUrl.toURI())
    val targetBuildFile = project.resolve("build.gradle.kts")
    Files.copy(sourceBuildFile, targetBuildFile)
  }
}
