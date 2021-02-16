import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.4.20"
  `java-gradle-plugin`
  `maven-publish`
  id("org.jlleitschuh.gradle.ktlint").version("9.4.1")
}

group = "org.example.libyear"
version = "0.1"

tasks.withType(JavaCompile::class) {
  sourceCompatibility = "1.8"
  targetCompatibility = "1.8"
}

tasks.withType(KotlinCompile::class) {
  kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
  plugins {
    create("libyear") {
      id = "org.example.libyear-gradle"
      implementationClass = "com.libyear.LibYearPlugin"
    }
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.squareup.okhttp3:okhttp:4.9.0")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.9.9")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

publishing {
  publications {
    create<MavenPublication>("pluginMaven") {
      groupId = "org.example.libyear"
      artifactId = "libyear-gradle"
      version = "0.1"
    }
  }
}

tasks.test {
  useJUnitPlatform()
}
