
Completing the configuration
============================

You are now in the `example/` folder

Missing files
-------------

    alice2bob/trustStore
    bob2alice/trustStore

you should obtain them from Charlie


Missing settings
----------------

In `alice2bob/settings.conf` and `bob2alice/settings.conf`, the actual values of `password` and `trustStorePassphrase` 
should be the ones provided by Charlie


Running
=======

Obviously the profiles `alice2bob` and `bob2alice` are meant to be run on two different computers. 
However, they may be run on the same computer for testing.

On Linux, go to `alice2bob` and say:

    mathpump.sh start + inkscape

(this script assumes that `inkscape` is installed on your system)

Then go to `bob2alice` and execute the same command as before.

To stop, say:

    mathpump.sh stop

