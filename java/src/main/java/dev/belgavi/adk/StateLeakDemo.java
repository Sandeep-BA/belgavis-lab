// Author: Sandeep Belgavi
package dev.belgavi.adk;

import com.google.adk.agents.BaseAgent;
import java.util.function.Supplier;

/** Lesson 4 demo: the same two-session script against a leaky and a session-scoped agent. */
public final class StateLeakDemo {
  private StateLeakDemo() {}

  public static void main(String[] args) {
    System.out.println("OFFLINE FIXTURE: real ADK, simulated model");
    try {
      run("LEAKY (static field)", LessonAgents::createLeakyDemo);
      run("SAFE  (ToolContext state)", () -> LessonAgents.create(4, false));
    } finally {
      io.reactivex.rxjava3.schedulers.Schedulers.shutdown();
    }
  }

  static void run(String label, Supplier<BaseAgent> agents) {
    System.out.println("== " + label);
    try (var first = new LabSession(agents.get());
        var second = new LabSession(agents.get())) {
      System.out.println("session A: Select CASE-1043 -> " + first.turn("Select CASE-1043"));
      System.out.println("session B: Recall          -> " + second.turn("Recall"));
    }
  }
}
