plugins {
  id("com.libyear.libyear-gradle-plugin")
  java
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web:2.4.3")
}

libyear {
  failOnError = true
  validator = allArtifactsCombinedMustNotBeOlderThan(100.years)
}
