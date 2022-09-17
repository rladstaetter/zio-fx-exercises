package net.ladstatt


import javafx.beans.property.StringProperty
import javafx.scene.Scene
import javafx.scene.control.{Button, TextArea, TextField}
import javafx.scene.layout.{HBox, VBox}
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
    val textField = new TextField("pom.xml")
    val textArea = new TextArea()
    val hBox = new HBox(textField, button)
    val vBox = new VBox(hBox, textArea)

    /** handle button click */
    button.setOnAction(e => {
      zio.Unsafe.unsafe { implicit unsafe =>
        zioRt.unsafe.run(ZioOps.updateTextArea(textField,textArea)).getOrThrowFiberFailure()
      }
    })
    val scene = new Scene(vBox, 400, 200)
    stage.setScene(scene)
    stage.setTitle("zionomicron exercises")
    stage.show()
  }


}

object ZioOps {

  def updateTextArea(textField: TextField, textArea: TextArea): Task[Unit] =
    for {fileName <- ZioOps.readStringProperty(textField.textProperty())
         content <- ZioOps.readFile(fileName)
         _ <- ZioOps.setStringProperty(textArea.textProperty(), content)} yield ()
  def readStringProperty(stringProperty: StringProperty): Task[String] = ZIO.attempt(stringProperty.get())
  def setStringProperty(stringProperty: StringProperty, value: String): Task[Unit] = ZIO.attempt(stringProperty.set(value))
  def readFile(file: String): Task[String] = ZIO.attempt(Ops.readFile(file))

}

object Ops {

  def readFile(file: String): String = {
    val source = scala.io.Source.fromFile(file)
    try source.getLines().mkString("\n") finally source.close()
  }

}