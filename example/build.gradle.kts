import java.time.Duration
import java.time.Period
import java.time.temporal.ChronoUnit

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
  // validator = com.libyear.validator.CumulativeAgeValidatorSpec(ChronoUnit.MONTHS.duration.multipliedBy(5))
  validator = com.libyear.validator.AgeValidatorSpec(ChronoUnit.YEARS.duration.multipliedBy(5))
  //validator = com.libyear.validator.AgeValidatorSpec(Duration.ofDays(1))
}
