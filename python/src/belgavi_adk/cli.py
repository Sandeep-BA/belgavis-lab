"""Command-line entry point for the lessons. Author: Sandeep Belgavi."""

import argparse
import asyncio

from .lab import RELEASED, LabSession, create_agent


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("lesson", type=int, choices=RELEASED, nargs="?", default=1)
    parser.add_argument("--live", action="store_true")
    parser.add_argument(
        "--fresh-session",
        action="store_true",
        help="lesson 3 demo: send the follow-up through a new session (the bug)",
    )
    parser.add_argument("--prompt", default="Look up CASE-1042")
    args = parser.parse_args()
    print("LIVE MODEL: API usage may incur charges" if args.live else "OFFLINE FIXTURE: real ADK")

    async def run():
        async with LabSession(create_agent(args.lesson, args.live)) as session:
            print(await session.turn(args.prompt))
            if args.lesson == 3 and not args.fresh_session:
                print(await session.turn("Recall the selected case"))
        if args.fresh_session:
            print("NEW SESSION for the follow-up (the bug)")
            async with LabSession(create_agent(args.lesson, args.live)) as other:
                print(await other.turn("Recall the selected case"))

    asyncio.run(run())


if __name__ == "__main__":
    main()
