#!/usr/bin/env bash

sbt assembly

[ -d ~/.local/bin ] || { mkdir -p ~/.local/bin ; }
[ -L ~/.local/bin/mathpump.sh ] && { rm ~/.local/bin/mathpump.sh; }
ln -s "$(pwd)/mathpump.sh" ~/.local/bin/

[ -d ~/.local/lib/mathpump ] || { mkdir -p ~/.local/lib/mathpump ; }
raco pkg install rsvg
raco exe mathpump-board.rkt
cp mathpump-board ~/.local/lib/mathpump/


[ "$(echo $PATH | grep -F $HOME/.local/bin)" ] || {
    echo "PROBLEM:"
    echo "~/.local/bin/ is not on "'$PATH'
    echo "please correct this!"
    }








