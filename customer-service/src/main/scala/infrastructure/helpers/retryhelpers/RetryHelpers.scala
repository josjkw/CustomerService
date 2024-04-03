package infrastructure.helpers.retryhelpers

import cats.effect.kernel.Async
import cats.implicits._

import scala.concurrent.duration.Duration

object RetryHelpers {

  def retryOnAllErrors[A, F[_]: Async](action: F[A], delay: Duration, retries: Int): F[A] = {
    action.attempt.flatMap {
      case Left(_) if retries > 0 => Async[F].sleep(delay) *> retryOnAllErrors(action, delay, retries - 1)
      case Left(e)                => Async[F].raiseError(e)
      case Right(res)             => Async[F].pure(res)
    }

  }

}
