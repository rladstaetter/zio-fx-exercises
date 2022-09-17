package net.ladstatt

object ZioFxAppLauncher {

  /** launcher for macos installer to circumvent module loading mechanism by javafx (dirty hack) */
  def main(args: Array[String]): Unit = {
    ZioFxApp.main(args)
  }

}
