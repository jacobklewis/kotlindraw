package me.jacoblewis.kotlinjvmdraw

import java.awt.*
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

    private fun convertToBytes(bi: BufferedImage): ByteArray {
        // Convert to bytes
        val baos = ByteArrayOutputStream()
        ImageIO.write(bi, "png", baos)
        baos.flush()
        val bytes = baos.toByteArray()
        baos.close()

        return bytes
    }

    fun draw(width: Number, height: Number, backgroundColor: Color, block: DrawRectBuilder.() -> Unit): ByteArray {
        val bi = BufferedImage(width.toInt(), height.toInt(), BufferedImage.TYPE_INT_ARGB)
        val canvas = initG2D(bi)
        val builder = DrawRectBuilder(0, 0, width.toInt(), height.toInt(), backgroundColor)
        builder.block()
        builder.render(canvas, 0, 0)
        builder.renderElements(canvas, 0, 0)
        return convertToBytes(bi)
    }

    open class DrawRectBuilder(x: Int, y: Int, var width: Int, var height: Int, var color: Color) : Drawable(x, y) {
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

    abstract class Drawable(var x: Int, var y: Int) {
        val elements: MutableList<Drawable> = mutableListOf()
        abstract fun render(canvas: Graphics2D, x: Int, y: Int)
        fun renderElements(canvas: Graphics2D, x: Int, y: Int) {
            elements.forEach {
                it.render(canvas, x + this.x, y + this.y)
                it.renderElements(canvas, x + this.x, y + this.y)
            }
        }

        fun rect(x: Number = 0, y: Number = 0, width: Number = 10, height: Number = 10, color: Color, block: (DrawRectBuilder.() -> Unit)? = null) {
            val b = DrawRectBuilder(x.toInt(), y.toInt(), width.toInt(), height.toInt(), color)
            elements.add(b)
            block?.invoke(b)
        }
        fun oval(x: Number = 0, y: Number = 0, width: Number = 10, height: Number = 10, color: Color, block: (DrawOvalBuilder.() -> Unit)? = null) {
            val b = DrawOvalBuilder(x.toInt(), y.toInt(), width.toInt(), height.toInt(), color)
            elements.add(b)
            block?.invoke(b)
        }
    }

    fun createCompoundTextBadge(labels: List<String>, backgroundColor: List<Color> = listOf(Color(21, 101, 192), Color(90, 200, 200))): ByteArray {

        val font = Font("SansSerif", Font.BOLD, 12)
        val tempBi = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        val tempGra = tempBi.createGraphics()
        tempGra.font = font
        // Params
        val paddingX = 8
        val paddingY = 5
        val textWidths = labels.map { tempGra.fontMetrics.stringWidth(it) + 2 * paddingX }
        val textHeight = tempGra.fontMetrics.height

        // Start Drawing


//        // Background with Shadow
//        textWidths.foldIndexed(0) { i, x, cWidth ->
//            roundedRectWithShadow(gra, Rectangle(x, 0, bi.width - x, bi.height), backgroundColor[i % backgroundColor.size], leadingSquare = i != 0)
//
//            gra.font = font
//            val tX = paddingX + x
//            val y = bi.height - paddingY - 3
//
//            // Label with Shadow
//            labelWithShadow(gra, labels[i], tX, y)
//            return@foldIndexed x + cWidth
//        }
//
//        return convertToBytes(bi)
        TODO()
    }

    private fun initG2D(bi: BufferedImage): Graphics2D {
        val gra = bi.createGraphics()
        gra.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        gra.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        gra.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        return gra
    }

    private fun roundedRectWithShadow(gra: Graphics2D, rect: Rectangle, backgroundColor: java.awt.Color, leadingSquare: Boolean = false) {
        gra.color = backgroundColor.darker()
        gra.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10)
        gra.color = backgroundColor
        gra.fillRoundRect(rect.x, rect.y, rect.width, rect.height - 1, 10, 10)
        if (leadingSquare) {
            gra.color = backgroundColor.darker()
            gra.fillRect(rect.x, rect.y, 10, rect.height)
            gra.color = backgroundColor
            gra.fillRect(rect.x, rect.y, 10, rect.height - 1)
        }
    }

    private fun labelWithShadow(gra: Graphics2D, label: String, x: Int, y: Int, color: java.awt.Color = java.awt.Color.WHITE) {
        gra.color = color.darker()
        gra.drawString(label, x + 0.1f, y + 0.5f)
        gra.color = color
        gra.drawString(label, x, y)
    }
}