import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

plugins {
  id("org.example.libyear-gradle")
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
  clock = Clock.fixed(LocalDate.of(2020, 12, 11).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
  failOnError = true
  // validator = com.libyear.validator.CumulativeAgeValidatorSpec(ChronoUnit.MONTHS.duration.multipliedBy(5))
  validator = com.libyear.validator.AgeValidatorSpec(ChronoUnit.YEARS.duration.multipliedBy(5))
  //validator = com.libyear.validator.AgeValidatorSpec(Duration.ofDays(1))
}
