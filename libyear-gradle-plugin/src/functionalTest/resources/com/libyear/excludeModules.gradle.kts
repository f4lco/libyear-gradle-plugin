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
    implementation("org.slf4j", "slf4j-api", "2.0.9")
    implementation("org.slf4j", "slf4j-api-core", "2.0.9")
}

libyear {
    failOnError = false
    validator = singleArtifactMustNotBeOlderThan(100.years)
    excludeModules = setOf("org.slf4j*")
    includeModules = setOf("*slf4j-api-core*")
}
