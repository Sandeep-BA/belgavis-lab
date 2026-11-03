// Author: Sandeep Belgavi
package dev.belgavi.adk;

import com.google.adk.models.*;
import com.google.genai.types.*;
import io.reactivex.rxjava3.core.Flowable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Deterministic fixture: exercises ADK orchestration, not model intelligence. */
public final class OfflineModel extends BaseLlm {
  private final String role;

  public OfflineModel(String role) {
    super("offline-fixture");
    this.role = role;
  }

  @Override
  public Flowable<LlmResponse> generateContent(LlmRequest request, boolean stream) {
    var contents = request.contents();
    var last = contents.get(contents.size() - 1);
    var parts = last.parts().orElse(List.of());
    for (var part : parts) {
      if (part.functionResponse().isPresent()) {
        return reply(
            Part.fromText("Synthetic tool result: " + part.functionResponse().get().response()));
      }
    }
    String prompt = parts.stream().map(p -> p.text().orElse("")).reduce("", String::concat);
    var match = Pattern.compile("CASE-[0-9]{4}").matcher(prompt);
    String id = match.find() ? match.group() : "CASE-1042";
    String tool =
        switch (role) {
          case "lookup" -> "lookupCase";
          case "session" -> prompt.toLowerCase().contains("recall") ? "recallCase" : "selectCase";
          default -> "";
        };
    if (!tool.isEmpty()) {
      Map<String, Object> args = tool.equals("recallCase") ? Map.of() : Map.of("caseId", id);
      return reply(
          Part.builder()
              .functionCall(FunctionCall.builder().name(tool).args(args).build())
              .build());
    }
    return reply(
        Part.fromText(
            "Offline " + role + ": " + request.getFirstSystemInstruction().orElse("Ready")));
  }

  private Flowable<LlmResponse> reply(Part part) {
    return Flowable.just(
        LlmResponse.builder()
            .content(Content.builder().role("model").parts(List.of(part)).build())
            .build());
  }

  @Override
  public BaseLlmConnection connect(LlmRequest request) {
    throw new UnsupportedOperationException("Offline fixture has no live connection");
  }
}
