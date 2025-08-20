package com.google.dart.server;

import org.dartlang.analysis.server.protocol.DartLspInlayHint;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

public interface DartLspInlayHintsConsumer extends Consumer {
  void computedInlayHints(List<DartLspInlayHint> hints);
  void onError(RequestError requestError);
}