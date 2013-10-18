package co.uk.noor

case class Tolerance(val red: Int,
                     val green: Int,
                     val blue: Int,
                     val minBrightness: Int,
                     val maxBrightness: Int,
                     val ignoreAntialiasing: Boolean,
                     val ignoreColors: Boolean) {
}

object Tolerance {
  val ignoreNothing = Tolerance(16, 16, 16, 16, 240, false, false)
  val ignoreAntialiasing = Tolerance(32, 32, 32, 64, 96, true, false)
  val ignoreColors = Tolerance(-1, -1, -1, 16, 240, false, true)
}
