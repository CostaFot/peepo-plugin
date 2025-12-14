package com.github.costafot.peepoplugin.giga

import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

class TypingOverlayProjectListener(project: Project) {

    // Key to store the TypingOverlayController instance on the document
    private val controllerKey = Key<TypingOverlayController>("MY_TYPING_CONTROLLER")

    init {
        // Register this class to listen for file open/close events
        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    val editor = source.getSelectedEditor(file)
                    if (editor is TextEditor) {
                        val document = editor.editor.document
                        
                        // Check if a controller is already attached
                        if (document.getUserData(controllerKey) == null) {
                            val controller = TypingOverlayController(editor.editor)
                            document.addDocumentListener(controller)
                            document.putUserData(controllerKey, controller)
                        }
                    }
                }

                override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                    val document = source.getSelectedEditor(file)?.let { (it as? TextEditor)?.editor?.document }
                    
                    document?.getUserData(controllerKey)?.let { controller ->
                        document.removeDocumentListener(controller)
                        controller.dispose()
                        document.putUserData(controllerKey, null)
                    }
                    
                    // Also ensure the visual overlay is removed
                    source.getSelectedEditor(file)?.let { (it as? TextEditor)?.editor?.let { ImageOverlayInjector.removeOverlay(it) } }
                }
            }
        )
    }
}