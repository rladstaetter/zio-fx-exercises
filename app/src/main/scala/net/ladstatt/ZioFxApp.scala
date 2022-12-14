package net.ladstatt


import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import zio.ZIO

object ZioFxApp {

  def main(args: Array[String]): Unit = {
    javafx.application.Application.launch(classOf[ZioFxApp], args: _*)
  }

}


class ZioFxApp extends javafx.application.Application  {

  /** zio runtime to help execute ZIO app */
  lazy val zioRt = zio.Runtime.default

  def start(stage: Stage): Unit = {
    val button = new Button("Invoke ZIO!")

    /** handle button click */
    button.setOnAction(e => {
      zio.Unsafe.unsafe { implicit unsafe =>
        zioRt.unsafe.run(ZIO.attempt(println("Hello World!"))).getOrThrowFiberFailure()
      }
    })
    val bp = new BorderPane(button)
    val scene = new Scene(bp, 400, 400)
    stage.setScene(scene)
    stage.show()
  }

}

