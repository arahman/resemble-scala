package co.uk.noor

import java.awt.image.BufferedImage

class ResembleResult {

  var isSameDimensions: Boolean = _
  var dimensionDifference: (Int, Int) = _

  var misMatchPercentage: Double = _
  var analysisTime: Long = _;

  var diff: BufferedImage = _

}
