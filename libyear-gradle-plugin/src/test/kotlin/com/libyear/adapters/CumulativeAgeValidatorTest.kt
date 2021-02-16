package com.libyear.adapters

import com.libyear.validator.CumulativeAgeValidatorSpec
import com.libyear.validator.DependencyInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

internal class CumulativeAgeValidatorTest {

  @Test
  fun testValid() {
    val validator = CumulativeAgeValidatorSpec(maxAge = Duration.ofSeconds(5)).create()

    validator.add(DependencyInfo(Fixtures.apacheCommonsTextArtifact, Duration.ofSeconds(1)))
    validator.add(DependencyInfo(Fixtures.apacheCommonsCollectionsArtifact, Duration.ofSeconds(3)))

    assertThat(validator.isValid()).isTrue()
  }

  @Test
  fun testInvalid() {
    val validator = CumulativeAgeValidatorSpec(maxAge = Duration.ofSeconds(3)).create()

    validator.add(DependencyInfo(Fixtures.apacheCommonsTextArtifact, Duration.ofSeconds(2)))
    validator.add(DependencyInfo(Fixtures.apacheCommonsCollectionsArtifact, Duration.ofSeconds(3)))

    assertThat(validator.isValid()).isFalse()
  }

  @Test
  fun testViolators() {
    val validator = CumulativeAgeValidatorSpec(maxAge = Duration.ofSeconds(1)).create()

    validator.add(DependencyInfo(Fixtures.apacheCommonsTextArtifact, Duration.ofSeconds(5)))
    validator.add(DependencyInfo(Fixtures.apacheCommonsCollectionsArtifact, Duration.ofSeconds(42)))

    assertThat(validator.violators())
      .isEqualTo(
        listOf(
          DependencyInfo(Fixtures.apacheCommonsCollectionsArtifact, Duration.ofSeconds(42)),
          DependencyInfo(Fixtures.apacheCommonsTextArtifact, Duration.ofSeconds(5)),
        )
      )
  }

  @Test
  fun threshold() {
    val validator = CumulativeAgeValidatorSpec(maxAge = Duration.ofSeconds(1)).create()

    assertThat(validator.threshold()).isEqualTo(Duration.ofSeconds(1))
  }
}
