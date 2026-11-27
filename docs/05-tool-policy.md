# 5. Deny a tool call before execution

By Sandeep Belgavi.

**Outcome:** demonstrate that application code can deny a model-proposed action, and test that the disabled tool body is not reached.

## Step 0: See the failure first

A sentence in the prompt is not a boundary. This variant tells the agent "Never export cases" and registers no callback. The fixture still asks for the export, and the tool body runs:

```sh
mvn -f java/pom.xml exec:java -Dexec.args="5 unguarded"
```

Expected: `exported=2`, with the synthetic ids `CASE-1042,CASE-1043`. The rule existed only as text, so nothing stopped the call. (A real model may often obey the sentence; the point is that nothing in your code guarantees it.) Only this demo enables the export body, and it returns synthetic data.

## Step 1: Request the denied action

```sh
mvn -f java/pom.xml exec:java -Dexec.args="5 offline"
```

Expected: `export_denied`. The fixture always requests `exportCases`, whatever the prompt, which makes the denial path repeatable.

## Step 2: Read the callback

In `LessonAgents.java`, find the `denied` role. It registers one tool and a `beforeToolCallbackSync` that calls `beforeTool(tool.name())`. That policy returns an error map for any tool in `DENIED_TOOLS` and `Optional.empty()` (allow) for every other tool. When the callback returns a map, ADK skips the tool and uses the map as the tool result.

```text
Proposed export → before-tool policy → export_denied
                                        ↓
                                 report the denial
Disabled export body is not executed.
```

This callback denies its only registered tool. It is a small teaching boundary, not a reusable role-based authorization engine.

## Step 3: Inspect the second boundary

`CaseTools.exportCases` throws if reached (the default `new CaseTools()` has export disabled; only the unguarded demo passes `true`). There is no export implementation and no customer data. If callback interception breaks, the code fails closed rather than performing an export.

## Step 4: Run the regression check

```sh
mvn -f java/pom.xml test "-Dtest=LabTest#everyReleasedLessonRunsThroughSdk"
```

The lesson 5 assertion requires the denial result. `LabTest` also covers the unguarded export, the denial and the allow/deny policy decision; the Python tests mirror them. Because the tool body throws, a successful run with that result also proves the call was intercepted. On a temporary branch, remove the callback and observe the failure, then restore it.

## Step 5: Explain what production would add

A real policy needs authenticated identity, resource ownership, a precise operation and allowed parameters. Tool-returned text is untrusted data. This demo does not establish prompt-injection resistance, an approval system or a full authorization service.

## Python comparison

```sh
python -m belgavi_adk.cli 5
```

Python uses `before_tool_callback` (`deny_export`, returning a dict to deny and `None` to allow) with the same deny-before-execution behavior. The failure demo is `python -m belgavi_adk.cli 5 --unguarded`. Return-value semantics and callback signatures are SDK-specific.

## Continue learning

Read [architecture](architecture.md) and [the references](references.md). [AICassindra's authorization article](https://aicassindra.com/blogs/adk_java/adk_java_authz_at_agent_boundary.html) expands the policy discussion. Lesson 6, a sequential workflow with a visible handoff, arrives with Episode 6.

## Versions and sources

Java uses `com.google.adk:google-adk:1.12.0`; Python uses `google-adk==2.12.0`. Both pins are exercised by the tests in this repository. The official [types of callbacks](https://adk.dev/callbacks/types-of-callbacks/) page documents the before-tool contract: returning `None` in Python (an empty result in Java) runs the tool; returning a dict or `Map` skips the tool and uses that value as its result. Java's `beforeToolCallbackSync` returns an `Optional`; the asynchronous form returns a `Maybe`.
