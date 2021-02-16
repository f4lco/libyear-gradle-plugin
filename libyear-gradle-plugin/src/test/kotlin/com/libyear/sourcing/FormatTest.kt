package com.libyear.sourcing

import com.libyear.formatApproximate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.time.temporal.ChronoUnit

internal class FormatTest {

  @ParameterizedTest()
  @MethodSource("samples")
  fun checkDurationFormat(sample: Sample) {
    assertThat(sample.input.formatApproximate()).isEqualTo(sample.formatted)
  }

  data class Sample(
    val input: Duration,
    val formatted: String
  )

  companion object {
    @JvmStatic
    fun samples() = listOf(
      Sample(ChronoUnit.YEARS.duration, "1 year"),
      Sample(ChronoUnit.YEARS.duration.multipliedBy(2), "2 years"),

      Sample(Duration.ofDays(1), "1 day"),
      Sample(Duration.ofDays(5), "5 days"),

      Sample(Duration.ofHours(1).plusMinutes(5).plusSeconds(55), "1 hour"),
      Sample(Duration.ofNanos(42), "1 second")
    )
  }
}
