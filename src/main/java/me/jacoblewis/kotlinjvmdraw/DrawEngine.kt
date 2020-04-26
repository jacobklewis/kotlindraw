package me.jacoblewis.kotlinjvmdraw

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object DrawEngine {

    private fun convertHexToColor(hex: String): Color {
        val strippedHex = hex.removePrefix("#").trim()
        if (strippedHex.length < 6) {
            return java.awt.Color(0, 0, 0)
        }
        val r = strippedHex.substring(0, 2).toInt(16)
        val g = strippedHex.substring(2, 4).toInt(16)
        val b = strippedHex.substring(4, 6).toInt(16)
        return java.awt.Color(r, g, b)
    }

    fun convertToBytes(bi: BufferedImage): ByteArray {
        // Convert to bytes
        val baos = ByteArrayOutputStream()
        ImageIO.write(bi, "png", baos)
        baos.flush()
        val bytes = baos.toByteArray()
        baos.close()

        return bytes
    }

    fun draw(width: Number, height: Number, backgroundColor: Color, block: DrawRectBuilder.() -> Unit): BufferedImage {
        val bi = BufferedImage(width.toInt(), height.toInt(), BufferedImage.TYPE_INT_ARGB)
        val canvas = initG2D(bi)
        val builder = DrawRectBuilder(0, 0, width.toInt(), height.toInt(), backgroundColor)
        builder.block()
        builder.render(canvas, 0, 0)
        builder.renderElements(canvas, 0, 0)
        return bi
    }

    open class DrawRectBuilder(x: Int, y: Int, var width: Int, var height: Int, var color: Color) : Drawable(x, y) {
        val centerX: Int
            get() = width / 2
        val centerY: Int
            get() = height / 2

        override fun render(canvas: Graphics2D, x: Int, y: Int) {
            canvas.color = color
            canvas.fillRect(x + this.x, y + this.y, width, height)
        }
    }

    open class DrawOvalBuilder(x: Int, y: Int, var width: Int, var height: Int, var color: Color) : Drawable(x, y) {
        override fun render(canvas: Graphics2D, x: Int, y: Int) {
            canvas.color = color
            canvas.fillOval(x + this.x, y + this.y, width, height)
        }
    }

    open class DrawTextBuilder(var text: String, x: Int, y: Int, var fontSize: Int = 12, var fontName: String, var fontStyle: Int, var color: Color, var centered: Boolean) : Drawable(x, y) {
        override fun render(canvas: Graphics2D, x: Int, y: Int) {
            val font = Font(fontName, fontStyle, fontSize)
            canvas.color = color
            canvas.font = font
            if (centered) {
                val offsetX = canvas.fontMetrics.stringWidth(text) / 2
                canvas.drawString(text, x + this.x - offsetX, y + this.y - fontSize / 2)
            } else {
                canvas.drawString(text, x + this.x, y + this.y)
            }
        }
    }

    abstract class Drawable(var x: Int, var y: Int) {
        val elements: MutableList<Drawable> = mutableListOf()
        abstract fun render(canvas: Graphics2D, x: Int, y: Int)
        fun renderElements(canvas: Graphics2D, x: Int, y: Int) {
            elements.forEach {
                it.render(canvas, x + this.x, y + this.y)
                it.renderElements(canvas, x + this.x, y + this.y)
            }
        }

        fun rect(x: Number = 0, y: Number = 0, width: Number = 100, height: Number = 100, centered: Boolean = false, color: Color = Color.BLACK, block: (DrawRectBuilder.() -> Unit)? = null) {
            val b = DrawRectBuilder(x.toInt() - if (centered) (width.toFloat() / 2).toInt() else 0, y.toInt() - if (centered) (height.toFloat() / 2).toInt() else 0, width.toInt(), height.toInt(), color)
            elements.add(b)
            block?.invoke(b)
        }

        fun oval(x: Number = 0, y: Number = 0, width: Number = 100, height: Number = 100, centered: Boolean = true, color: Color = Color.BLACK, block: (DrawOvalBuilder.() -> Unit)? = null) {
            val b = DrawOvalBuilder(x.toInt() - if (centered) (width.toFloat() / 2).toInt() else 0, y.toInt() - if (centered) (height.toFloat() / 2).toInt() else 0, width.toInt(), height.toInt(), color)
            elements.add(b)
            block?.invoke(b)
        }

        fun text(text: String, x: Number = 0, y: Number = 0, fontSize: Int = 12, color: Color = Color.BLACK, fontName: String = "SansSerif", fontStyle: Int = Font.BOLD, centered: Boolean = true, block: (DrawTextBuilder.() -> Unit)? = null) {
            val b = DrawTextBuilder(text, x.toInt(), y.toInt(), fontSize, fontName, fontStyle, color, centered)
            elements.add(b)
            block?.invoke(b)
        }
    }

    private fun initG2D(bi: BufferedImage): Graphics2D {
        val gra = bi.createGraphics()
        gra.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        gra.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        gra.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        return gra
    }

}