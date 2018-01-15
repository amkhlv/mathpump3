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

MPDIR="$(dirname "$(realpath "$0")")"/

find_jar() {

    X="$(find "${MPDIR}target/" -maxdepth 2 -name 'MathPump-assembly*.jar')"

    [ "$X" ] || { echo "ERROR: can not find jar"; exit 1; }

    [[ $(echo -n $X | wc | awk '{print $1}') != 0 ]] && { echo "ERROR: more than one jar"; exit 1; }

    echo -n "$X"

}

whiteboard() {

    export INCOMINGDIR="$(realpath $1)"
    (cd "$MPDIR/svg_whiteboard";
    source bin/activate;
    python3 bin/svg_whiteboard.py --qml QML/svg-whiteboard.qml "$INCOMINGDIR") &

}

all_whiteboards() {
    find incoming/  -maxdepth 1  -not -path incoming/  -type d | while read U ; do whiteboard $U ; done
}

case $1 in
    start)
        java -Dconfig.file=settings.conf -jar "$(find_jar)" &
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
    show)
        if [ "$2" ]
        then
            if [ "$2" == "+" ]
            then
                all_whiteboards
                shift 2; "$0" "$@"
            else
                whiteboard "incoming/$2"
                [ "$3" == "+" ] && { shift 3; "$0" "$@"; }
            fi
        else
            all_whiteboards
        fi
    ;;
    *)
        help
    ;;
esac