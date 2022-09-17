package net.ladstatt

import javafx.beans.property.{Property, StringProperty}
import javafx.stage.FileChooser
import zio.{Task, ZIO}

import java.io.File

object ZioOps {

  def copyFile(srcFile: File, destFile: File): Task[File] = {
    for {cnt <- readFile(srcFile)
         _ <- writeFile(destFile, cnt)} yield destFile
  }

  def updateProp[T](p: Property[T], t: T): Task[Unit] = ZIO.attempt(p.setValue(t))

  def showOpenFileChooser(fc: FileChooser): Task[File] = ZIO.attempt(fc.showOpenDialog(null))

  def showSaveFileChooser(fc: FileChooser): Task[File] = ZIO.attempt(fc.showSaveDialog(null))

  def runUnsafeTask[T](implicit zioRt: zio.Runtime[Any], task: Task[T]): T = {
    zio.Unsafe.unsafe { implicit unsafe =>
      zioRt.unsafe.run(task).getOrThrowFiberFailure()
    }
  }

  def readStringProperty(stringProperty: StringProperty): Task[String] = ZIO.attempt(stringProperty.get())

  def setStringProperty(stringProperty: StringProperty, value: String): Task[Unit] = ZIO.attempt(stringProperty.set(value))

  def readFile(file: File): Task[String] = ZIO.attempt(Ops.readFile(file))

  def writeFile(path: File, content: String): Task[Unit] = ZIO.attempt(Ops.writeFile(path, content))

  def printLine(line: String) = ZIO.attempt(println(line))

  val readLine = ZIO.attempt(scala.io.StdIn.readLine())


  val printlnVariantA = printLine("What is your name?").flatMap(_ =>
    readLine.flatMap(name => printLine(s"Hello, ${name}!")))

  val printlnVariantB =
    for {_ <- printLine("What is your name?")
         name <- readLine
         _ <- printLine(s"Hello $name!")
         } yield ()

  val random = ZIO.attempt(scala.util.Random.nextInt(3) + 1)

  def guessEffect(i: String, r: Int): Task[Unit] = if (i == r.toString) {
    printLine("You guessed right!")
  } else {
    printLine(s"You guessed wrong, the number was $r!")
  }

  val numberGuessing: Task[Unit] = for {r <- random
                                        _ <- printLine("Guess a number from 1 to 3:")
                                        i <- readLine
                                        _ <- guessEffect(i, r)
                                        } yield ()

}
