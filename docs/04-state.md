# 4. Store validated state and preserve isolation

By Sandeep Belgavi.

**Outcome:** update a session value through `ToolContext`, keep it intact when validation fails and verify that another session in this lab cannot read it.

## The failure this lesson prevents

A tool that remembers the "current case" in a `static` field (Java) or a module-level variable (Python) shares that value with every session in the process. Session B can then read the case session A selected. This lab ships that mistake on purpose, in `LeakyCaseTools` and `select_shared_case`, so you can watch it happen and compare it with the fix. Never copy the leaky versions into an application.

```sh
mvn -f java/pom.xml exec:java "-Dexec.mainClass=dev.belgavi.adk.StateLeakDemo"
python -m belgavi_adk.state_demo
```

Expected (Java): under `LEAKY`, session B's recall returns `selected_case=CASE-1043`, a case it never selected. Under `SAFE`, it returns `selected_case=none`. The Python demo prints the same two outcomes.

The fix is to write through `ToolContext`, whose `state` belongs to the current session. The tests `staticFieldStateLeaksAcrossSessions` and `stateIsSessionLocal` (Java), and `test_module_level_state_leaks_across_sessions` and `test_state_isolation` (Python), pin both behaviors. The leak test asserts the leak, so a refactor cannot hide it silently; the leaky agent's session state stays empty, which is how you can tell the value lives outside ADK.

## Step 1: Run the state example

```sh
mvn -f java/pom.xml exec:java "-Dexec.args=4 offline 'Select CASE-1043'"
```

Expected: selection followed by a recall of CASE-1043. Lesson 4 deliberately uses lesson 3's configuration. The new work is inspecting and testing state mutation, not adding another agent.

## Step 2: Inspect the write boundary

In `CaseTools.selectCase`, validation and lookup happen before `selected_case` is written. Failed lookups return an error and leave the previous selection intact. `recallCase` returns `none` when the key is absent.

The injected Java parameter is named `toolContext`. ADK for Java 1.12.0 injects the context by that parameter name, not by type, and the Maven compiler's `parameters` setting keeps parameter names in the class files. Treat this as a version-specific integration contract. Do not rename the parameter without testing.

## Step 3: Check a failed update

```sh
mvn -f java/pom.xml test "-Dtest=LabTest#failedSelectionDoesNotOverwriteState"
```

The test selects CASE-1043, attempts CASE-9999 and recalls again. Expected: CASE-1043 remains selected. No partial write occurs on a missing record.

## Step 4: Check a different session

```sh
mvn -f java/pom.xml test "-Dtest=LabTest#stateIsSessionLocal"
```

Expected: the first session recalls its case; the second returns `none`. This tests our in-memory example; it does not prove authorization for an HTTP service.

## Step 5: Try a controlled failure

On a temporary local branch, change the key written by `selectCase` but not the key read by `recallCase`. Run the isolation test and observe the failed recall assertion. Restore the key. This demonstrates why the state-key contract matters.

## Python comparison

```sh
python -m pytest python/tests/test_lab.py -q -k "state_isolation or failed_selection or leaks or written_to_the_session"
```

Python finds the context parameter by its `ToolContext` type annotation and falls back to the name `tool_context`; Java matches the name `toolContext`. Follow each SDK's conventions rather than translating names mechanically.

## Sources and versions

Pinned for this lesson: ADK for Java 1.12.0 (`java/pom.xml`, [releases](https://github.com/google/adk-java/releases)) and ADK for Python 2.12.0 ([PyPI](https://pypi.org/project/google-adk/2.12.0/)). This lab's offline tests exercise the injected-context behavior on those versions. See [references](references.md) for the official function-tool guide that describes injected tool context.

## Read next

Lesson 5, a tool the model must never run, arrives with Episode 5. [AICassindra's state-management guide](https://aicassindra.com/blogs/adk/adk_state_management.html) provides background on state scopes. This lab uses one session-scoped key and does not implement the broader storage architecture.
