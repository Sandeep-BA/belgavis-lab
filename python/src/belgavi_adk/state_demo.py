"""Lesson 4 demo: one two-session script against a leaky and a session-scoped agent.

Author: Sandeep Belgavi.
"""

import asyncio

from .lab import LabSession, create_agent, create_leaky_demo_agent


async def run(label, make_agent):
    print(f"== {label}")
    async with LabSession(make_agent()) as first, LabSession(make_agent()) as second:
        print("session A: Select CASE-1043 ->", await first.turn("Select CASE-1043"))
        print("session B: Recall          ->", await second.turn("Recall"))


def main():
    print("OFFLINE FIXTURE: real ADK")
    asyncio.run(run("LEAKY (module-level dict)", create_leaky_demo_agent))
    asyncio.run(run("SAFE  (tool_context.state)", lambda: create_agent(4)))


if __name__ == "__main__":
    main()
