#!/usr/bin/env bash

[ -L ~/.local/bin/mathpump.sh ] && { rm ~/.local/bin/mathpump.sh; }

ln -s "$(pwd)/mathpump.sh" ~/.local/bin/

python -m venv svg_whiteboard

(cd svg_whiteboard
source bin/activate
pip install -r requirements.txt)

sbt assembly





