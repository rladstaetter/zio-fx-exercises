package net.ladstatt


import javafx.beans.property.{Property, StringProperty}
import javafx.scene.Scene
import javafx.scene.control.{Button, TextArea, ToolBar}
import javafx.scene.layout.VBox
import javafx.stage.{FileChooser, Stage}
import zio.{Task, ZIO}

import java.io.{File, PrintWriter}

object ZioFxApp {

  def main(args: Array[String]): Unit = {
    javafx.application.Application.launch(classOf[ZioFxApp], args: _*)
  }

}


class ZioFxApp extends javafx.application.Application {

  /** zio runtime to help execute ZIO app */
  lazy val zioRt: zio.Runtime[Any] = zio.Runtime.default

  def start(stage: Stage): Unit = {
    val b1 = new Button("Start number guessing")
    val toolBar = new ToolBar(b1)
    val textArea = new TextArea()
    val vBox = new VBox(toolBar, textArea)

    b1.setOnAction(_ => ZioOps.runUnsafeTask(zioRt, ZioOps.numberGuessing))

    val scene = new Scene(vBox, 350, 100)
    stage.setScene(scene)
    stage.setTitle("zionomicron exercises")
    stage.show()
  }


}

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