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
  fun testFilterModules() {
    setUpProject("filterModules.gradle.kts")

    val result = withGradleRunner("reportLibyears").build()
    val output = result.output

    assertThat(output)
      .contains("from 1 dependencies")
      .contains("org.slf4j:slf4j-api")
    assertThat(output).doesNotContain("unknown.package")
  }

  @Test
  fun testReportLibyear() {
    setUpProject("valid.gradle.kts")
    withGradleRunner("reportLibyear").build()
    val libyearJsonFile = project.resolve("build/reports/libyear/libyear.json").toFile().readText()
    val expectedJson = javaClass.getResourceAsStream("expectedReport.json")?.bufferedReader()?.use { it.readText() }

    // Replace lagDays values with 0 using regex so reports match despite age
    val lagDaysRegex = "\"lag_days\"\\s*:\\s*\\d+".toRegex()
    val normalizedLibyearJson = libyearJsonFile.replace(lagDaysRegex, "\"lag_days\": 0")
    val normalizedExpectedJson = expectedJson?.replace(lagDaysRegex, "\"lag_days\": 0")

    assertThat(normalizedLibyearJson).isEqualToIgnoringWhitespace(normalizedExpectedJson)
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
