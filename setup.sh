#!/usr/bin/env bash

[ -L ~/.local/bin/mathpump.sh ] && { rm ~/.local/bin/mathpump.sh; }

ln -s "$(pwd)/mathpump.sh" ~/.local/bin/

mkdir -p ~/.config/mathpump3/QML
mkdir -p ~/.config/mathpump3/svgs

cp  files/QML/svg-whiteboard.qml  ~/.config/mathpump3/QML/
cp  files/svgs/welcome.svg ~/.config/mathpump3/svgs/

sbt assembly





