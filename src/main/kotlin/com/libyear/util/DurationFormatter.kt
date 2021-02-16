package com.libyear

import java.text.NumberFormat
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Locale

private val SUPPORTED_UNITS = listOf(
  ChronoUnit.DECADES,
  ChronoUnit.YEARS,
  ChronoUnit.MONTHS,
  ChronoUnit.DAYS,
  ChronoUnit.HOURS,
  ChronoUnit.MINUTES,
  ChronoUnit.SECONDS,
)

fun Duration.formatApproximate(): String {
  assert(this <= ChronoUnit.DECADES.duration)

  for (unit in SUPPORTED_UNITS) {
    val amount: Double = this.toHours().toDouble() / unit.duration.toHours()
    if (amount >= 1L) {
      return format(amount, unit)
    }
  }

  return format(1.0, ChronoUnit.SECONDS)
}

private fun format(amount: Double, unit: ChronoUnit): String {
  val unitName: String = when (amount) {
    1.0 -> unit.name.trimEnd('S')
    else -> unit.name
  }.toLowerCase()

  val format = NumberFormat.getInstance(Locale.ENGLISH)
  format.maximumFractionDigits = 1
  return "${format.format(amount)} $unitName"
}
