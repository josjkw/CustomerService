package retry.retryops

import scala.concurrent.duration.FiniteDuration

sealed trait RetryPolicy {

  val updatePolicy: RetryPolicy = this match {
    case RetryAndDelay(retries, delay) if retries > 0 => RetryAndDelay(retries = retries - 1, delay = delay)
    case _: RetryAndDelay                             => GiveUp
    case GiveUp                                       => GiveUp
  }

}

final case class RetryAndDelay(retries: Int, delay: FiniteDuration) extends RetryPolicy
case object GiveUp                                                  extends RetryPolicy
