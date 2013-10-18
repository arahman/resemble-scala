package co.uk.noor

import javax.imageio.ImageIO
import java.io.File
import java.awt.image.BufferedImage
import java.awt.{Dimension, Color}
import java.util.Date

class Resemble(imageA: String, imageB: String) {
  val biA = imageA.load()
  val biB = imageB.load()

  def compare(tolerance: Tolerance): ResembleResult = {
    val maxWidth = Math.max(biA.getWidth(), biB.getWidth)
    val maxHeight = Math.max(biA.getHeight(), biB.getHeight)

    implicit val maxDimension = new Dimension(maxWidth, maxHeight)
    implicit val implicitTolerance = tolerance

    biA.normalise().compareTo(biB.normalise())
  }

  implicit class RichString(fileName: String) {
    def load(): BufferedImage = {
      ImageIO.read(new File(fileName))
    }
  }

  implicit class RichImage(image: BufferedImage) {
    def normalise()(implicit dimension: Dimension): BufferedImage = {
      val normalised = new BufferedImage(dimension.getWidth.toInt, dimension.getHeight.toInt, BufferedImage.TYPE_INT_ARGB)
      normalised.createGraphics().drawImage(image, 0, 0, null)
      normalised
    }

    def compareTo(other: BufferedImage)(implicit dimension: Dimension, tolerance: Tolerance): ResembleResult = {
      Resemble.analyse(image, other)
    }
  }

}
object Resemble {
  private def analyse(imageA: BufferedImage, imageB: BufferedImage)
                     (implicit dimension: Dimension, tolerance: Tolerance): ResembleResult = {
    val time = new Date()
    val width = dimension.getWidth.toInt
    val height = dimension.getHeight.toInt

    val diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    var mismatchCount: Double = 0
    var skip = 0

    if ((width > 1200 || height > 1200) && tolerance.ignoreAntialiasing) {
      skip = 6
    }

    for (y <- 0 until height; x <- 0 until width) {
      if (skip != 0 && ((y % skip) == 0 || (x % skip) == 0)) {
        // only skip if the image isn't small
      } else {
        val pixel1 = getPixelInfo(imageA, x, y)
        val pixel2 = getPixelInfo(imageB, x, y)

        if (tolerance.ignoreColors) {
          if (pixel1.isBrightnessSimilar(pixel2)) {
            pixel2.copyGrayScalePixel(diff, x, y)
          } else {
            errorPixel(diff, x, y)
            mismatchCount += 1
          }
        } else {
          if (pixel1.isRGBSimilar(pixel2)) {
            pixel2.copyPixel(diff, x, y)
          } else if (tolerance.ignoreAntialiasing && (isAntialiased(imageA, x, y) || isAntialiased(imageB, x, y))) {
            if (pixel1.isBrightnessSimilar(pixel2)) {
              pixel2.copyGrayScalePixel(diff, x, y)
            } else {
              errorPixel(diff, x, y)
              mismatchCount += 1
            }
          } else {
            errorPixel(diff, x, y)
            mismatchCount += 1
          }
        }
      }
    }
    val result = new ResembleResult()
    result.isSameDimensions = imageA.getWidth() == imageB.getWidth() && imageA.getHeight() == imageB.getHeight
    result.dimensionDifference = (imageA.getWidth() - imageB.getWidth(), imageA.getHeight() - imageB.getHeight)
    result.misMatchPercentage = (mismatchCount / (dimension.getHeight * dimension.getWidth) * 100)
    result.analysisTime = new Date().getTime - time.getTime

    result.diff = diff
    
    result
  }

  private def errorPixel(target: BufferedImage, x: Int, y: Int) {
    target.setRGB(x, y, new Color(255, 0, 255).getRGB)
  }

  private def getOptionPixelInfo(image: BufferedImage, x: Int, y: Int): Option[PixelInfo] = {
    try {
      Some(PixelInfo(image.getRGB(x, y)))
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        None
    }
  }

  private def getPixelInfo(image: BufferedImage, x: Int, y: Int): PixelInfo = {
    PixelInfo(image.getRGB(x, y))
  }

  private def isAntialiased(image: BufferedImage, x: Int, y: Int)(implicit tolerance: Tolerance): Boolean = {
    val distance = 1
    var hasHighContrastSibling = 0
    var hasSiblingWithDifferentHue = 0
    var hasEquivalentSibling = 0

    val sourcePix = getPixelInfo(image, x, y)
    for (i <- (distance * -1) to distance; j <- (distance * -1) to distance) {
      if (!(i == 0 && j == 0)) { // ignore source pixel
        val optionTargetPix = getOptionPixelInfo(image, x + i, y + j)

        if (optionTargetPix.isDefined) {
          val targetPix = optionTargetPix.get

          if (sourcePix.isContrasting(targetPix)) {
            hasHighContrastSibling += 1
          }

          if (sourcePix.isRGBSame(targetPix)) {
            hasEquivalentSibling += 1
          }
          if (Math.abs(targetPix.hue - sourcePix.hue) > 0.3) {
            hasSiblingWithDifferentHue += 1
          }

          if (hasSiblingWithDifferentHue > 1 || hasHighContrastSibling > 1) {
            return true
          }
        }
      }

    }

    if (hasEquivalentSibling < 2) {
      return true
    }

    return false
  }
}

