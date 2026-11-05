// Author: Sandeep Belgavi
package dev.belgavi.adk;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.ToolContext;
import java.util.Map;

/** Synthetic data only. Tool results are data, never agent instructions. */
public final class CaseTools {
  @Schema(description = "Read a synthetic help-desk case by its CASE-NNNN identifier.")
  public Map<String, Object> lookupCase(
      @Schema(name = "caseId", description = "Identifier such as CASE-1042") String caseId) {
    if (caseId == null || !caseId.matches("CASE-[0-9]{4}")) {
      return Map.of("error", "invalid_id");
    }
    return switch (caseId) {
      case "CASE-1042" -> Map.of("id", caseId, "status", "open", "title", "E-book download fails after password reset");
      case "CASE-1043" -> Map.of("id", caseId, "status", "resolved", "title", "Gift card balance shows zero");
      default -> Map.of("id", caseId, "error", "not_found");
    };
  }

  /**
   * Teaching contrast for lesson 2, never registered with an agent: it trusts whatever the model
   * sends. Text the model controls is echoed back into the next model request as if it were data.
   */
  public Map<String, Object> lookupCaseUnchecked(String caseId) {
    return switch (String.valueOf(caseId)) {
      case "CASE-1042" -> Map.of("id", caseId, "status", "open", "title", "E-book download fails after password reset");
      case "CASE-1043" -> Map.of("id", caseId, "status", "resolved", "title", "Gift card balance shows zero");
      default -> Map.of("id", String.valueOf(caseId), "error", "not_found");
    };
  }

  @Schema(description = "Remember a valid synthetic case in the current session.")
  public Map<String, Object> selectCase(
      @Schema(name = "caseId", description = "Synthetic case identifier") String caseId,
      ToolContext toolContext) {
    var result = lookupCase(caseId);
    if (!result.containsKey("error")) toolContext.state().put("selected_case", caseId);
    return result;
  }

  @Schema(description = "Recall the case selected in this session.")
  public Map<String, Object> recallCase(ToolContext toolContext) {
    return Map.of("selected_case", toolContext.state().getOrDefault("selected_case", "none"));
  }
}
