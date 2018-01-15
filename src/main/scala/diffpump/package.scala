import java.io.File

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import scala.util.Random
import scala.util.matching.Regex


package object diffpump {
  type UserName = String
  type FileName = String
  type CheckSum = String

  val config = ConfigFactory.load()
  val myName = config.getString("me.name")
  val myPassword = config.getString("me.password")
  val outDirName = config.getString("me.dir")
  val ignoredFilenamePatterns : List[Regex] = config.getStringList("me.ignore").asScala.map(x => x.r).toList
  case class PersonConfig(dir: String, width: Int, height: Int)
  val them: Map[String, PersonConfig] = Map((for (c <- config.getConfigList("them").asScala) yield {
    val (nm: String , dir: String, width: Int, height: Int) = c match {
      case conf: Config => (
        conf.getString("name"),
        conf.getString("dir"),
        conf.getInt("width"),
        conf.getInt("height")
        )
    }
    new File(dir + "/tmp").mkdirs()
    (nm -> PersonConfig(dir = dir, width = width, height = height))
  }):_*)
  val rabbitURL = config.getString("rabbitURL")
  val rabbitPort = config.getInt("rabbitPort")
  val vhost = config.getString("vhost")
  val rabbitVerifyCertificates = config.getBoolean("rabbitVerifyCertificates")
  val trustStore: String = config.getString("trustStore")
  val trustPassphrase: String = config.getString("trustStorePassphrase")

  val alphabet = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
  val stopWatcherFileName = (1 to 8).map(_ => alphabet(Random.nextInt(alphabet.size))).mkString ++ ".eraseme"
  new File("tmp/stop").mkdirs()
  new File("tmp/stop/" + stopWatcherFileName).createNewFile()
  val customConf = ConfigFactory.parseString("akka { log-dead-letters-during-shutdown = off } ")
  val system = ActorSystem("MathPump", customConf)
  val delivery = new PostOffice()
  val situation = new Situation
  var prevWatcherEventTime: DateTime = new DateTime()
  val dispatcher : ActorRef = system.actorOf(Props(new Central(delivery, situation)), name = "dispatcher")
  val patcher : ActorRef = system.actorOf(Props(new Patcher(dispatcher)), name = "patcher")
}
