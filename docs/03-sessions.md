# 3. Keep two turns in one session

By Sandeep Belgavi.

**Outcome:** see the failure a new-session-per-message bug causes, fix it by reusing one session, and tell conversation history apart from explicit application state.

Pinned for this lesson: ADK for Java 1.12.0 (`java/pom.xml`, [releases](https://github.com/google/adk-java/releases)) and ADK for Python 2.12.0 ([PyPI](https://pypi.org/project/google-adk/2.12.0/)). See the [official ADK documentation](https://adk.dev/) for the session concepts.

## The failure this lesson prevents

A user selects a case, then asks a follow-up. If the application opens a new session for the follow-up, the agent has no record of the first message and the tool finds no selection. The model is not at fault: the application started a second conversation.

## Step 1: Watch it fail

```sh
mvn -f java/pom.xml exec:java "-Dexec.args=3 offline 'Select CASE-1043' fresh-session"
```

The `fresh-session` switch sends the follow-up through a brand-new `LabSession`, which is the bug. Expected: the first answer describes CASE-1043; after `NEW SESSION for the follow-up (the bug)` the recall answer is `selected_case=none`. The order of keys inside `{...}` can differ between runs.

## Step 2: Run the fixed conversation

```sh
mvn -f java/pom.xml exec:java "-Dexec.args=3 offline 'Select CASE-1043'"
```

The CLI sends the selection request and then a recall request through the same session. Expected: the second answer contains `selected_case=CASE-1043`.

## Step 3: Follow session creation

Open `LabSession.java`. The session is created once in the constructor, and both calls to `turn` pass the same session ID and the same demo user. Do not move session creation into `turn`, or each message starts a fresh conversation. `LabSession.id()` exposes the identifier, and `storedEventCount()` shows the stored history growing across turns.

The CLI's extra recall turn is in `LabCli.java` and runs for lesson 3. The command then exits; there is no interactive chat loop.

## Step 4: Separate two kinds of memory

ADK stores conversation events in the session. Our selection tool also writes an explicit `selected_case` value to session state, and the recall tool reads that value directly. This offline example demonstrates session continuity and state; it does not prove that a live model inferred a fact from conversation history.

```text
Session A: select CASE-1043 → selected_case = CASE-1043
Session A: recall         → CASE-1043
Session B: recall         → none
```

## Step 5: Run the checks

```sh
mvn -f java/pom.xml test "-Dtest=LabTest#followUpInTheSameSessionRemembersTheSelection+newSessionPerTurnLosesTheSelection+stateIsSessionLocal"
```

- `followUpInTheSameSessionRemembersTheSelection`: same ID on both turns, selection recalled, event count grows.
- `newSessionPerTurnLosesTheSelection`: a second session has a different ID and no selection.
- `stateIsSessionLocal`: independent sessions do not share state.

A shared static selection would break the last two. Read the tests before changing them.

## Step 6: Explain the limits

The service is in memory. Restarting the process loses its sessions. The fixed `demo_user` is not an authenticated identity. A hosted application would need trusted user identity, ownership checks, persistence and session expiry.

## Python comparison

```sh
python -m belgavi_adk.cli 3 --prompt "Select CASE-1043" --fresh-session
python -m belgavi_adk.cli 3 --prompt "Select CASE-1043"
```

Expected: the same two runs as Java. Python prints dictionaries in Python syntax, for example `{'selected_case': 'none'}` after the fresh session and `{'selected_case': 'CASE-1043'}` in the fixed run. ADK's warnings go to stderr. The tests are `test_follow_up_in_same_session_remembers_selection`, `test_new_session_per_turn_loses_selection`, `test_state_isolation` and `test_failed_selection_preserves_state`:

```sh
python -m pytest python/tests -q
```

| Concept | Java | Python |
|---|---|---|
| Runner | `InMemoryRunner(agent, appName)` | `InMemoryRunner(agent=..., app_name=...)` |
| Create a session | `runner.sessionService().createSession(app, user)` (RxJava `Single`) | `await runner.session_service.create_session(app_name=..., user_id=...)` |
| Send a turn | `runner.runAsync(user, sessionId, content, runConfig)` | `runner.run_async(user_id=..., session_id=..., new_message=...)` |
| Read state | `getSession(...).state()` | `get_session(...).state` |
| Read history | `getSession(...).events()` | `get_session(...).events` |

The Python `LabSession` also reuses one stored session ID for both turns. The session concept is the same even though the API names differ. The Python `LabSession` is an async context manager; Java's is `AutoCloseable`.

## Read next

Lesson 4, explicit state without cross-session leaks, arrives with Episode 4. [AICassindra's session guide](https://aicassindra.com/blogs/adk/adk_sessions.html) explains the application/user/session identity tuple and accumulated events. Its examples may use the Python SDK.
