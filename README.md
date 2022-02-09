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
Collected 9.5 years  worth of libyears from 13 dependencies:
 -> 2.6 years  from ch.qos.logback:logback-core (1.2.3 => 1.3.0-alpha5)
 -> 2.6 years  from ch.qos.logback:logback-classic (1.2.3 => 1.3.0-alpha5)
 -> 1.3 years  from org.glassfish:jakarta.el (3.0.3 => 4.0.1)
 -> 1.2 years  from jakarta.annotation:jakarta.annotation-api (1.3.5 => 2.0.0)
 -> 5.9 months from org.apache.logging.log4j:log4j-api (2.13.3 => 2.14.0)
 -> 5.9 months from org.apache.logging.log4j:log4j-to-slf4j (2.13.3 => 2.14.0)
 -> 5.4 months from org.yaml:snakeyaml (1.27 => 1.28)
 -> 28 days    from com.fasterxml.jackson.core:jackson-annotations (2.11.4 => 2.12.1)
 -> 28 days    from com.fasterxml.jackson.core:jackson-core (2.11.4 => 2.12.1)
 -> 28 days    from com.fasterxml.jackson.datatype:jackson-datatype-jsr310 (2.11.4 => 2.12.1)
 -> 28 days    from com.fasterxml.jackson.datatype:jackson-datatype-jdk8 (2.11.4 => 2.12.1)
 -> 28 days    from com.fasterxml.jackson.module:jackson-module-parameter-names (2.11.4 => 2.12.1)
 -> 28 days    from com.fasterxml.jackson.core:jackson-databind (2.11.4 => 2.12.1)

BUILD SUCCESSFUL in 29s
1 actionable task: 1 executed
```

## Changelog

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
