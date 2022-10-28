import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JFrame


class VizWindow : JFrame("Advent of Code 2019") {
    companion object {
        val RED = Color(0xf44336)
        val RED_LIGHT = Color(0xff7961)
        val RED_DARK = Color(0xba000d)
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(1366, 768)
        background = Color.WHITE
    }

    fun open() {
        isVisible = true
    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        val g2d = g as? Graphics2D ?: return
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, width, height)
        g2d.color = RED
        g2d.fillRect(100, 100, 200, 200)
    }
}