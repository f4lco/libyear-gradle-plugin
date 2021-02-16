package com.libyear

import com.libyear.validator.CumulativeAgeValidatorSpec
import com.libyear.validator.DependencyValidatorSpec
import java.time.Clock
import java.time.Duration
import java.time.temporal.ChronoUnit

open class LibYearExtension {

  companion object {
    val DEFAULT_MAX_AGE: Duration = ChronoUnit.YEARS.duration.multipliedBy(10)
  }

  var failOnError: Boolean = true

  var validator: DependencyValidatorSpec = CumulativeAgeValidatorSpec(DEFAULT_MAX_AGE)

  var clock: Clock = Clock.systemUTC()
}
