package me.jacoblewis.kotlinjvmdraw

import java.awt.Color
import java.io.File
import java.io.FileOutputStream

fun main() {

    val imgBytes = DrawEngine.draw(500, 500, Color.WHITE) {
        rect(50, 50, 200, 200, Color.BLUE)
        rect(100, 44, 200, 200, Color.GREEN)
        rect(60, 80, 200, 200, Color.RED) {
            rect(30, 30, 60, 60, color = Color.BLACK) {
                oval(20, 20, color = Color.WHITE)
            }
        }
    }

    saveImage(imgBytes)
}

private fun saveImage(imgBytes: ByteArray) {
    val img = File("img.png")
    img.createNewFile()
    val imgWriter = FileOutputStream(img)
    imgWriter.write(imgBytes)
    imgWriter.close()
}