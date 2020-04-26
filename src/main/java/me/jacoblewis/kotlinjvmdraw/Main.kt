package me.jacoblewis.kotlinjvmdraw

import org.jcodec.api.awt.AWTSequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Rational
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import kotlin.math.*

fun main() {
    val TEST_FRAME: Int? = 50
    val SAVE_AS_MP4 = true

    val animationLengthSeconds = 4
    val animationFPS = 60
    val animationFrames = animationLengthSeconds * animationFPS

    val animatedValueStart = 0
    val animatedValueEnd = 50

    fun renderFrame(frame: Int): BufferedImage {
        val frameProgress = expoInterpolator(frame / animationFrames.toFloat(), 2)
        val currentAnimatedValue = frameProgress * animatedValueEnd + (1 - frameProgress) * animatedValueStart

        return DrawEngine.draw(854, 480, Color.WHITE) {

            val radius = height / 3.5
            val steps = 0..50
            for (i in steps) {
                val angle = 2 * PI / steps.last * i
                val radiusMod = (abs(cos(angle)).pow(currentAnimatedValue.toDouble()) + sin(angle)) * radius
                val x = cos(angle) * radiusMod + centerX
                val y = -sin(angle) * radiusMod + centerY + height / 8

                oval(x, y, 10, 10)
            }

            text("Frame: $frame", centerX, height - height / 10, height / 12)
        }
    }

    if (TEST_FRAME != null) {
        val imgBytes = renderFrame(TEST_FRAME)
        println("Saving frame: $TEST_FRAME")
        saveImage(imgBytes, folderName = "test_img", fileName = "img")
    } else {
        val out = NIOUtils.writableFileChannel("animation.mp4")
        val encoder = AWTSequenceEncoder(out, Rational.R(30, 1))
        for (frame in 0 until animationFrames) {
            val image = renderFrame(frame)
            println("Saving frame: $frame")
            if (SAVE_AS_MP4) {
                encoder.encodeImage(image)
            } else {
                saveImage(image, fileName = "img_$frame")
            }
        }
        encoder.finish()
    }
}

private fun saveImage(image: BufferedImage, folderName: String = "imgs", fileName: String = "img") {
    val folder = File(folderName)
    folder.mkdir()
    val img = File("$folderName/$fileName.png")
    img.createNewFile()
    val imgWriter = FileOutputStream(img)
    imgWriter.write(DrawEngine.convertToBytes(image))
    imgWriter.close()
}

private fun expoInterpolator(input: Number, multiplier: Number): Float {
    return input.toDouble().pow(multiplier.toDouble()).toFloat()
}