package com.jetbrains.lang.dart.hints.inlay

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiFile
import com.jetbrains.lang.dart.DartLanguage
import com.jetbrains.lang.dart.hints.inlay.services.DartInlayHintsService

class DartParameterNameInlayHintsProvider : InlayHintsProvider {
  companion object {
    const val PROVIDER_ID: String = "dart.inlay.parameter.names"
  }

  override fun isLanguageSupported(language: com.intellij.lang.Language): Boolean =
    language.isKindOf(DartLanguage.INSTANCE)

  override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector = object : OwnBypassCollector {
    override fun collectHintsForFile(file: PsiFile, sink: InlayTreeSink) {
      if (DumbService.isDumb(file.project)) return
      val virtualFile = file.virtualFile ?: return

      val service = DartInlayHintsService.getInstance(file.project)
      val hints = service.getParameterNameHints(file, editor)

      val doc = editor.document
      for (hint in hints) {
        val offset = hint.offset ?: continue
        if (offset < 0 || offset > doc.textLength) continue

        val pos = InlineInlayPosition(offset, relatedToPrevious = false)
        sink.addPresentation(
          position = pos,
          hintFormat = HintFormat.default
        ) {
          text(hint.label)
        }
      }
    }
  }
}