import cats.MonadError
import retry.retriesimpl.RetryOnAllErrors
import retry.retryops.RetryPolicy
import retry.sleep.Sleep

package object retry {
  def retryingOnAllErrors[A] = new RetryOnAllErrors[A]

  final class RetryingErrorOps[F[_], A, E](action: => F[A])(implicit
      M: MonadError[F, E]
  ) {

    def retryingOnAllErrors(
        policy: RetryPolicy
    )(implicit S: Sleep[F]): F[A] =
      retry.retryingOnAllErrors(
        action,
        policy,
      )

  }

}
