package com.libyear.validator

import com.libyear.util.formatApproximate
import org.gradle.api.logging.Logger

class LoggingValidator(
  private val logger: Logger,
  private val wrapped: DependencyValidator
) : DependencyValidator by wrapped {

  override fun add(dep: DependencyInfo) {
    logger.debug("Adding {} of age {}", dep.version, dep.age.formatApproximate())
    wrapped.add(dep)
  }

  override fun isValid(): Boolean {
    val valid = wrapped.isValid()
    logger.debug("Validator [{}] valid: {}", wrapped, valid)
    return valid
  }
}
