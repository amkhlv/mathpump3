package diffpump

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.nio.file.Files._
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest

import scala.util.matching.Regex

object Utils {
  val separator = sys.props("line.separator")
  private val sha1digester = MessageDigest.getInstance("SHA-1")
  def sha1utf8(x: String) = String.format(
    "%032x",
    new BigInteger(1, sha1digester.digest(x.getBytes(StandardCharsets.UTF_8)))
  )
  def sha1byte(x: Array[Byte]) = String.format("%032x", new BigInteger(1, sha1digester.digest(x)))
  def fileAtPathExists(path: Path): Boolean =
    Files.exists(path)

  def copyPath(orig:Path, dest:Path): Path = copy(orig,dest)

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  def writeUnicodeStringToFilePath(path: Path, data: String) = Files.write(path, data.getBytes(StandardCharsets.UTF_8))
    // printToFile(path.toFile())(p => p.println(data))

  def readUnicodeStringFromFilePath(path: Path) : String = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
  //def readFromFilePath(filePath: Path): String =  Source.fromFile(filePath.toFile).mkString

  def resourceFile(x: String): java.io.File = Paths.get(getClass.getResource(x).getPath).toFile

  def notIgnored(x: String): Boolean = ignoredFilenamePatterns.toList.map {
            case r: Regex => r.findFirstMatchIn(x) match {
              case Some(y) => false
              case None => true
            }}.fold(true)((a,b) => a && b)

}
