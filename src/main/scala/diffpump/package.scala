import java.io._
import java.nio.charset.StandardCharsets

import org.apache.log4j.{Logger, PropertyConfigurator}
import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.sys.process.{Process, ProcessIO, _}
import scala.util.{Failure, Random, Success}
import scala.util.matching.Regex

package object diffpump {
  type UserName = String
  type FileName = String
  type CheckSum = String
  val logger: Logger = Logger.getLogger("PACKAGE OBJECT")
  PropertyConfigurator.configure("log4j.properties")

  val qmlFile = System.getenv("HOME") + "/.config/mathpump3/QML/svg-whiteboard.qml"

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
  val player: String = config.getString("beeper")

  val alphabet = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
  val stopWatcherFileName = (1 to 8).map(_ => alphabet(Random.nextInt(alphabet.size))).mkString ++ ".eraseme"
  new File("tmp/sound").mkdirs()
  new File("tmp/stop").mkdirs()
  new File("tmp/stop/" + stopWatcherFileName).createNewFile()
  val customConf = ConfigFactory.parseString("akka { log-dead-letters-during-shutdown = off } ")
  val system = ActorSystem("MathPump", customConf)
  val delivery = new PostOffice()
  val beeper : ActorRef = system.actorOf(Props(new Beeper()), name = "beeper")
  val situation = new Situation
  var prevWatcherEventTime: DateTime = new DateTime()
  val dispatcher : ActorRef = system.actorOf(Props(new Central(delivery, situation)), name = "dispatcher")
  println(dispatcher.path)

  val board : Map[UserName, ActorRef] = them.map{  case (u, pc) => (u, system.actorOf(Props(new WhiteBoard(u))))  }

  val home = System.getProperty("user.home")
  val runningWhiteBoards : mutable.HashMap[UserName, Process] = new mutable.HashMap()
  val patcher : ActorRef = system.actorOf(Props(new Patcher(dispatcher)), name = "patcher")
}
