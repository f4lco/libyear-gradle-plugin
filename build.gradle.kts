
plugins {
  id("org.jlleitschuh.gradle.ktlint").version("9.4.1")
}

allprojects {

  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  repositories {
    mavenCentral()
  }
}

tasks.register("check") {
  dependsOn(gradle.includedBuild("libyear-gradle-plugin").task(":check"))
}
