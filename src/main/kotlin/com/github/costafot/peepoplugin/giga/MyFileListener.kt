package com.github.costafot.peepoplugin.giga

import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project

class MyFileListener(private val project: Project) : FileEditorManagerListener {

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        println("File opened in editor: ${file.path}")
        // Your logic here: e.g., inspect the file, add markers
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        println("File closed in editor: ${file.path}")
        // Your logic here: e.g., save state, clean up resources
    }
}