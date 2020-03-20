#!/usr/bin/env bash

[ -d ~/.local/bin ] || { mkdir -p ~/.local/bin ; }
[ -d ~/.local/lib/mathpump ] || { mkdir -p ~/.local/lib/mathpump ; }
[ -d ~/.local/share/mathpump ] || { mkdir -p ~/.local/share/mathpump ; }

sbt assembly

raco pkg install rsvg

(
    [ -e ~/.local/bin/mathpump.sh ] && { rm ~/.local/bin/mathpump.sh ; }
    cd files
    # cp mathpump.sh  ~/.local/bin/
    cd Racket
    raco exe mathpump.rkt
    cp mathpump ~/.local/bin/
    raco exe mathpump-inkwell.rkt
    cp mathpump-inkwell ~/.local/bin/
    raco exe mathpump-board.rkt
    cp mathpump-board ~/.local/lib/mathpump/
    raco exe mathpump-headless.rkt
    cp mathpump-headless ~/.local/lib/mathpump/
)

cp files/sounds/* ~/.local/share/mathpump/

[ "$(echo $PATH | grep -F $HOME/.local/bin)" ] || {
    echo "PROBLEM:"
    echo "~/.local/bin/ is not on "'$PATH'
    echo "please correct this!"
    }








