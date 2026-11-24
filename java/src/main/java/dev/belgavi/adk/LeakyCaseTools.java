// Author: Sandeep Belgavi
package dev.belgavi.adk;

import com.google.adk.tools.Annotations.Schema;
import java.util.Map;

/**
 * Teaching counterexample for lesson 4. The selection lives in a static field, so every session in
 * the JVM shares it. Never copy this pattern; see {@link CaseTools#selectCase} for the fix.
 */
public final class LeakyCaseTools {
  private static volatile String shared = "none";

  @Schema(description = "Remember a valid synthetic case (shared, leaky teaching version).")
  public Map<String, Object> selectSharedCase(
      @Schema(name = "caseId", description = "Synthetic case identifier") String caseId) {
    var result = new CaseTools().lookupCase(caseId);
    if (!result.containsKey("error")) shared = caseId;
    return result;
  }

  @Schema(description = "Recall the shared case (leaky teaching version).")
  public Map<String, Object> recallSharedCase() {
    return Map.of("selected_case", shared);
  }

  /** Test hook so demos start from a clean slate. */
  static void reset() {
    shared = "none";
  }
}
