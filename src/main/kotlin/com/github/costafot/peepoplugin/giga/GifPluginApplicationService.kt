package com.github.costafot.peepoplugin.giga

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class GifPluginApplicationService(private val project: Project) {

    init {
        println("GifPluginApplicationService initialized for project: ${project.name}")
        TypingOverlayProjectListener(project)
    }
}
