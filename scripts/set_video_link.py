"""Fill an episode's Video cell in the README "Watch the series" table.

Author: Sandeep Belgavi.

    python scripts/set_video_link.py <episode> <youtube-url-or-id> [--readme README.md]

Accepts a full YouTube URL (watch, youtu.be, shorts, embed) or the 11-character
video id, and writes a canonical https://www.youtube.com/watch?v=<id> link.
Only the Video column of the matching row changes; running it again replaces
the link.
"""
import argparse
import pathlib
import re
import sys

ID = re.compile(r"^[A-Za-z0-9_-]{11}$")
URL = re.compile(r"(?:v=|youtu\.be/|/shorts/|/embed/|/live/)([A-Za-z0-9_-]{11})")
SECTION = "## Watch the series"


def video_id(value):
    if ID.match(value):
        return value
    m = URL.search(value)
    if not m:
        raise ValueError("not a YouTube URL or video id: %s" % value)
    return m.group(1)


def set_link(text, episode, vid):
    start = text.find(SECTION)
    if start < 0:
        raise ValueError("README has no '%s' section" % SECTION)
    end = text.find("\n## ", start + len(SECTION))
    end = len(text) if end < 0 else end
    lines = text[start:end].split("\n")
    for i, line in enumerate(lines):
        cells = line.split("|")
        if len(cells) > 3 and cells[1].strip() == str(episode):
            cells[-2] = " [Watch](https://www.youtube.com/watch?v=%s) " % vid
            lines[i] = "|".join(cells)
            return text[:start] + "\n".join(lines) + text[end:]
    raise ValueError("no row for episode %s in '%s'" % (episode, SECTION))


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("episode", type=int)
    ap.add_argument("video", help="YouTube URL or 11-character video id")
    ap.add_argument("--readme", default="README.md")
    args = ap.parse_args(argv)
    path = pathlib.Path(args.readme)
    try:
        text = set_link(path.read_text(encoding="utf-8"), args.episode, video_id(args.video))
    except ValueError as err:
        print("error:", err, file=sys.stderr)
        return 2
    path.write_text(text, encoding="utf-8", newline="\n")
    print("episode %d video linked in %s" % (args.episode, path))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
