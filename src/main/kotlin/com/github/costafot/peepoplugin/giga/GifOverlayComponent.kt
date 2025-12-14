package com.github.costafot.peepoplugin.giga

import java.awt.Dimension
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class GifOverlayComponent(gifResourcePath: String) : JPanel() {
    private val label: JLabel

    init {
        isOpaque = false
        layout = null
        val icon = ImageIcon(javaClass.getResource(gifResourcePath))
            ?: throw IllegalArgumentException("GIF not found at \$gifResourcePath")
        label = JLabel(icon)
        add(label)
        val w = icon.iconWidth
        val h = icon.iconHeight
        label.setBounds(0, 0, w, h)
        preferredSize = Dimension(w, h)
        size = preferredSize
    }

    fun width(): Int = preferredSize.width
    fun height(): Int = preferredSize.height
}
