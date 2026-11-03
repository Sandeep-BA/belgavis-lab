// Author: Sandeep Belgavi
package dev.belgavi.adk;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.runner.InMemoryRunner;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** One runner and one user session; do not use it as a production identity boundary. */
public final class LabSession implements AutoCloseable {
  private final InMemoryRunner runner;
  private final String id;

  public LabSession(BaseAgent agent) {
    runner = new InMemoryRunner(agent, "belgavi_lab");
    id = runner.sessionService().createSession(runner.appName(), "demo_user").blockingGet().id();
  }

  public String turn(String prompt) {
    return runner
        .runAsync(
            "demo_user",
            id,
            Content.fromParts(Part.fromText(prompt)),
            RunConfig.builder().maxLlmCalls(12).build())
        .timeout(90, TimeUnit.SECONDS)
        .filter(e -> e.finalResponse())
        .map(e -> e.stringifyContent())
        .toList()
        .blockingGet()
        .toString();
  }

  /** The identifier every turn of this object reuses. */
  public String id() {
    return id;
  }

  /** Events (user messages, tool calls and results, answers) the session service has stored. */
  public int storedEventCount() {
    return runner
        .sessionService()
        .getSession(runner.appName(), "demo_user", id, Optional.empty())
        .blockingGet()
        .events()
        .size();
  }

  public Map<String, Object> state() {
    return runner
        .sessionService()
        .getSession(runner.appName(), "demo_user", id, Optional.empty())
        .blockingGet()
        .state();
  }

  @Override
  public void close() {
    runner.close().blockingAwait();
  }
}
