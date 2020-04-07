Third iteration of MathPump
===========================

This version has (hopefully) improved reliability, and does not use `JavaFX`. Instead,
it uses a small [Racket](https://racket-lang.org/) program for GUI. 

Introduction
============

**MathPump** is a simplistic remote collaboration tool for mathematicians and other researchers. It could be particularly useful
for those researchers who tend to think by drawing pictures (theoretical physics). 

![Mathpump](docs/images/mathpump.png?raw=true)

Bob uses [Inkscape](http://inkscape.org/) to draw a picture, which is incrementally transmitted to the Alice's computer so she can look at it.
She answers by drawing her own picture, which is transmitted to Bob. Transmission happens every time the svg file is saved. 

The transmission requires a special server. That is, both Bob and Alice can have their machines under a firewall. But the server
(run by Charlie) has to have some ports open. We recomment that Charlie [buy a cheap VPS](http://lowendbox.com/). The server is the standard
[RabbitMQ](http://www.rabbitmq.com/). This README only explains how to setup the client (Alice and Bob). If you are 
Charlie, please read [docs/setup-server.md](docs/setup-server.md).

Wacom device
============

A [Wacom device](http://www.wacom.com/) is recommended. As they change rapidly, some present difficulties with Linux. See, for example,
[my writeup on CTL-480](docs/Wacom_ctl-480.md), similar steps should theoretically work also for other models.
It is useful to [map one of Wacom buttons](docs/Wacom_buttons.md) to `Save File` in Inkscape. 

Client setup
============

This manual is for Linux. The installation on Windows should be completely analogous.

Prerequisites
-------------

1. Install JDK (on Debian: `sudo aptitude install default-jdk`)

2. Install [sbt](http://www.scala-sbt.org/) (on Debian: `sudo aptitude install sbt`)

3. Install [git](https://git-scm.com/) (on Debian: `sudo aptitude install git`)

4. Install [Inkscape](https://inkscape.org/) (on Debian: `sudo aptitude install inkscape`)

5. Install [Racket](https://racket-lang.org/) (we use Racket for graphical windows)
   (on Debian: `sudo aptitude install --without-recommends racket`)

6. Install `librsvg` (on Debian: `sudo aptitude install librsvg2-2 librsvg2-common`, on Mac OS/X: `brew install librsvg`)

7. For sound: `sudo aptitude install pulseaudio-utils`, which installs `paplay`,
    or any other program that can play `.wav`. 
    It should be specified in `mathpump.conf`, _e.g._ : `beeper = "paplay"`

8. Make sure that the directory `~/.local/bin/` exists and is on the executable `PATH`


Building
--------

Then execute the following commands:

    git clone https://github.com/amkhlv/mathpump3
    cd mathpump3
    ./setup.sh

This actually takes some time, maybe 10 min or so. 

Configuration and running
=========================

    cd example/

See [example/README.md](example/README.md)

You will need three things from Charlie:

1. `trustStore` (a file)
2. password
3. truststore passphrase

Use hints
=========

Voice channel
-------------

There is no voice channel in `MathPump`. I recommend Skype conference call for voice,
or WhatsApp if less than 5 people.


Chaining MathPump with SVGServer
--------------------------------

Suppose that you are giving a distributed talk over Internet. Everyone in the audience is sitting
at home, with their own computer. It is not practical to request everyone to install MathPump.
In this situation, chain MathPump with [SVGServer2](https://github.com/amkhlv/SVGServer2), in the following way:

1. On the same server where `RabbitMQ` runs, run a headless instance of MathPump. "Headless" means 
   setting  `viewer = "~/.local/lib/mathpump/mathpump-headless %"` (instead of `mathpump-board`)
   and `beeper = "true"` (`true` is the Linux command which does nothing)

2. Make [SVGServer2](https://github.com/amkhlv/SVGServer2) to watch the incoming directory of that headless MathPump
   (the `dir` in `instance.xml`).

3. [SVGServer2](https://github.com/amkhlv/SVGServer2) serves your talk via its web interface

Now your audience can watch your talk in their browsers.

Using BystroTeX to prefabricate formulas
----------------------------------------

When giving a talk, it is useful to have prefab formulas. 
Since our format is `SVG`, we need `SVG` formulas.
They can be prepared by [BystroTeX](http://andreimikhailov.com/slides/bystroTeX/slides-manual/index.html).
Prepare a ``skeleton'' BystroTeX slide containing main formulas of the talk.
Then, in Firefox, right click on the formula, "Copy Image" and then paste in Inkscape.


Inkscape hints
--------------

I prefer, for drawing formulas, the calligraphic tool (keyboard shortcut `c`).

See more hints in [Inkscape hints](docs/inkscape.md)
