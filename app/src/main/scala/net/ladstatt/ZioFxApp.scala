package net.ladstatt


import javafx.beans.property.{Property, SimpleObjectProperty, StringProperty}
import javafx.scene.Scene
import javafx.scene.control.{Button, TextArea, ToolBar}
import javafx.scene.layout.{HBox, VBox}
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

  val srcFileProperty = new SimpleObjectProperty[File]()
  val destFileProperty = new SimpleObjectProperty[File]()

  def start(stage: Stage): Unit = {
    val srcFileChooser = new FileChooser()
    srcFileChooser.setTitle("Choose source file")
    val destFileChooser = new FileChooser()
    destFileChooser.setTitle("Choose target file")
    val srcButton = new Button("Choose source file")
    val destButton = new Button("Choose destination file")
    val copyButton = new Button("copy")
    val textArea = new TextArea()
    val toolBar = new ToolBar(srcButton, destButton, copyButton)
    val vBox = new VBox(toolBar, textArea)

    srcButton.setOnAction(_ =>
      ZioOps.runUnsafeTask(zioRt, for {f <- ZioOps.showOpenFileChooser(srcFileChooser)
                                       _ <- ZioOps.updateProp(srcFileProperty, f)} yield ())
    )
    destButton.setOnAction(_ =>
      ZioOps.runUnsafeTask(zioRt, for {f <- ZioOps.showSaveFileChooser(destFileChooser)
                                       _ <- ZioOps.updateProp(destFileProperty, f)} yield ())
    )
    copyButton.setOnAction(_ => {
      ZioOps.runUnsafeTask(zioRt, for {dest <- ZioOps.copyFile(srcFileProperty.get(), destFileProperty.get())
                                       _ <- ZioOps.setStringProperty(textArea.textProperty(), s"Wrote ${dest.toString}")} yield ())
    })

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