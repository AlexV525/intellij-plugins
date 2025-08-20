package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.DartLspInlayHintsConsumer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.dartlang.analysis.server.protocol.DartLspInlayHint;
import org.dartlang.analysis.server.protocol.DartLspPosition;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates DAS-wrapped LSP inlay hint responses for the consumer.
 */
public class DartLspInlayHintsProcessor extends ResultProcessor {

  private final DartLspInlayHintsConsumer consumer;

  public DartLspInlayHintsProcessor(DartLspInlayHintsConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      JsonObject lspResponse = resultObject.getAsJsonObject("lspResponse");
      if (lspResponse != null) {
        JsonElement inner = lspResponse.get("result");
        List<DartLspInlayHint> hints = new ArrayList<>();
        if (inner instanceof JsonArray) {
          for (JsonElement el : inner.getAsJsonArray()) {
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();

            JsonObject posObj = obj.getAsJsonObject("position");
            DartLspPosition pos = new DartLspPosition(
              posObj.get("line").getAsInt(),
              posObj.get("character").getAsInt()
            );

            String label;
            JsonElement labelEl = obj.get("label");
            if (labelEl == null) continue;
            if (labelEl.isJsonPrimitive()) label = labelEl.getAsString();
            else if (labelEl.isJsonArray()) {
              StringBuilder sb = new StringBuilder();
              for (JsonElement part : labelEl.getAsJsonArray()) {
                if (part.isJsonObject() && part.getAsJsonObject().has("value")) {
                  sb.append(part.getAsJsonObject().get("value").getAsString());
                }
              }
              label = sb.toString();
            } else continue;

            String kind = obj.has("kind") ? obj.get("kind").getAsString() : null;
            boolean paddingLeft = obj.has("paddingLeft") && obj.get("paddingLeft").getAsBoolean();
            boolean paddingRight = obj.has("paddingRight") && obj.get("paddingRight").getAsBoolean();

            hints.add(new DartLspInlayHint(pos, label, kind, paddingLeft, paddingRight));
          }
        }
        consumer.computedInlayHints(hints);
      }
    }
    if (requestError != null) consumer.onError(requestError);
  }
}