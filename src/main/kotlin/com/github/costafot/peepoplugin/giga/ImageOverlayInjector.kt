package com.github.costafot.peepoplugin.giga

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.BorderLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JLayeredPane // Use JLayeredPane for proper overlay
import javax.swing.SwingConstants
import com.intellij.openapi.editor.impl.EditorComponentImpl
import javax.swing.SwingUtilities

object ImageOverlayInjector {

    private val componentKey = com.intellij.openapi.util.Key<JPanel>("MY_PLUGIN_OVERLAY_GIF_COMPONENT")
    private val GIF_PATH = "/gifs/giga-chad-chatting.gif"

    private fun createAndInjectOverlay(editor: Editor): JPanel {
        // 1. Find the parent component to host the overlay
        val editorComponent = editor.contentComponent.parent.parent // Go up the hierarchy to find the container component

        // Check if the container is a JLayeredPane, which is common for editors
        val layeredPane = (editorComponent as? JLayeredPane) ?: run {
            // Fallback: This is a simplification. In a real plugin, you'd navigate the hierarchy carefully.
            // For standard TextEditor, the component hierarchy is usually: EditorComponentImpl -> JPanel -> JLayeredPane
            // Let's assume the editor's content component is the right starting point for traversal.
            // A better approach is often injecting a custom component via extension points,
            // but for a simple overlay, this direct component manipulation is often used.
            val rootPane = SwingUtilities.getRootPane(editor.contentComponent)

            return@run rootPane?.layeredPane ?: throw IllegalStateException("Cannot find JLayeredPane for editor.")
        }

        // 2. Create the GIF Label
        val gifIcon = ImageIcon(javaClass.getResource(GIF_PATH))
        val gifLabel = JLabel(gifIcon)
        gifLabel.preferredSize = Dimension(gifIcon.iconWidth, gifIcon.iconHeight)

        // 3. Create the container Panel (This panel will hold the GIF and handle positioning)
        val overlayPanel = JPanel(BorderLayout()) // Use BorderLayout for easy anchoring
        overlayPanel.isOpaque = false // Key for transparency
        overlayPanel.add(gifLabel, BorderLayout.SOUTH) // Place the GIF at the bottom

        // Use a wrapping panel for bottom-right alignment *within* the overlayPanel's layout area
        val wrapper = JPanel(FlowLayout(FlowLayout.RIGHT, 15, 15)) // FlowLayout with padding
        wrapper.isOpaque = false
        wrapper.add(gifLabel)

        overlayPanel.add(wrapper, BorderLayout.SOUTH)
        overlayPanel.isVisible = false // Start hidden

        // 4. Inject the overlay into the JLayeredPane
        // Set its bounds to cover the entire editor area
        overlayPanel.bounds = layeredPane.bounds
        layeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER) // Use a high layer to ensure it's on top

        // 5. Store and return
        editor.putUserData(componentKey, overlayPanel)
        return overlayPanel
    }

    // --- Visibility Control (showOverlay, hideOverlay, removeOverlay remain the same) ---

    fun showOverlay(editor: Editor) {
        val panel = editor.getUserData(componentKey) ?: createAndInjectOverlay(editor)
        if (!panel.isVisible) {
            panel.isVisible = true
            panel.revalidate()
            panel.repaint()
        }
    }

    fun hideOverlay(editor: Editor) {
        editor.getUserData(componentKey)?.let { panel ->
            if (panel.isVisible) {
                panel.isVisible = false
                panel.revalidate()
                panel.repaint()
            }
        }
    }

    fun removeOverlay(editor: Editor) {
        editor.getUserData(componentKey)?.let { panel ->
            // Find the layered pane again to remove the component
            val editorComponent = editor.contentComponent.parent.parent
            val rootPane = SwingUtilities.getRootPane(editor.contentComponent)
            val layeredPane = (editorComponent as? JLayeredPane) ?: rootPane?.layeredPane

            layeredPane?.remove(panel)
            editor.putUserData(componentKey, null)
        }
    }
}