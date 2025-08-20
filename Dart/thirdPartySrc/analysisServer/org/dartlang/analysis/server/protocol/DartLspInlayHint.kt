package org.dartlang.analysis.server.protocol

class DartLspInlayHint(
  val position: DartLspPosition,
  val label: String,
  val kind: String?,
  val paddingLeft: Boolean = false,
  val paddingRight: Boolean = false
)