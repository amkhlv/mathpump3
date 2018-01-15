Installation
============

First install ``linux-headers-3``, then install some dependencies as follows:

    aptitude install x11proto-randr-dev xserver-xorg xserver-xorg-dev libxrandr-dev libxi-dev libxinerama-dev libudev0 libudev-dev

Clone ``linuxwacom-input-wacom`` from http://sourceforge.net/p/linuxwacom/input-wacom/ci/master/tree/

In the folder ``linuxwacom-input-wacom``:

    ./autogen.sh

After the ``autogen.sh`` script gets executed, it should print instructions about where to put the ``wacom.ko`` file.
Do it in order for the driver to be properly picked up at the system startup.

But for now, if you don't want to restart the system, you can load them immediately, by executing as root:

    insmod 3.7/wacom.ko 

After that, the tablet should start working immediately.

After each kernel upgrade
=========================

Unfortunately, after each kernel upgrade have to do the following:

    make clean
    ./autogen.sh

Then:

    insmod 3.7/wacom.ko

and also follow instructions printed by ``autogen.sh``.

Mapping buttons to Ctrl-S
=========================

It may come handy if Wacom, on pressing its button, would initiate the saving of the current `SVG` file.
(And therefore the transmission.)

See [my writeup](Wacom_buttons.md) on how to achieve this.

