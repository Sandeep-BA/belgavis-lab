// Author: Sandeep Belgavi
package dev.belgavi.adk;

public final class LabCli {
  private LabCli() {}

  public static void main(String[] args) {
    if (args.length > 0 && args[0].equals("contract")) {
      ContractDemo.main(args);
      return;
    }
    int lesson = args.length > 0 ? Integer.parseInt(args[0]) : 1;
    boolean live = args.length > 1 && args[1].equals("live");
    // Lesson 5 teaching switch: the prompt-only rule with no callback (the failure).
    boolean unguarded = args.length > 1 && args[1].equals("unguarded");
    if (args.length > 1 && !live && !unguarded && !args[1].equals("offline"))
      throw new IllegalArgumentException("Mode must be offline, live or unguarded.");
    if (unguarded && lesson != 5)
      throw new IllegalArgumentException("Mode unguarded applies to lesson 5 only.");
    String prompt = args.length > 2 ? args[2] : "Look up CASE-1042";
    // Lesson 3 teaching switch: send the follow-up through a brand-new session (the bug).
    boolean freshSession = args.length > 3 && args[3].equals("fresh-session");
    if (args.length > 3 && !freshSession)
      throw new IllegalArgumentException("Fourth argument must be fresh-session.");
    System.out.println(
        live
            ? "LIVE MODEL: API usage may incur charges"
            : "OFFLINE FIXTURE: real ADK, simulated model");
    if (unguarded) System.out.println("UNGUARDED: prompt-only rule, no callback (lesson 5 demo)");
    var agent = unguarded ? LessonAgents.createUnguarded(false) : LessonAgents.create(lesson, live);
    try {
      try (var session = new LabSession(agent)) {
        System.out.println(session.turn(prompt));
        if ((lesson == 3 || lesson == 4) && !freshSession)
          System.out.println(session.turn("Recall the selected case"));
      }
      if (freshSession) {
        System.out.println("NEW SESSION for the follow-up (the bug)");
        try (var other = new LabSession(LessonAgents.create(lesson, live))) {
          System.out.println(other.turn("Recall the selected case"));
        }
      }
    } finally {
      // This process owns the Rx schedulers. Library/session code must not stop shared pools.
      io.reactivex.rxjava3.schedulers.Schedulers.shutdown();
    }
  }
}
