plugins {
  id("com.libyear.libyear-gradle-plugin")
  java
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.apache.commons:commons-text:1.9")
  implementation("org.apache.commons:commons-collections4:4.4")
}

libyear {
  failOnError = true
  validator = singleArtifactMustNotBeOlderThan(5.years)
}
