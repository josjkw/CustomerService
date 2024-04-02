package infrastructure.repositories

import cats.data.OptionT
import cats.effect.Async
import cats.effect.kernel.{Fiber, Outcome}
import cats.implicits._
import domain.CustomerDetails
import domain.repositories.CustomerDetailsRepository
import infrastructure.config.Configs.CustomerDetailsServiceConfig
import io.circe.generic.auto._
import io.scalaland.chimney.dsl.TransformerOps
import org.http4s.Method.GET
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.{Request, Uri}
import retry._
import retry.retryops.RetryAndDelay

import scala.concurrent.duration.FiniteDuration

final case class CustomerDetailsExternalApiOutput(data: String)
final case class CustomerDetailsLegacyExternalApiOutput(data: String)

class CustomerDetailsHttpRepository[F[_]: Async](config: CustomerDetailsServiceConfig, httpClient: Client[F])
    extends CustomerDetailsRepository[F] {

  private val root = Uri.unsafeFromString(
    s"http://${config.customerDetailsConfig.host}:${config.customerDetailsConfig.port}/internal"
  )

  private val rootLegacyRepository = Uri.unsafeFromString(
    s"http://${config.customerDetailsLegacyConfig.host}:${config.customerDetailsLegacyConfig.port}/internal/legacy"
  )

  // can be put in config
  private val legacyRepositoryRetryPolicy = RetryAndDelay(3, FiniteDuration(5, "second"))

  private def handleRepositoryFibers(
      res: Outcome[F, Throwable, Option[CustomerDetails]],
      fiber: Fiber[F, Throwable, Option[CustomerDetails]],
  ): F[Option[CustomerDetails]] = res match {
    case Outcome.Succeeded(firstRepoDetails) =>
      firstRepoDetails.flatMap {
        case Some(_) => fiber.cancel *> firstRepoDetails: F[Option[CustomerDetails]]
        case None =>
          fiber.join.flatMap {
            case Outcome.Succeeded(secondRepoDetails) => secondRepoDetails
            case Outcome.Errored(e) =>
              Async[F].raiseError(new RuntimeException(s"Unexpected runtime error with with fiber $fiber: $e"))
            case Outcome.Canceled() =>
              Async[F].raiseError(
                new IllegalStateException(
                  "Cancellation during requesting customer details - should never be cancelled other that manually"
                )
              )
          }: F[Option[CustomerDetails]]
      }
    case Outcome.Errored(e) =>
      fiber.cancel *> Async[F].raiseError(
        new RuntimeException(s"Unexpected runtime error with with fiber $fiber: $e")
      )
    case Outcome.Canceled() =>
      Async[F].raiseError(
        new IllegalStateException(
          "Cancellation during requesting customer details - should never be cancelled other that manually"
        )
      )
  }

  override def getDetails(id: String): OptionT[F, CustomerDetails] = {

    val customerDetails = httpClient
      .expectOption[CustomerDetailsLegacyExternalApiOutput](Request[F](GET, root / id))
      .map(_.transformInto[Option[CustomerDetails]])

    val customerDetailsLegacy = httpClient
      .expect[CustomerDetailsExternalApiOutput](Request[F](GET, rootLegacyRepository / id))
      .map(_.transformInto[Option[CustomerDetails]])

    val customerDetailsLegacyRetry =
      retryingOnAllErrors(customerDetailsLegacy, legacyRepositoryRetryPolicy).handleError(_ => None)

    OptionT(for {
      race <- Async[F].racePair(customerDetails, customerDetailsLegacyRetry)
      maybeCustomerDetails <- race match {
        case Left((resLegacy, fiber)) =>
          handleRepositoryFibers(resLegacy, fiber)
        case Right((fiber, res)) =>
          handleRepositoryFibers(res, fiber)
      }

    } yield maybeCustomerDetails)
  }

}
