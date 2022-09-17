package net.ladstatt

import java.io.{File, PrintWriter}

object Ops {


  def readFile(file: File): String = {
    val source = scala.io.Source.fromFile(file)
    try source.getLines().mkString("\n") finally source.close()
  }

  def writeFile(file: File, text: String): Unit = {
    val pw = new PrintWriter(file)
    try pw.write(text) finally pw.close()
  }
}
