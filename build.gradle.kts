
plugins {
  // Workaround for Ktlint bug "Version 10.0.0 fails in non-Kotlin projects"
  // https://github.com/JLLeitschuh/ktlint-gradle/issues/443
  // (Fix not yet released)
  `embedded-kotlin`

  id("org.jlleitschuh.gradle.ktlint").version("10.0.0")
}

allprojects {

  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  repositories {
    mavenCentral()
  }
}

// Workaround for Ktlint bug "Version 10.0.0 fails in non-Kotlin projects"
// (should again be `register` after we remove the Kotlin plugin application)
tasks.named("check") {
  dependsOn(gradle.includedBuild("libyear-gradle-plugin").task(":check"))
}
