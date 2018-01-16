package diffpump

import javax.sound.sampled.{AudioSystem, Clip, DataLine}

import akka.actor.Actor

class Beeper extends Actor {
  def play (x: String) : Unit = {
    val url = getClass.getResource(x)
    val audioIn = AudioSystem.getAudioInputStream(url)
    val info = new DataLine.Info(classOf[Clip], audioIn.getFormat())
    val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
    clip.open(audioIn)
    clip.start

  }
  override def receive: Receive = {
    case BeepFileOut => play("/ton.wav")
    case BeepPatchOut => play("/drum-1.5.wav")
    case BeepReceipt => play("/drum-1.wav")
  }

}
