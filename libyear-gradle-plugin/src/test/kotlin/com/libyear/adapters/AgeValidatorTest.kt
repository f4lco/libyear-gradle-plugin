package com.libyear.adapters

import com.libyear.validator.AgeValidatorSpec
import com.libyear.validator.DependencyInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

internal class AgeValidatorTest {

  @Test
  fun testValid() {
    val validator = AgeValidatorSpec(maxAge = Duration.ofSeconds(5)).create()

    validator.add(DependencyInfo(Fixtures.apacheCommonsTextArtifact, Duration.ofSeconds(1)))

    assertThat(validator.isValid()).isTrue()
  }

  @Test
  fun testInvalid() {
    val validator = AgeValidatorSpec(maxAge = Duration.ofSeconds(1)).create()

    validator.add(DependencyInfo(Fixtures.apacheCommonsTextArtifact, Duration.ofSeconds(5)))

    assertThat(validator.isValid()).isFalse()
  }

  @Test
  fun testViolators() {
    val validator = AgeValidatorSpec(maxAge = Duration.ofSeconds(1)).create()

    validator.add(DependencyInfo(Fixtures.apacheCommonsTextArtifact, Duration.ofSeconds(5)))

    assertThat(validator.violators())
      .isEqualTo(listOf(DependencyInfo(Fixtures.apacheCommonsTextArtifact, Duration.ofSeconds(5))))
  }

  @Test
  fun threshold() {
    val validator = AgeValidatorSpec(maxAge = Duration.ofSeconds(1)).create()

    assertThat(validator.threshold()).isEqualTo(Duration.ofSeconds(1))
  }
}
