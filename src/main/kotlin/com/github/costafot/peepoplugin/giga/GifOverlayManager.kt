package com.github.costafot.peepoplugin.giga

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.Disposer
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.SwingUtilities
import javax.swing.Timer
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.diagnostic.Logger

class GifOverlayManager(
    private val gifResourcePath: String = "/gifs/giga-chad-chatting.gif",
    private val hideDelayMillis: Int = 1500
) {
    init {
        println("GifOverlayManager initialized")
        // log stuff
        val listener = object : EditorFactoryListener {
            override fun editorCreated(event: EditorFactoryEvent) {
                val editor = event.editor
                attachOverlayToEditor(editor)

                Logger.getInstance(GifOverlayManager::class.java)
                    .info("GifOverlay attached to new editor: ${editor.javaClass.name}")
            }

            override fun editorReleased(event: EditorFactoryEvent) {
                // handled per-editor when attaching: listeners and overlay removed there
            }
        }
        // register for the app lifetime
        EditorFactory.getInstance().addEditorFactoryListener(listener, ApplicationManager.getApplication())
    }

    private fun attachOverlayToEditor(editor: Editor) {
        val content = editor.contentComponent
        // create overlay
        val overlay = GifOverlayComponent(gifResourcePath)
        overlay.isVisible = false

        // timer to hide overlay after inactivity
        val hideTimer = Timer(hideDelayMillis) {
            SwingUtilities.invokeLater { overlay.isVisible = false }
        }.also { it.isRepeats = false }

        // document listener: show overlay and restart timer on change
        val docListener = object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                SwingUtilities.invokeLater {
                    positionOverlay(editor, overlay)
                    overlay.isVisible = true
                    hideTimer.restart()
                }
            }
        }
        editor.document.addDocumentListener(docListener)

        // reposition overlay when editor component resizes
        val compListener = object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                SwingUtilities.invokeLater { positionOverlay(editor, overlay) }
            }
        }
        content.addComponentListener(compListener)

        // add overlay to editor UI on the EDT
        SwingUtilities.invokeLater {
            // add as child and set absolute bounds; positioning handled in positionOverlay
            content.add(overlay)
            content.setComponentZOrder(overlay, 0) // bring to front
            positionOverlay(editor, overlay)
            content.revalidate()
            content.repaint()
        }

        // cleanup when editor is released
        val disposable = com.intellij.openapi.util.Disposer.newDisposable()
        // We rely on EditorFactoryListener.editorReleased to remove overlay:
        // Register a small watcher by using editor component's client property to store references
        // so editorReleased can find them. Simpler: attach a property to the editor instance.
        // Store as user data could be used but to keep code self-contained, use a weak map is omitted.
        // Instead, register a disposal on editor if it's Disposable (Editor implements Disposable).
        try {
            Disposer.register(disposable) {
                hideTimer.stop()
                try {
                    editor.document.removeDocumentListener(docListener)
                } catch (_: Exception) {}
                try {
                    content.removeComponentListener(compListener)
                } catch (_: Exception) {}
                SwingUtilities.invokeLater {
                    content.remove(overlay)
                    content.revalidate()
                    content.repaint()
                }
            }
        } catch (_: Throwable) {
            // ignore if disposal registration fails
        }
    }

    private fun positionOverlay(editor: Editor, overlay: GifOverlayComponent) {
        val parent = editor.contentComponent
        if (parent.width <= 0 || parent.height <= 0) return
        val x = parent.width - overlay.width() - 10
        val y = parent.height - overlay.height() - 10
        overlay.setBounds(x.coerceAtLeast(0), y.coerceAtLeast(0), overlay.width(), overlay.height())
    }
}
