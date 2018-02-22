Configuring sound in JAVA
=========================

Location of JAVA_HOME
---------------------

    JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")


Sound configuration
-------------------

Edit file `$JAVA_HOME/lib/sound_properties`

It offers the following choice:

    javax.sound.sampled.Clip=org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider
    javax.sound.sampled.Port=org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider
    javax.sound.sampled.SourceDataLine=org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider
    javax.sound.sampled.TargetDataLine=org.classpath.icedtea.pulseaudio.PulseAudioMixerProvider

    #javax.sound.sampled.Clip=com.sun.media.sound.DirectAudioDeviceProvider
    #javax.sound.sampled.Port=com.sun.media.sound.PortMixerProvider
    #javax.sound.sampled.SourceDataLine=com.sun.media.sound.DirectAudioDeviceProvider
    #javax.sound.sampled.TargetDataLine=com.sun.media.sound.DirectAudioDeviceProvider

Apparently `PulseAudio` may have problems with `Java`, and in such cases it is better to choose `DirectAudioDeviceProvider` (_i.e._ the commented lines)

