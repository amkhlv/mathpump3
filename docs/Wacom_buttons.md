Problem
=======

We want configure Wacom so that pressing the button saves the current `SVG` file. (And therefore initiates the
transmission.)

Wacom device usually has four buttons. Two left buttons are typically mapped to the left and right mouse buttons.
However, two right buttons are mapped to `button 8` and `button 9` which are unused.
It is useful to map them to `Ctrl-S` in `Inkscape`.


Solution 1
==========

Use the command `xsetwacom`

First figure out the device number:

    xsetwacom --list devices

This will give you something like this:

    Wacom Intuos S Pad pad                  id: 16  type: PAD
    Wacom Intuos S Pen stylus               id: 17  type: STYLUS
    Wacom Intuos S Pen eraser               id: 18  type: ERASER

In this case we should use __16__ :

    xsetwacom --set 16 Button 3 key +control s -control
    xsetwacom --set 16 Button 1 key +control s -control

(I am not sure how to figure out which button is which number, but for my Wacom those were the two left buttons.
The key sequence here is to first press `control`, then press and release `s`, then release `control`.)

__Attention__ : if you press this button while hovering over a terminal, you will __freeze__ the terminal. To unfreeze press `Ctrl-Q`.


Solution 2
==========

This is probably __obsolete__ (see [Solution 1](#Solution 1) above)

We will use `xbindkeys` :

    aptitude install xbindkeys

Create the file `~/.xbindkeysrc` and put the following lines in it:

    #mouse button 8
    "amkhlv-mousebutton.sh 8"
        b:8

    #mouse button 9
    "amkhlv-mousebutton.sh 9"
        b:9

It remains to write the script `amkhlv-mousebutton.sh` :

    WMC="$(xprop -id `xprop -root _NET_ACTIVE_WINDOW | sed -e's/.*\(0x.*\),.*/\1/g'` | awk '/WM_CLASS/{print $4}')"

    [ "$1" ] || { echo $WMC ; exit 0 ; }

    case $WMC in
        '"Inkscape"')
            xte 'keydown Control_L' 'key S' 'keyup Control_L'
            ;;
        *)
            true
            ;;
    esac

Here in the first line, the inner `xprop` gets the window ID of the currently active window. 
It assumes that the output of `xprop -root _NET_ACTIVE_WINDOW` is of the form:

    _NET_ACTIVE_WINDOW(WINDOW): window id # 0x520000f, 0x0

The solution [on askubuntu](http://askubuntu.com/questions/97213/application-specific-key-combination-remapping)
is slightly different, and perhaps better. They use the `xdotool` utility to pass the mouse event in case
the window class does not match. (While I simply drop it, assuming that no sensible programs use higher mouse
buttons.)

