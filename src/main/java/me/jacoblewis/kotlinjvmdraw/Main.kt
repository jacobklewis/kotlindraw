package me.jacoblewis.kotlinjvmdraw

import me.jacoblewis.kotlinjvmdraw.DrawEngine.asColor
import org.jcodec.api.awt.AWTSequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Rational
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import kotlin.math.*


val SAVE_AS_MP4 = true

val ANIMATION_LENGTH_SECONDS = 4
val FPS = 60
val TOTAL_FRAMES = ANIMATION_LENGTH_SECONDS * FPS

val ANIMATED_VALUES = 0 to 50

fun main() {
    if (TEST_FRAME != null) {
        val imgBytes = renderFrame(TEST_FRAME)
        println("Saving frame: $TEST_FRAME")
        saveImage(imgBytes, folderName = "test_img", fileName = "img")
    } else {
        val out = NIOUtils.writableFileChannel("animation.mp4")
        val encoder = AWTSequenceEncoder(out, Rational.R(30, 1))
        for (frame in 0 until TOTAL_FRAMES) {
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


fun renderFrame(frame: Int): BufferedImage {
    val frameProgress = expoInterpolator(frame / TOTAL_FRAMES.toFloat(), 2)
    val currentAnimatedValue = frameProgress * ANIMATED_VALUES.second + (1 - frameProgress) * ANIMATED_VALUES.first
    return renderFrame(frame, currentAnimatedValue)
}






val TEST_FRAME: Int? = 150









val RED = "f44336".asColor
val PINK = "e91e63".asColor
val PURPLE = "9c27b0".asColor
val BLUE = "2196f3".asColor
val GREEN = "4caf50".asColor
val YELLOW = "ffeb3b".asColor
val ORANGE = "ff9800".asColor




fun renderFrame(frame: Int, currentValue: Float): BufferedImage {
    return DrawEngine.draw(2880, 1440, Color.WHITE) {
        val radius = height / 3.5
        val steps = 0..50
        for (i in steps) {
            val angle = 2 * PI / steps.last * i
            val radiusMod = (abs(cos(angle)).pow(currentValue.toDouble()) + sin(angle)) * radius
            val x = cos(angle) * radiusMod + centerX
            val y = -sin(angle) * radiusMod + centerY + height / 8

            oval(x, y, 10, 10)
        }

        text("i: $currentValue", x = centerX - height / 4, y = height - height / 10, fontSize = height / 12, centered = false)
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