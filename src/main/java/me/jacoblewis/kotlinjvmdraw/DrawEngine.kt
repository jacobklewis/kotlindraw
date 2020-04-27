package me.jacoblewis.kotlinjvmdraw

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object DrawEngine {

    val String.asColor: Color
        get() {
            val strippedHex = this.removePrefix("#").filter { it.isDigit() || listOf('a', 'b', 'c', 'd', 'e', 'f').contains(it.toLowerCase()) }.trim()
            if (strippedHex.length != 6) {
                return Color(0, 0, 0)
            }
            val r = strippedHex.substring(0, 2).toInt(16)
            val g = strippedHex.substring(2, 4).toInt(16)
            val b = strippedHex.substring(4, 6).toInt(16)
            return Color(r, g, b)
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

    fun draw(width: Number,
             height: Number,
             backgroundColor: Color,
             block: DrawRectBuilder.() -> Unit): BufferedImage {
        val bi = BufferedImage(width.toInt(), height.toInt(), BufferedImage.TYPE_INT_ARGB)
        val canvas = initG2D(bi)
        val builder = DrawRectBuilder(0, 0, width.toInt(), height.toInt(), backgroundColor, false)
        builder.block()
        builder.render(canvas, 0, 0)
        builder.renderElements(canvas, 0, 0)
        return bi
    }

    open class DrawRectBuilder(x: Int, y: Int, var width: Int, var height: Int, var color: Color, centered: Boolean) : Drawable(x, y, centered) {
        val centerX: Int
            get() = width / 2
        val centerY: Int
            get() = height / 2

        override fun measuredWidth(canvas: Graphics2D): Float = width.toFloat()
        override fun measuredHeight(canvas: Graphics2D): Float = height.toFloat()

        override fun render(canvas: Graphics2D, x: Int, y: Int) {
            canvas.color = color
            canvas.fillRect(x + this.x, y + this.y, width, height)
        }
    }

    open class DrawOvalBuilder(x: Int, y: Int, var width: Int, var height: Int, var color: Color, centered: Boolean) : Drawable(x, y, centered) {
        val centerX: Int
            get() = width / 2
        val centerY: Int
            get() = height / 2

        override fun measuredWidth(canvas: Graphics2D): Float = width.toFloat()
        override fun measuredHeight(canvas: Graphics2D): Float = height.toFloat()

        override fun render(canvas: Graphics2D, x: Int, y: Int) {
            canvas.color = color
            canvas.fillOval(x + this.x, y + this.y, width, height)
        }
    }

    open class DrawTextBuilder(var text: String, x: Int, y: Int, var fontSize: Int = 12, var fontName: String, var fontStyle: Int, var color: Color, centered: Boolean) : Drawable(x, y, centered) {

        override fun measuredWidth(canvas: Graphics2D): Float {
            val font = Font(fontName, fontStyle, fontSize)
            canvas.font = font
            return canvas.fontMetrics.stringWidth(text).toFloat()
        }

        override fun measuredHeight(canvas: Graphics2D): Float = fontSize / 2f

        override fun render(canvas: Graphics2D, x: Int, y: Int) {
            val font = Font(fontName, fontStyle, fontSize)
            canvas.color = color
            canvas.font = font
            canvas.drawString(text, x + this.x, y + this.y)
        }
    }

    abstract class Drawable(var x: Int, var y: Int, var centered: Boolean) {
        val elements: MutableList<Drawable> = mutableListOf()
        protected abstract fun measuredWidth(canvas: Graphics2D): Float
        protected abstract fun measuredHeight(canvas: Graphics2D): Float
        abstract fun render(canvas: Graphics2D, x: Int, y: Int)
        fun renderElements(canvas: Graphics2D, x: Int, y: Int) {
            elements.forEach {
                val offsetX = if (it.centered) (it.measuredWidth(canvas) / 2).toInt() else 0
                val offsetY = if (it.centered) (it.measuredHeight(canvas) / 2).toInt() else 0
                it.render(canvas, x + this.x - offsetX, y + this.y - offsetY)
                it.renderElements(canvas, x + this.x - offsetX, y + this.y - offsetY)
            }
        }

        fun rect(x: Number = 0, y: Number = 0, width: Number = 100, height: Number = 100, centered: Boolean = false, color: Color = Color.BLACK, block: (DrawRectBuilder.() -> Unit)? = null) {
            val b = DrawRectBuilder(x.toInt(), y.toInt(), width.toInt(), height.toInt(), color, centered)
            elements.add(b)
            block?.invoke(b)
        }

        fun oval(x: Number = 0, y: Number = 0, width: Number = 100, height: Number = 100, centered: Boolean = true, color: Color = Color.BLACK, block: (DrawOvalBuilder.() -> Unit)? = null) {
            val b = DrawOvalBuilder(x.toInt(), y.toInt(), width.toInt(), height.toInt(), color, centered)
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