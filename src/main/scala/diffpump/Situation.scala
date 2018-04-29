package diffpump

import java.nio.file.{Files, Paths}

import org.apache.log4j.Logger

import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}

case class RemoteFileState(wholeFileAckPending : Boolean, checksums : ListBuffer[CheckSum])

class Situation {
  val logger: Logger = Logger.getLogger("SITUATION")
  private var lcl : Map[FileName, Array[Byte]] = new mutable.HashMap[FileName, Array[Byte]]
  for (f <- Paths.get(outDirName).toFile.listFiles())
    lcl.put(f.getName, Files.readAllBytes(Paths.get(outDirName,f.getName)))

  private var rmt : Map[UserName, mutable.HashMap[FileName, RemoteFileState]]  =
    new mutable.HashMap[UserName, mutable.HashMap[FileName, RemoteFileState]]
  for (k <- them.keys) rmt.put(k, new mutable.HashMap[FileName, RemoteFileState])

  private def newRFS(ackPending: Boolean) = RemoteFileState(ackPending, new ListBuffer[CheckSum])

  private def allRemoteFiles(u: UserName) : mutable.HashMap[FileName, RemoteFileState] =
    rmt.getOrElseUpdate(u, new mutable.HashMap[FileName, RemoteFileState])

  private def specificRemoteFile(u: UserName, fn: FileName) : RemoteFileState =
    allRemoteFiles(u).getOrElseUpdate(fn, newRFS(false))

  def getOldLocalContents(fn: FileName) : Array[Byte] =
    lcl.getOrElseUpdate(fn, Files.readAllBytes(Paths.get(outDirName + "/" +fn)))

  def setNewLocalContents(fn: FileName, bs: Array[Byte]) : Unit = lcl.put(fn, bs)

  def setWholeFileAckPending(u: UserName, fn: FileName, wholeFileAckIsPending: Boolean) : Unit = {
    val r = RemoteFileState(  // have to rebuild the RemoteFileState
      wholeFileAckIsPending,
      rmt(u).get(fn) match { case Some(xm) => xm.checksums case None => new ListBuffer[CheckSum]() }
    )
    rmt(u).put(fn,r)
  }

  private def regRF(u: UserName, fn: FileName, cs: CheckSum, isPending: Boolean) : Unit =
    allRemoteFiles(u).update(fn, RemoteFileState(isPending, new ListBuffer[CheckSum]().+=:(cs)))
  def registerRemoteFile(u: UserName, fn: FileName, cs: CheckSum) : Unit = regRF(u, fn, cs, false)
  def registerPendingRemoteFile(u: UserName, fn: FileName, cs: CheckSum) : Unit = regRF(u, fn, cs, true)

  def registerPendingPatch(u: UserName, fn: FileName, ncs: CheckSum) : Unit =
    specificRemoteFile(u, fn).checksums.+=:(ncs)

  def registerThatPatchWasApplied(u: UserName, fn: FileName) : Unit = {
    val rfs: mutable.Map[FileName, RemoteFileState] = allRemoteFiles(u)
    val rf: RemoteFileState = specificRemoteFile(u, fn)
    rfs.update(fn, RemoteFileState(false, rf.checksums.dropRight(1)))
  }

  def noRemoteFileYet(u: UserName, fn: FileName) : Boolean = specificRemoteFile(u,fn).checksums.length == 0

  def patchesArePending(u: UserName, fn: FileName) : Boolean = specificRemoteFile(u,fn).checksums.length > 1

  def wholeFileAckIsPending(u: UserName, fn: FileName) : Boolean = specificRemoteFile(u,fn).wholeFileAckPending

  def receiptIsOK(u: UserName, fn: FileName, ocs: CheckSum, ncs: CheckSum) : Boolean = {
    val css : ListBuffer[CheckSum] = specificRemoteFile(u, fn).checksums
    patchesArePending(u,fn) && (css.last == ocs) && (css.dropRight(1).last == ncs)
  }

  def printout : Unit = {
    for (u <- rmt.keys) {
      logger.info(" -" + u + ":")
      val fs = allRemoteFiles(u)
      for (f <- fs.keys) {
        val fstate = fs.getOrElse(f, newRFS(false))
        if (fstate.wholeFileAckPending)
          logger.info("    -" + f + " (ACK_PENDING):")
        else
          logger.info("    -" + f + ":")
        for (cs <- fstate.checksums) {
          logger.info("       -" + cs)
        }
      }
    }
  }

}
