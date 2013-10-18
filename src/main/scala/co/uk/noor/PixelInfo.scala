package co.uk.noor

import java.awt.Color
import java.awt.image.BufferedImage

class PixelInfo(colour: Color) {

  val red = colour.getRed
  val green = colour.getGreen
  val blue = colour.getBlue

  val hue = Color.RGBtoHSB(red, green, blue, null)(0)
  val brightness = 0.3 * red + 0.59 * green + 0.11 * blue

  def isBrightnessSimilar(other: PixelInfo)(implicit tolerance: Tolerance): Boolean = {
    Math.abs(this.brightness - other.brightness) < tolerance.minBrightness
  }

  def isRGBSimilar(other: PixelInfo)(implicit tolerance: Tolerance): Boolean = {
    def isColorSimilar(a: Int, b: Int, colour: Int): Boolean = {
      val absDiff = Math.abs(a - b)

      if (a == b || absDiff < colour) {
        return true
      } else {
        return false
      }
    }

    val red = isColorSimilar(this.red, other.red, tolerance.red)
    val green = isColorSimilar(this.green, other.green, tolerance.green)
    val blue = isColorSimilar(this.blue, other.blue, tolerance.blue)

    red && green && blue
  }

  def isContrasting(other: PixelInfo)(implicit tolerance: Tolerance) = {
    Math.abs(this.brightness - other.brightness) > tolerance.maxBrightness
  }

  def isRGBSame(other: PixelInfo) = {
    val red = this.red == other.red
    val green = this.green == other.green
    val blue = this.blue == other.blue
    red && green && blue
  }

  def copyGrayScalePixel(target: BufferedImage, x: Int, y: Int) {
    val grey = new Color(brightness.toInt, brightness.toInt, brightness.toInt)
    target.setRGB(x, y, grey.getRGB)
  }

  def copyPixel(target: BufferedImage, x: Int, y: Int) {
    target.setRGB(x, y, colour.getRGB)
  }

}

object PixelInfo {
  def apply(colour: Int): PixelInfo = {
    new PixelInfo(new Color(colour))
  }
}
