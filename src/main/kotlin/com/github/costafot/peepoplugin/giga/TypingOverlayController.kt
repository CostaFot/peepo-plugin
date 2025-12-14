package com.github.costafot.peepoplugin.giga

import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.DocumentEvent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TypingOverlayController(private val editor: com.intellij.openapi.editor.Editor) : DocumentListener {

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    // Timer is set to null when no activity is scheduled
    @Volatile private var hideTimer: java.util.concurrent.ScheduledFuture<*>? = null

    override fun documentChanged(event: DocumentEvent) {
        // 1. Show the GIF overlay immediately upon typing
        ImageOverlayInjector.showOverlay(editor)

        // 2. Cancel any pending "hide" task
        hideTimer?.cancel(false)

        // 3. Schedule a new "hide" task to run after 500ms of inactivity
        hideTimer = scheduler.schedule({
            // Run on the UI thread for component manipulation
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                ImageOverlayInjector.removeOverlay(editor)
            }
        }, 500, TimeUnit.MILLISECONDS)
    }

    // Call this when the editor tab is closed to clean up resources
    fun dispose() {
        scheduler.shutdownNow()
    }
}