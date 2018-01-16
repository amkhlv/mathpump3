package diffpump

import java.io._
import java.nio.charset.StandardCharsets
import java.security._
import java.util
import javax.net.ssl._

import com.rabbitmq.client._
import org.apache.log4j.{Logger, PropertyConfigurator}

import scala.language.postfixOps

class PostOffice {
  val logger: Logger = Logger.getLogger("POSTOFFICE")
  PropertyConfigurator.configure("log4j.properties")
  val connection: Connection = {
    val factory: com.rabbitmq.client.ConnectionFactory = new ConnectionFactory()
    factory.setHost(rabbitURL)
    factory.setUsername(myName)
    factory.setPassword(myPassword)
    factory.setPort(rabbitPort)
    factory.setVirtualHost(vhost)
    factory.setConnectionTimeout(7200)
    factory.setRequestedHeartbeat(30)
    if (rabbitVerifyCertificates) {
      //Truststore:
      val tks = KeyStore.getInstance("JKS")
      tks.load(new FileInputStream(trustStore), trustPassphrase.toCharArray)
      val tmf = TrustManagerFactory.getInstance("SunX509")
      tmf.init(tks)

      val c = SSLContext.getInstance("TLSv1.2")
      c.init(null, tmf.getTrustManagers, null)

      factory.useSslProtocol(c)
    } else factory.useSslProtocol()
    factory.newConnection()
  }

  val inChannel: Channel = connection.createChannel()
  //inChannel.exchangeDeclare("DiffPump Exchange", "direct", true)
  inChannel.queueDeclare(myName, false, false, false, null)
  //inChannel.queueBind(myName, "", myName)
  val consumer: DefaultConsumer = new DefaultConsumer(inChannel) {
    override def handleDelivery(consumerTag: String,
                                envelope: Envelope,
                                properties: AMQP.BasicProperties,
                                body: Array[Byte]
                               ): Unit = {
      val hdrs: util.Map[String, AnyRef] = properties.getHeaders
      hdrs.get("type").toString match {
        case "PatchReceipt" =>
          logger.info("got receipt for patch")
          dispatcher ! ParcelPatchReceipt(
            properties.getUserId,
            hdrs.get("filename").toString,
            hdrs.get("ocs").toString,
            hdrs.get("ncs").toString
          )
        case "FileReceipt" =>
          logger.info("got receipt for file")
          dispatcher ! ParcelFileReceipt(
            properties.getUserId,
            hdrs.get("filename").toString,
            hdrs.get("cs").toString
          )
        case "Patch" =>
          logger.info("got patch")
          patcher ! ParcelPatch(
            new String(body, StandardCharsets.UTF_8),
            properties.getUserId,
            hdrs.get("filename").toString)
        case "PatchError" =>
          logger.info("got patching error notification (DMP of -->" + properties.getUserId +
            "<-- was unable to apply patch)")
          dispatcher ! ParcelPatchError(properties.getUserId, hdrs.get("filename").toString)
        case "File"  =>
          logger.info("got file")
          patcher ! ParcelFile(body, properties.getUserId, hdrs.get("filename").toString)
      }
    }
  }
  inChannel.basicConsume(myName, true, consumer)
  val outChannel: Map[String, com.rabbitmq.client.Channel] = Map((
    for (nm <- myName :: (them.keys toList)) yield {
      logger.info("creating connection for " + nm)
      val ch = connection.createChannel(); ch.queueDeclare(nm, false, false, false, null)
      nm -> ch
    }
    ): _*)

  def broadcast(recipients: List[String], obj: Parcel): Unit = {
    val diagnosticString = obj match {
      case y: ParcelFile => "ParcelTextFile, filename:" + y.filename
      case y: ParcelPatch => "ParcelPatch, filename:" + y.filename
      case y: ParcelFileReceipt => "ParcelFileReceipt, filename:" + y.filename
      case y: ParcelPatchReceipt => "ParcelPatchReceipt, filename:" + y.filename
      case y: ParcelPatchError => "ParcelPatchError, filename:"   + y.filename
      case StopDelivery => "Stop"
      case u => "SOMETHING STRANGE"
    }
    var hdrs = new java.util.HashMap[java.lang.String, AnyRef]
    obj match {
      case y: ParcelPatchReceipt =>
        hdrs.put("type", "PatchReceipt".asInstanceOf[AnyRef])
        hdrs.put("filename", y.filename.asInstanceOf[AnyRef])
        hdrs.put("ocs", y.oldSHA1.asInstanceOf[AnyRef])
        hdrs.put("ncs", y.newSHA1.asInstanceOf[AnyRef])
      case y: ParcelFileReceipt =>
        hdrs.put("type", "FileReceipt".asInstanceOf[AnyRef])
        hdrs.put("filename", y.filename.asInstanceOf[AnyRef])
        hdrs.put("cs", y.SHA1.asInstanceOf[AnyRef])
      case y: ParcelPatch =>
        hdrs.put("type", "Patch".asInstanceOf[AnyRef])
        hdrs.put("filename", y.filename.asInstanceOf[AnyRef])
      case y: ParcelPatchError =>
        hdrs.put("type", "PatchError".asInstanceOf[AnyRef])
        hdrs.put("filename", y.filename.asInstanceOf[AnyRef])
      case y: ParcelFile =>
        hdrs.put("type", "File".asInstanceOf[AnyRef])
        hdrs.put("filename", y.filename.asInstanceOf[AnyRef])
      case StopDelivery =>
        hdrs.put("type", "Stop".asInstanceOf[AnyRef])
    }
    val props = new AMQP.BasicProperties.Builder().userId(myName).headers(hdrs).build()
    for (name <- recipients) {
      val body : Array[Byte] = obj match {
        case y: ParcelPatch => y.patch.getBytes(StandardCharsets.UTF_8)
        case y: ParcelFile  => y.cont
        case _ =>  new Array[Byte](0)
      }
      outChannel(name).basicPublish("", name, props, body)
      logger.info(diagnosticString + " --> Sent to: " + name)
    } ;
    ()
  }
  def close(): Boolean = {
    var result = true
    try {
      inChannel.close()
    } catch {
      case ex: com.rabbitmq.client.AlreadyClosedException =>
        result = false
        println("ERROR: lost inChannel")
        logger.error("lost inChannel")
    }
    for (nm <- myName :: (them.keys toList)) {
      try {
        outChannel(nm).close()
      } catch {
        case ex: com.rabbitmq.client.AlreadyClosedException =>
          result = false
          println("ERROR: lost outChannel")
          logger.error("lost outChannel")
      }
    }
    try {
      connection.close()
    } catch {
      case ex: com.rabbitmq.client.AlreadyClosedException =>
        result = false
        println("ERROR: lost connection")
        logger.error("lost connection")
    }
    result
  }

}
