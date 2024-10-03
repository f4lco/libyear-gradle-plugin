package com.libyear

import com.libyear.sourcing.VersionInfoAdapter
import com.libyear.validator.AgeValidatorSpec
import com.libyear.validator.CumulativeAgeValidatorSpec
import com.libyear.validator.DependencyValidatorSpec
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

open class LibYearExtension {

  // Settings

  var versionAdapters = mutableMapOf<String, VersionInfoAdapter>()

  var failOnError: Boolean = true

  var validator: DependencyValidatorSpec = defaultValidator

  var ignoreTransitive: Boolean = false

  var configurations: List<String> = defaultConfigurations

  // DSL for build script authors

  val defaultConfigurations: List<String> get() = listOf("compileClasspath")

  val defaultValidator: DependencyValidatorSpec get() = CumulativeAgeValidatorSpec(DEFAULT_MAX_AGE)

  fun allArtifactsCombinedMustNotBeOlderThan(maxAge: Duration): DependencyValidatorSpec {
    return CumulativeAgeValidatorSpec(maxAge)
  }

  fun singleArtifactMustNotBeOlderThan(maxAge: Duration): DependencyValidatorSpec {
    return AgeValidatorSpec(maxAge)
  }

  fun fixedClock(year: Int, month: Int, dayOfMonth: Int): Clock = Clock.fixed(
    LocalDate.of(
      year,
      month,
      dayOfMonth
    ).atStartOfDay().toInstant(ZoneOffset.UTC),
    ZoneOffset.UTC
  )

  // extension properties for Kotlin DSL
  val Int.days: Duration get() = ChronoUnit.DAYS.duration.multipliedBy(this.toLong())
  val Int.months: Duration get() = ChronoUnit.MONTHS.duration.multipliedBy(this.toLong())
  val Int.years: Duration get() = ChronoUnit.YEARS.duration.multipliedBy(this.toLong())

  // simple methods for Groovy build scripts
  fun days(x: Int): Duration = ChronoUnit.DAYS.duration.multipliedBy(x.toLong())
  fun months(x: Int): Duration = ChronoUnit.MONTHS.duration.multipliedBy(x.toLong())
  fun years(x: Int): Duration = ChronoUnit.YEARS.duration.multipliedBy(x.toLong())

  companion object {
    val DEFAULT_MAX_AGE: Duration = ChronoUnit.YEARS.duration.multipliedBy(10)
  }
}
