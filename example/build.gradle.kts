plugins {
  id("com.libyear.libyear-gradle-plugin")
  java
}

dependencies {
  implementation("org.apache.commons:commons-text:1.9")
  implementation("org.apache.commons:commons-collections4:4.4")
}

libyear {
  failOnError = false
  // validator = allArtifactsCombinedMustNotBeOlderThan(5.months)
  validator = singleArtifactMustNotBeOlderThan(5.years)
  // validator = singleArtifactMustNotBeOlderThan((1.days)
}
