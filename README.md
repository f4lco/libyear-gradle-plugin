# Libyear Gradle Plugin

[![Actions Status](https://github.com/f4lco/libyear-gradle-plugin/actions/workflows/build-test.yml/badge.svg)](https://github.com/f4lco/libyear-gradle-plugin/actions)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/libyear/libyear-gradle-plugin/com.libyear.libyear-gradle-plugin.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/com.libyear.libyear-gradle-plugin)

This Gradle plugin measures **libyears** of your project.
libyears is a simple measure of software dependency freshness.
It is a **single number** telling you how up-to-date your dependencies are.
Learn more on [libyear.com][libyear].

![libyear before comic](https://libyear.com/cartoon1.jpg) ![libyear after comic](https://libyear.com/cartoon2.jpg)

## Usage

Update your Gradle build instructions with the following plugin application, which will automatically enable a dependency check after resolving dependencies:

```kotlin
// in Kotlin / build.gradle.kts:
plugins {
  id("com.libyear.libyear-gradle-plugin").version("<version>")
}

libyear {
   // Which dependency configuration to check?
  configurations = listOf("compileClasspath")
  
  // Fail in case fetching dependency metadata fails?
  failOnError = true
  
  // How to validate:
  validator = allArtifactsCombinedMustNotBeOlderThan(5.years)
  
  // alternatively:
  validator = singleArtifactMustNotBeOlderThan(2.days)
}
```

```groovy
// in Groovy / build.gradle:
plugins {
  id 'com.libyear.libyear-gradle-plugin' version "<version>"
}

libyear {
  configurations = ['compileClasspath']
  failOnError = true
  validator = allArtifactsCombinedMustNotBeOlderThan(days(5))
}
```

Please refer to [LibYearExtension.kt][0] for all possible configuration options and additional documentation.


## Tasks

The `reportLibyears` task shows a table with all outdated dependencies, including their newest version numbers and impact on the total of libyears.

## Example

If you build on a modern framework such as Spring Boot, you invite about 10 libyears into your project. And you haven't actually started to do something useful, did you?

```
> Task :example-spring-boot:reportLibyears
Collected 2.3 decades  worth of libyears from 33 dependencies:
 -> 4.2 years  from jakarta.annotation:jakarta.annotation-api (1.3.5 => 3.0.0-M1)
 -> 1.9 years  from org.slf4j:jul-to-slf4j (1.7.36 => 2.1.0-alpha0)
 -> 1.9 years  from org.slf4j:slf4j-api (1.7.36 => 2.1.0-alpha0)
 -> 1.8 years  from org.apache.logging.log4j:log4j-api (2.17.2 => 3.0.0-beta1)
 -> 1.8 years  from org.apache.logging.log4j:log4j-to-slf4j (2.17.2 => 3.0.0-beta1)
 -> 1.7 years  from org.yaml:snakeyaml (1.30 => 2.2)
 -> 11 months  from com.fasterxml.jackson.module:jackson-module-parameter-names (2.13.5 => 2.16.1)
 -> 11 months  from com.fasterxml.jackson.datatype:jackson-datatype-jdk8 (2.13.5 => 2.16.1)
 -> 11 months  from com.fasterxml.jackson.datatype:jackson-datatype-jsr310 (2.13.5 => 2.16.1)
 -> 11 months  from com.fasterxml.jackson.core:jackson-annotations (2.13.5 => 2.16.1)
 -> 11 months  from com.fasterxml.jackson:jackson-bom (2.13.5 => 2.16.1)
 -> 11 months  from com.fasterxml.jackson.core:jackson-core (2.13.5 => 2.16.1)
 -> 11 months  from com.fasterxml.jackson.core:jackson-databind (2.13.5 => 2.16.1)
 -> 8.3 months from ch.qos.logback:logback-core (1.2.12 => 1.4.14)
 -> 8.3 months from ch.qos.logback:logback-classic (1.2.12 => 1.4.14)
 -> 28.2 days  from org.springframework.boot:spring-boot-starter-tomcat (2.7.18 => 3.2.1)
 -> 28.2 days  from org.springframework.boot:spring-boot-starter-web (2.7.18 => 3.2.1)
 -> 28.2 days  from org.springframework.boot:spring-boot (2.7.18 => 3.2.1)
 -> 28.2 days  from org.springframework.boot:spring-boot-starter (2.7.18 => 3.2.1)
 -> 28.2 days  from org.springframework.boot:spring-boot-starter-json (2.7.18 => 3.2.1)
 -> 28.2 days  from org.springframework.boot:spring-boot-starter-logging (2.7.18 => 3.2.1)
 -> 28.2 days  from org.springframework.boot:spring-boot-autoconfigure (2.7.18 => 3.2.1)
 -> 28.1 days  from org.springframework:spring-aop (5.3.31 => 6.1.2)
 -> 28.1 days  from org.springframework:spring-context (5.3.31 => 6.1.2)
 -> 28.1 days  from org.springframework:spring-core (5.3.31 => 6.1.2)
 -> 28.1 days  from org.springframework:spring-expression (5.3.31 => 6.1.2)
 -> 28.1 days  from org.springframework:spring-web (5.3.31 => 6.1.2)
 -> 28.1 days  from org.springframework:spring-webmvc (5.3.31 => 6.1.2)
 -> 28.1 days  from org.springframework:spring-jcl (5.3.31 => 6.1.2)
 -> 28.1 days  from org.springframework:spring-beans (5.3.31 => 6.1.2)
 -> 27.7 days  from org.apache.tomcat.embed:tomcat-embed-websocket (9.0.83 => 11.0.0-M15)
 -> 27.7 days  from org.apache.tomcat.embed:tomcat-embed-el (9.0.83 => 11.0.0-M15)
 -> 27.7 days  from org.apache.tomcat.embed:tomcat-embed-core (9.0.83 => 11.0.0-M15)

BUILD SUCCESSFUL in 28s
1 actionable task: 1 executed
```

## Changelog

### 0.1.8 (2024-06-17)

The build of this release tests against the latest Gradle 7.x and 8.x release.

### 0.1.7 (2024-01-01)

The build of this release tests against the latest Gradle 7.x release.

### 0.1.6 (2022-02-09)

This release uses Gradle 6.9.2 as baseline for running the test suite and the plugin deployment.

### 0.1.5 (2022-02-09)

This release uses Gradle 6.9.1 as baseline for running the test suite and the plugin deployment.

### 0.1.4 (2021-06-24)

The violators report now excludes up-to-date dependencies which contribute zero seconds to the accumulated libyears.

The build runs against Gradle 6.9, Gradle 7.1, and Ktlint 10.1.0.

### 0.1.3 (2021-06-23)

The plugin now also considers repositories defined in the settings script (`settings.gradle` or `settings.gradle.kts`) in addition to the project's repositories depending on the [repositories mode][repo-mode]. Previously, the plugin processed only the project's repositories.

The build also runs against Gradle 7 in addition to Gradle 6.x to prevent regressions.

## Acknowledgements

J. Cox, E. Bouwers, M. van Eekelen and J. Visser, [Measuring Dependency
Freshness in Software Systems][1]. In Proceedings of the 37th International
Conference on Software Engineering (ICSE 2015), May 2015

[0]: https://github.com/f4lco/libyear-gradle-plugin/blob/main/libyear-gradle-plugin/src/main/kotlin/com/libyear/LibYearExtension.kt

[1]: https://ericbouwers.github.io/papers/icse15.pdf

[libyear]: https://libyear.com/

[repo-mode]: https://docs.gradle.org/current/userguide/declaring_repositories.html#sub:centralized-repository-declaration
