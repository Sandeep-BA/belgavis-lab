# References and reading order

By Sandeep Belgavi. Links checked 2026-10-19.

## Primary SDK references

- [Official Java quickstart](https://adk.dev/get-started/java/): setup, agent construction and running a first application.
- [Official function-tool guide](https://adk.dev/tools-custom/function-tools/): tool declarations and injected context.
- [Google ADK Java releases](https://github.com/google/adk-java/releases): released Java versions.

## Reading the framework source

The application depends on the released artifact `com.google.adk:google-adk:1.12.0`. The links below are pinned to the matching source tag, so they do not move with the main branch. They are reference links, not framework code copied into this application.

- [Source at v1.12.0](https://github.com/google/adk-java/tree/v1.12.0): browse the modules.
- [City/time/weather tutorial](https://github.com/google/adk-java/tree/v1.12.0/tutorials/city-time-weather): another small application to study after lesson 1.
- [FunctionTool implementation](https://github.com/google/adk-java/blob/v1.12.0/core/src/main/java/com/google/adk/tools/FunctionTool.java): read how function methods are exposed to the runtime.
- [Framework license](https://github.com/google/adk-java/blob/v1.12.0/LICENSE): Apache-2.0, separate from the MIT license on our original examples.

The city/time/weather tutorial illustrates `LlmAgent.builder`, `FunctionTool.create` and annotated parameters. Its weather response is hard-coded, not a live weather service. Our help-desk examples apply those framework concepts to original synthetic data and add repeatable tests; they do not copy that tutorial's implementation.

## AICassindra reading map

Read the executable lesson first, then the associated article for broader context. The links below resolved successfully when checked. Articles can evolve independently of this release.

- **Lesson 1:** [ADK Java core module](https://aicassindra.com/blogs/adk_java/adk_java_core_module_overview.html). Use it to place the agent, runner and session within the framework.
- **Lesson 3:** [ADK sessions](https://aicassindra.com/blogs/adk/adk_sessions.html). The application/user/session identity tuple and how events accumulate in one session. Its examples may use the Python SDK.

AICassindra material is by Sandeep Belgavi. It is linked rather than republished. Exact SDK code should be taken from the tested source here and the pinned upstream release, not assumed from conceptual article snippets. No private AICassindra files or local editorial URLs are required by the learner project.

Reading for the next lessons is added as each episode is released.
