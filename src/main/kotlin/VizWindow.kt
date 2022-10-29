import Utils.getTypeName
import com.dallonf.ktcause.RuntimeValue
import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.roundToInt


class VizWindow : JFrame("Advent of Code 2019") {
    private val drawPanel = DrawPanel()
    var drawables: List<RuntimeValue.RuntimeObject>
        get() = drawPanel.drawables
        set(value) {
            drawPanel.drawables = value
        }

    enum class PaletteColor(val color: Color) {
        WHITE(Color(0xffffff)),
        BLACK(Color(0x000000)),
        RED(Color(0xf44336)),
        RED_LIGHT(Color(0xff7961)),
        RED_DARK(Color(0xba000d)), ;

        companion object {
            fun fromCause(runtimeValue: RuntimeValue): PaletteColor {
                return when (val name = getTypeName(runtimeValue)) {
                    "White" -> WHITE
                    "Black" -> BLACK
                    "Red" -> RED
                    "RedLight" -> RED_LIGHT
                    "RedDark" -> RED_DARK
                    else -> throw IllegalArgumentException("Unexpected color name: $name")
                }
            }
        }
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        add(drawPanel)
        pack()

        font = Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("ShareTechMono-Regular.ttf"))
    }

    class DrawPanel : JPanel() {
        var drawables: List<RuntimeValue.RuntimeObject> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        init {
            preferredSize = Dimension(1366, 768)
            background = PaletteColor.WHITE.color
        }

        override fun paint(g: Graphics?) {
            super.paint(g)
            val g2d = g as? Graphics2D ?: return
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

            for (drawable in drawables) {
                when (getTypeName(drawable)) {
                    "Rectangle" -> paintRectangle(g2d, drawable)
                    "DrawableText" -> paintText(g2d, drawable)
                }
            }
        }

        private fun paintRectangle(g2d: Graphics2D, drawable: RuntimeValue.RuntimeObject) {
            val color = PaletteColor.fromCause(drawable.values[0])
            val x = (drawable.values[1] as RuntimeValue.Number).asDouble()
            val y = (drawable.values[2] as RuntimeValue.Number).asDouble()
            val width = (drawable.values[3] as RuntimeValue.Number).asDouble()
            val height = (drawable.values[4] as RuntimeValue.Number).asDouble()

            g2d.color = color.color
            g2d.fillRect(x.roundToInt(), y.roundToInt(), width.roundToInt(), height.roundToInt())
        }

        private fun paintText(g2d: Graphics2D, drawable: RuntimeValue.RuntimeObject) {
            val values = drawable.values.iterator()
            val text = (values.next() as RuntimeValue.Text).value
            val color = PaletteColor.fromCause(values.next())
            val fontSize = (values.next() as RuntimeValue.Number).asDouble()
            val x = (values.next() as RuntimeValue.Number).asDouble()
            val y = (values.next() as RuntimeValue.Number).asDouble()
            val vAlignment = getTypeName(values.next())
            val hAlignment = getTypeName(values.next())

            val prevFont = font

            g2d.font = font.deriveFont(fontSize.toFloat())
            val textWidth = g2d.fontMetrics.stringWidth(text)
            val textHeight = g2d.fontMetrics.height

            val xAligned: Float = when (hAlignment) {
                "Left" -> x
                "Center" -> x - textWidth / 2
                "Right" -> x - textWidth
                else -> throw AssertionError("bad alignment: $hAlignment")
            }.toFloat()

            val yAligned: Float = when (vAlignment) {
                "Top" -> y + textHeight
                "Center" -> y + textHeight / 2
                "Bottom" -> y
                else -> throw AssertionError("bad alignment: $vAlignment")
            }.toFloat()

            g2d.color = color.color
            g2d.drawString(text, xAligned, yAligned)
            g2d.font = prevFont
        }
    }

    fun open() {
        isVisible = true
        size = Dimension(1366 + insets.left + insets.right, 768 + insets.top + insets.bottom)
    }


}