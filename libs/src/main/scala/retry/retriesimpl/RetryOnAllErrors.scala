package retry.retriesimpl

import cats.MonadError
import cats.implicits.{catsSyntaxApply, toFlatMapOps, toFunctorOps}
import retry.retryops.{GiveUp, RetryAndDelay, RetryPolicy}
import retry.sleep.Sleep

class RetryOnAllErrors[A] {

  def apply[F[_], E](
      action: => F[A],
      retryPolicy: RetryPolicy,
  )(implicit monadError: MonadError[F, E], sleep: Sleep[F]): F[A] = {
    monadError.tailRecM(retryPolicy) { retryPolicy =>
      retryImpl(
        action,
        retryPolicy,
      )
    }
  }

  private def retryImpl[F[_], E](action: => F[A], retryPolicy: RetryPolicy)(implicit
      monadError: MonadError[F, E],
      temporal: Sleep[F],
  ): F[Either[RetryPolicy, A]] =
    monadError.attempt(action).flatMap {
      case Left(error) =>
        val updatedPolicy = retryPolicy.updatePolicy
        updatedPolicy match {
          case RetryAndDelay(_, delay) => temporal.sleep(delay) *> monadError.pure(Left(updatedPolicy))
          case GiveUp                  => monadError.raiseError[A](error).map(Right(_))
        }
      case Right(res) => monadError.pure(Right(res))
    }

}