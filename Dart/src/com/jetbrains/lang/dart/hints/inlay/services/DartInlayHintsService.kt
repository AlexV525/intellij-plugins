package com.jetbrains.lang.dart.hints.inlay.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService
import com.jetbrains.lang.dart.hints.inlay.model.DartInlayHint
import com.jetbrains.lang.dart.hints.inlay.model.DartInlayHintKind
import com.jetbrains.lang.dart.hints.inlay.psi.PsiParameterNameHintCalculator
import org.dartlang.analysis.server.protocol.DartLspPosition
import org.dartlang.analysis.server.protocol.DartLspRange

@Service(Service.Level.PROJECT)
class DartInlayHintsService(private val project: Project) {

  companion object {
    fun getInstance(project: Project): DartInlayHintsService = project.service()
  }

  fun getParameterNameHints(file: PsiFile, editor: Editor): List<DartInlayHint> {
    val vFile = file.virtualFile ?: return emptyList()

    val server = DartAnalysisServerService.getInstance(project)

    // Try server-backed inlay hints via LSP-over-DAS. Fallback to PSI if unavailable.
    val fileUri = server.execution_mapUri(vFile.path, null)
    if (fileUri != null) {
      val range = DartLspRange(DartLspPosition(0, 0), DartLspPosition(editor.document.lineCount, 0))
      val serverHints = server.lspMessage_textDocument_inlayHint(fileUri, range)
      if (!serverHints.isNullOrEmpty()) {
        return serverHints
          .filter { it.kind == "parameter" }
          .map { hint ->
            // Convert to inline offset as early as possible; if conversion fails, skip.
            val offset = com.jetbrains.lang.dart.analyzer.getOffsetInDocument(editor.document, hint.position)
            if (offset != null) DartInlayHint(hint.label, DartInlayHintKind.Parameter, offset = offset) else null
          }
          .filterNotNull()
      }
    }

    // PSI fallback
    return PsiParameterNameHintCalculator.calculate(file, editor)
  }
}