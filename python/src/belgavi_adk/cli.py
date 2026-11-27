"""Command-line entry point for the lessons. Author: Sandeep Belgavi."""

import argparse
import asyncio

from .lab import RELEASED, LabSession, create_agent, create_unguarded_agent


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("lesson", type=int, choices=RELEASED, nargs="?", default=1)
    parser.add_argument("--live", action="store_true")
    parser.add_argument(
        "--fresh-session",
        action="store_true",
        help="lesson 3 demo: send the follow-up through a new session (the bug)",
    )
    parser.add_argument("--unguarded", action="store_true", help="lesson 5 only: no callback")
    parser.add_argument("--prompt", default="Look up CASE-1042")
    args = parser.parse_args()
    if args.unguarded and args.lesson != 5:
        parser.error("--unguarded applies to lesson 5 only")
    print("LIVE MODEL: API usage may incur charges" if args.live else "OFFLINE FIXTURE: real ADK")
    if args.unguarded:
        print("UNGUARDED: prompt-only rule, no callback (lesson 5 demo)")

    async def run():
        agent = (
            create_unguarded_agent(args.live)
            if args.unguarded
            else create_agent(args.lesson, args.live)
        )
        async with LabSession(agent) as session:
            print(await session.turn(args.prompt))
            if args.lesson in (3, 4) and not args.fresh_session:
                print(await session.turn("Recall the selected case"))
        if args.fresh_session:
            print("NEW SESSION for the follow-up (the bug)")
            async with LabSession(create_agent(args.lesson, args.live)) as other:
                print(await other.turn("Recall the selected case"))

    asyncio.run(run())


if __name__ == "__main__":
    main()
