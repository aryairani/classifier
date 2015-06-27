package tools

import java.io.File

case class PathHelp(path: File) {
  def p(file: String): File = new File(path, file)
  def ps(file: String): String = p(file).getAbsolutePath
}

object PathHelp {
  def fromString(s: String) = PathHelp(new File(s))
}
