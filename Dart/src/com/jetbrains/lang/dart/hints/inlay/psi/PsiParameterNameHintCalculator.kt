package com.jetbrains.lang.dart.hints.inlay.psi

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.hints.inlay.model.DartInlayHint
import com.jetbrains.lang.dart.hints.inlay.model.DartInlayHintKind
import com.jetbrains.lang.dart.ide.info.DartFunctionDescription
import com.jetbrains.lang.dart.ide.info.DartParameterDescription
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.util.DartPsiImplUtil

object PsiParameterNameHintCalculator {

  fun calculate(file: PsiFile, editor: Editor): List<DartInlayHint> {
    val result = mutableListOf<DartInlayHint>()
    PsiTreeUtil.processElements(file) { element ->
      if (element is DartCallExpression) {
        computeForCall(element, result)
      }
      true
    }
    return result
  }

  private fun computeForCall(call: DartCallExpression, out: MutableList<DartInlayHint>) {
    val args = DartPsiImplUtil.getArguments(call) ?: return
    val argList = args.argumentList ?: return
    val positionalArgs = argList.expressionList
    if (positionalArgs.isEmpty()) return

    val description = DartFunctionDescription.tryGetDescription(call) ?: return
    val params = description.parameters

    for ((index, expr) in positionalArgs.withIndex()) {
      if (index >= params.size) break
      val paramName = params[index].name
      if (paramName.isNullOrBlank()) continue

      // De-noise: skip when arg is a reference with the same name as the parameter.
      if (expr is DartReferenceExpression && expr.text == paramName) continue

      val offset = expr.textRange.startOffset
      out += DartInlayHint(label = "$paramName: ", kind = DartInlayHintKind.Parameter, offset = offset)
    }
  }

  // Accessor for parameter name via DartParameterDescription API (fallback to parsing toString if necessary)
  private val DartParameterDescription.name: String?
    get() {
      return try {
        // Parameter description text can be in formats like:
        // - "paramName"
        // - "Type paramName"
        // - "final Type paramName"
        val s = this.toString().trim()
        if (s.isBlank()) return null
        
        // Try to get the last word which should be the parameter name
        val parts = s.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (parts.isEmpty()) return null
        
        // Return the last part, which should be the parameter name
        parts.lastOrNull()?.takeIf { it.isNotBlank() }
      } catch (_: Throwable) { 
        null 
      }
    }
}