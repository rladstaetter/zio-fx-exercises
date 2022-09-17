package net.ladstatt


import javafx.scene.Scene
import javafx.scene.control.{Button, TextArea, ToolBar}
import javafx.scene.layout.VBox
import javafx.stage.Stage

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








