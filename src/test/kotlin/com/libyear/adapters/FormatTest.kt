package com.libyear.adapters

import com.libyear.formatApproximate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.time.temporal.ChronoUnit

class FormatTest {

  @ParameterizedTest()
  @MethodSource("samples")
  fun checkDurationFormat(sample: Sample) {
    assertEquals(sample.formatted, sample.input.formatApproximate())
  }

  data class Sample(
    val input: Duration,
    val formatted: String,
  )

  companion object {
    @JvmStatic
    fun samples() = listOf(
      Sample(ChronoUnit.YEARS.duration, "1 year"),
      Sample(ChronoUnit.YEARS.duration.multipliedBy(2), "2 years"),

      Sample(Duration.ofDays(1), "1 day"),
      Sample(Duration.ofDays(5), "5 days"),

      Sample(Duration.ofHours(1).plusMinutes(5).plusSeconds(55), "1 hour"),
      Sample(Duration.ofNanos(42), "1 second"),
    )
  }
}
