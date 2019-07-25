#!/usr/bin/env bash

help() {
    cat << ENDDATA
usage:

  $(basename "$0") start

  -- starts MathPump in the current dir, which should contain settings.conf


  $(basename "$0") stop

  -- stops pumping;  the whiteboard windows should be closed separately


  $(basename "$0") show [USERNAME]

  -- starts whiteboard for the user USERNAME
     when no USERNAME is give, starts whiteboards for all users

ENDDATA
}

case $1 in
    start)
        java -Dconfig.file=settings.conf -jar ~/.local/lib/mathpump/mathpump-assembly.jar &
        [ "$2" == "+" ] && { shift 2; "$0" "$@"; }
    ;;
    inkscape)
        if [ "$2" ]
        then
            if [ "$2" == "+" ]
            then
                inkscape outgoing/whiteboard.svg &
                shift 2; "$0" "$@"
            else
                inkscape outgoing/"$2".svg &
                [ "$3" == "+" ] && { shift 3 ; "$0" "$@" ; } || { shift 2 ; "$0" inkscape "$@" ; }
            fi
        else
            inkscape outgoing/whiteboard.svg &
        fi
    ;;
    stop)
        mv tmp/stop/* outgoing/
    ;;
    *)
        help
    ;;
esac
