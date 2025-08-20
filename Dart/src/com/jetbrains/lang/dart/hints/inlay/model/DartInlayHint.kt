package com.jetbrains.lang.dart.hints.inlay.model

enum class DartInlayHintKind { Parameter, Type, Other }

data class DartInlayHint(
  val label: String,
  val kind: DartInlayHintKind,
  val offset: Int
)