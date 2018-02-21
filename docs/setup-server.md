
Installing RabbitMQ
===================

Please [install a recent version of RabbitMQ](https://www.rabbitmq.com/install-debian.html)

(In particular, the version from Debian Stretch repository __does not seem to work__ for me,
I needed to update to 
`esl-erlang_20.2.2-1~debian~stretch_amd64.deb` and `rabbitmq-server_3.7.3-1_all.deb` )

On Debian, `RabbitMQ` is started immediately after install. To stop it, type:

    systemctl stop rabbitmq-server

First of all, let us change the default user's password. The default user is called `guest`.
To change the password, first have to start the server:

    systemctl start rabbitmq-server

and then say:

    rabbitmqctl delete_user guest


Keys and Certificates
=====================

There is a very good guide on the [RabbitMQ website](https://www.rabbitmq.com/ssl.html), just follow the 
instructions in the section called "Keys, Certificates and CA Certificates". You do not need the
client part, **only the server part**. 

CA on server
------------

The  most natural choice of path to the Certificate Authority folder is `/var/lib/rabbitmq/RabbitCA`

Notice that the file `server/cert.pem` is the __public__ certificate. (We will later import it into the
`trustStore` and send out to clients.) At the same time, `server/key.pem` is the __private__ key. As
it is needed at runtime, put its permissions to `640` and ownership to `root:rabbitmq`.

A nice general introduction to the `OpenSSL` Certificate Authority 
is [here](https://jamielinux.com/docs/openssl-certificate-authority/).


Email the server certificate to Alice and Bob
--------------------------------------------

The folder ``server/`` contains a file `cert.pem`. Execute the following command:

    keytool -import -alias server1 -file cert.pem -keystore trustStore

You will be asked for a passphrase. Choose some passphrase and give it to Alice and Bob. You should
also send them the newly created file `trustStore`.



Configuring RabbitMQ
====================

Location of config file
-----------------------

`/etc/rabbitmq/rabbitmq.conf` :
 
    listeners.ssl.default = 5671

    ssl_options.cacertfile = /var/lib/rabbitmq/RabbitCA/testca/cacert.pem
    ssl_options.certfile = /var/lib/rabbitmq/RabbitCA/server/cert.pem
    ssl_options.keyfile = /var/lib/rabbitmq/RabbitCA/server/key.pem
    ssl_options.verify = verify_peer
    ssl_options.fail_if_no_peer_cert = false



Registering users
=================

To add users `alice` and `bob` and allow them to talk over the vhost ("Virtual Host") `ourtheorem` :

    rabbitmqctl add_vhost ourtheorem
    rabbitmqctl add_user alice sarrpuOSX1Nm 
    rabbitmqctl set_permissions -p ourtheorem alice '^(alice|bob)$' '^amq\.default$' '^(alice|bob)$'
    rabbitmqctl add_user bob eij3yataeJoh
    rabbitmqctl set_permissions -p ourtheorem bob '^(alice|bob)$' '^amq\.default$' '^(alice|bob)$'

where `sarrpuOSX1Nm` and `eij3yataeJoh` are their passwords. Notice that `ourtheorem` is the name of the Virtual Host,
which should match the line `<vhost>ourtheorem</vhost>` in the client configuration files `conf.xml` for both Alice
and Bob. 

Also notice that `amq.default` is the name reserved in RabbitMQ for the default exchange. 
See [the manual](https://www.rabbitmq.com/access-control.html) for details about the access control structure.

See also [the manual for rabbitmqctl](https://www.rabbitmq.com/man/rabbitmqctl.1.man.html).

Web console
===========

I want one of the users to be the administrator for the purpose of Web Console
management. Let it be Alice:

    rabbitmqctl set_user_tags alice administrator

Then I want to enable the Web Console plugin:

    rabbitmq-plugins enable rabbitmq_management

Moreover, I want to run the Web Console under SSL. For that, 
I have to edit the file ``/etc/rabbitmq/rabbitmq.conf`` and add the following lines:

    management.listener.port = 15671
    management.listener.ssl = true
    management.listener.ssl_opts.cacertfile = /var/lib/rabbitmq/RabbitCA/testca/cacert.pem
    management.listener.ssl_opts.certfile = /var/lib/rabbitmq/RabbitCA/server/cert.pem
    management.listener.ssl_opts.keyfile = /var/lib/rabbitmq/RabbitCA/server/key.pem

Troubleshooting
===============

Log files are in `/var/log/rabbitmq`

