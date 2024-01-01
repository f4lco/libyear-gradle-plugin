plugins {
  id("com.libyear.libyear-gradle-plugin")
  java
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web:2.7.18")
}

libyear {
  // in CI, this may fail with 429 Too many requests
  failOnError = false
  validator = allArtifactsCombinedMustNotBeOlderThan(100.years)
}
