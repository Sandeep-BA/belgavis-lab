# Belgavi's Lab

**By Sandeep Belgavi.** Build and understand a small help-desk agent for Belgavi's Book Store, a fictional online bookstore, with ADK for Java, one capability per episode of [Belgavi's AI Lab](https://www.youtube.com/@belgaviai) on YouTube.

This repository grows with the series: each episode adds its lesson here when the video is released. Every lesson has a command, an expected result, a code walkthrough, a test and an exercise. The default examples require no model account or API key.

## 1. Check your tools

Install **JDK 17+**, **Maven 3.9+** and Git. Python **3.11+** is optional, for the Python comparison in each lesson.

```sh
java -version
mvn -version
git --version
```

Maven must report the intended JDK. If it reports an older Java version, fix `JAVA_HOME` before continuing.

## 2. Get the project

```sh
git clone https://github.com/Sandeep-BA/belgavis-lab.git
cd belgavis-lab
```

Run all commands below from this directory. No framework checkout or local SDK build is needed. Maven downloads the pinned `com.google.adk:google-adk:1.12.0` dependency.

## 3. Build and test

```sh
mvn -B -ntp -f java/pom.xml test
```

Expected: `BUILD SUCCESS`. The suite exercises real ADK runners and tools with a deterministic model fixture. It also checks invalid identifiers and missing records. The first build downloads dependencies and needs internet access; the offline model only removes the need for a model account.

## 4. Run your first agent

```sh
mvn -f java/pom.xml exec:java -Dexec.args="1 offline"
```

Expected: `OFFLINE FIXTURE: real ADK, simulated model`, followed by an `Offline hello` response. The model is simulated; ADK orchestration is real. This proves wiring, not live-model intelligence.

## 5. Follow the lessons

| Episode | What you build | Start here |
| --- | --- | --- |
| 1 | Your first agent, and how ADK runs it | [Your first agent](docs/01-first-agent.md) |
| 2 | A function tool that validates what the model sends | [Function tools](docs/02-function-tools.md) |
| 3 | Sessions that keep a conversation together | [Sessions](docs/03-sessions.md) |
| 4 | Explicit state without cross-session leaks | [State](docs/04-state.md) |
| 5 | Deny a tool call before it runs | [Tool policy](docs/05-tool-policy.md) |

Episode 2's contrast demo runs the same inputs through an unchecked tool and the validated one:

```sh
mvn -f java/pom.xml exec:java -Dexec.args="contract"
```

Episode 3 sends a follow-up through a new session (the bug), then through the same session (the fix):

```sh
mvn -f java/pom.xml exec:java "-Dexec.args=3 offline 'Select CASE-1043' fresh-session"
mvn -f java/pom.xml exec:java "-Dexec.args=3 offline 'Select CASE-1043'"
```

Episode 4 runs the same two-session script against a tool that keeps state in a static field (the bug), then against `ToolContext` state (the fix):

```sh
mvn -f java/pom.xml exec:java "-Dexec.mainClass=dev.belgavi.adk.StateLeakDemo"
mvn -f java/pom.xml exec:java "-Dexec.args=4 offline 'Select CASE-1043'"
```

Episode 5 runs a prompt-only rule with no callback (the bug), then the same request with a before-tool callback (the fix):

```sh
mvn -f java/pom.xml exec:java "-Dexec.args=5 unguarded"
mvn -f java/pom.xml exec:java "-Dexec.args=5 offline"
```

**Coming next in the series:** workflows that chain lookup, policy and reply. Each lesson lands here with its episode. Watch or subscribe at [youtube.com/@belgaviai](https://www.youtube.com/@belgaviai).

### Python comparison

Each lesson also shows the Python equivalent with `google-adk` 2.12.0:

```sh
python -m venv .venv
.venv/bin/pip install -e "./python[dev]"      # Windows: .venv\Scripts\pip
.venv/bin/python -m pytest python/tests -q
.venv/bin/python -m belgavi_adk.contract_demo
.venv/bin/python -m belgavi_adk.cli 3 --prompt "Select CASE-1043" --fresh-session
.venv/bin/python -m belgavi_adk.state_demo
.venv/bin/python -m belgavi_adk.cli 5 --unguarded
```

## 6. Understand the boundaries

- Only synthetic `CASE-1042` and `CASE-1043` records exist. The tools contact no external business system.
- Sessions are in memory and use a fixed demo identity. This is not a hosted authentication or persistence implementation.
- A live model is optional and may incur charges. See [live setup](docs/live-mode.md) before enabling it.
- [Troubleshooting](docs/troubleshooting.md) explains common setup and SDK warnings.
- [Architecture](docs/architecture.md) explains the code layout and execution flow.
- [References](docs/references.md) maps each lesson to the official docs, the pinned Google ADK source and relevant AICassindra articles.

## Watch the series

Every lesson has a video on [Belgavi's AI Lab](https://www.youtube.com/@belgaviai). [Subscribe](https://www.youtube.com/@belgaviai?sub_confirmation=1) to get each episode as it lands; its lesson arrives here on the same day.

| Episode | Topic | Lesson | Video |
| --- | --- | --- | --- |
| 1 | What an AI agent really is, and how ADK runs one | [Your first agent](docs/01-first-agent.md) | coming soon |
| 2 | Function tools with explicit contracts | with the episode | coming soon |
| 3 | Sessions and multi-turn conversations | with the episode | coming soon |
| 4 | Explicit state without cross-session leaks | with the episode | coming soon |
| 5 | Block a tool call outside the prompt | with the episode | coming soon |
| 6 | A sequential workflow with a visible handoff | with the episode | coming soon |
| 7 | Parallel agents without shared-key collisions | with the episode | coming soon |
| 8 | Bound your loops and your model calls | with the episode | coming soon |
| 9 | Evaluate behavior, not polished answers | with the episode | coming soon |
| 10 | Package an agent lab for repeatable development | with the episode | coming soon |

When an episode is published, fill its Video cell with `python scripts/set_video_link.py <episode> <youtube-url-or-id>` (for example `python scripts/set_video_link.py 1 https://youtu.be/VIDEO_ID`). Link the lesson in the same commit when it is released.

## Reference projects and attribution

[google/adk-java](https://github.com/google/adk-java) is the upstream framework. This application uses its released Maven artifact; the [pinned source map](docs/references.md) links the matching source tag for reading.

Original examples and learner documentation are by Sandeep Belgavi, licensed under [MIT](LICENSE). ADK and other dependencies retain their own licenses. No framework source is vendored. This independent educational project is not an official Google course. Views are my own and do not represent any employer, past or present.
