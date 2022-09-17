package net.ladstatt


import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import zio.{Task, ZIO}

object ZioFxApp {

  def main(args: Array[String]): Unit = {
    javafx.application.Application.launch(classOf[ZioFxApp], args: _*)
  }

}


class ZioFxApp extends javafx.application.Application {

  /** zio runtime to help execute ZIO app */
  lazy val zioRt = zio.Runtime.default

  def start(stage: Stage): Unit = {
    val button = new Button("Read File with ZIO!")

    /** handle button click */
    button.setOnAction(e => {
      zio.Unsafe.unsafe { implicit unsafe =>
        zioRt.unsafe.run(ZioOps.readFileWithInput).getOrThrowFiberFailure()
      }
    })
    val bp = new BorderPane(button)
    val scene = new Scene(bp, 400, 400)
    stage.setScene(scene)
    stage.show()
  }


}

object ZioOps {

  val readFileWithInput: Task[Unit] =
    for {fileName <- readLine()
         cnt <- readFile(fileName)
         _ <- printLine(cnt)} yield ()

  def printLine(s: String): Task[Unit] = ZIO.attempt(println(s))

  def readLine(): Task[String] = ZIO.attempt("pom.xml")

  def readFile(file: String): Task[String] = ZIO.attempt(Ops.readFile(file))

}

object Ops {

  def readFile(file: String): String = {
    val source = scala.io.Source.fromFile(file)
    try source.getLines().mkString("\n") finally source.close()
  }

}