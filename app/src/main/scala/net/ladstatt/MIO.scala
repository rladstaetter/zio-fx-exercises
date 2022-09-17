package net.ladstatt

object MIO {

  /** first, run self, after taht, run that, and finally combine both results in a new MIO applying given function */
  def zipWith[R, E, A, B, C](self: MIO[R, E, A],
                             that: MIO[R, E, B])(f: (A, B) => C): MIO[R, E, C] =
    MIO(r => self.run(r).flatMap {
      a => that.run(r).map(b => f(a, b))
    })

  def attempt[A](a: => A): MIO[Any, Throwable, A] =
    MIO(_ => try Right(a) catch {
      case t: Throwable => Left(t)
    })

  def fail[E](e: => E): MIO[Any, E, Nothing] =
    MIO(_ => Left(e))
}

final case class MIO[-R, +E, +A](run: R => Either[E, A]) {
  self =>

  def map[B](f: A => B): MIO[R, E, B] =
    MIO(r => self.run(r).map(f))

  def flatMap[R1 <: R, E1 >: E, B](f: A => MIO[R1, E1, B]): MIO[R1, E1, B] =
    MIO(r => self.run(r).fold(MIO.fail(_), f).run(r))

}

object MIOTest {

  val one = MIO.attempt(1)
  val two = MIO.attempt(2)
  val three = MIO.zipWith(one, two)((a, b) => s"$a $b")

}