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

1. Install JDK

2. Install [sbt](http://www.scala-sbt.org/)

3. Install [git](https://git-scm.com/)

4. Install [Inkscape](https://inkscape.org/)

5. Install [Racket](https://racket-lang.org/) (we use Racket for graphical windows)

6. Install `librsvg` (on Debian: `sudo aptitude install librsvg2-2 librsvg2-common`)

7. For sound: `sudo aptitude install pulseaudio-utils`, which installs `paplay`,
    or any other program that can play `.wav`; it should be specified in `settings.conf`

8. Make sure that the directory `~/.local/bin/` exists and is on the executable `PATH`


Building
--------

Then execute the following commands:

    git clone https://github.com/amkhlv/mathpump3
    cd mathpump3
    ./setup.sh

This actually takes some time, depending on your Internet connection. Maybe 20 min or so. 

Configuration and running
=========================

    cd example/

See [example/README.md](example/README.md)

You will need three things from Charlie:

1. `trustStore` (a file)
2. password
3. truststore passphrase


