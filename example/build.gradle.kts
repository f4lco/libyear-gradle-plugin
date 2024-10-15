plugins {
  id("com.libyear.libyear-gradle-plugin")
  java
}

dependencies {
  implementation("org.apache.commons:commons-text:1.12.0")
  implementation("org.apache.commons:commons-collections4:4.4")
}

libyear {
  // in CI, this may fail with 429 Too many requests
  failOnError = false
  // validator = allArtifactsCombinedMustNotBeOlderThan(5.months)
  validator = singleArtifactMustNotBeOlderThan(5.years)
  // validator = singleArtifactMustNotBeOlderThan((1.days)
}
