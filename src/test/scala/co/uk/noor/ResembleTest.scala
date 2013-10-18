package co.uk.noor

import org.scalatest.{FunSpec, BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import javax.imageio.ImageIO
import java.io.File
import java.awt.image.BufferedImage
import scala.collection.mutable.MutableList

class ResembleTest extends FunSpec with ShouldMatchers with BeforeAndAfterEach {

  val resemble = new Resemble("./src/test/resources/TestA.jpg", "./src/test/resources/TestB.jpg")

  it("should do compare ignoring nothing") {
    val actual = resemble.compare(Tolerance.ignoreNothing)

    val expected =  ImageIO.read(new File("./src/test/resources/ignoring-nothing-expected.png"))

    actual.misMatchPercentage should be(8.0108)
    actual.isSameDimensions should be(true)
    actual.dimensionDifference should be((0,0))

    diff(expected, actual.diff).size should be(0)
  }

  it("should do compare ignoring colors") {
    val actual = resemble.compare(Tolerance.ignoreColors)

    val expected =  ImageIO.read(new File("./src/test/resources/ignoring-colours-expected.png"))

    actual.misMatchPercentage should be(0.3568)
    actual.isSameDimensions should be(true)
    actual.dimensionDifference should be((0,0))

    diff(expected, actual.diff).size should be(0)
  }

  it("should do compare ignoring antialiasing") {
    val actual = resemble.compare(Tolerance.ignoreAntialiasing)

    val expected =  ImageIO.read(new File("./src/test/resources/ignoring-antialiasing-expected.png"))

    actual.misMatchPercentage should be(5.4356)
    actual.isSameDimensions should be(true)
    actual.dimensionDifference should be((0,0))

    diff(expected, actual.diff).size should be(0)
  }

  private def diff(a: BufferedImage, b: BufferedImage): List[(Int, Int)] = {
    var difference = MutableList[(Int, Int)]()
    for (y <- 0 until a.getHeight(); x <- 0 until a.getWidth()) {
      val pixA  = a.getRGB(x, y)
      val pixB  = b.getRGB(x, y)

      if (!pixA.equals(pixB)) {
        difference += ((x, y))
        debug("x: %s, y: %s".format(x, y))
      }
    }
    difference.toList
  }

  var count = 0

  private def debug(s: String) {
    if (count > 0) {
      count -= 1;
      println(s)
    }
  }
}
