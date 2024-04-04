package infrastructure.repositories

import cats.data.OptionT
import cats.effect.Async
import cats.effect.kernel.{Fiber, Outcome}
import cats.implicits._
import domain.CustomerDetails
import domain.repositories.CustomerDetailsRepository
import infrastructure.config.Configs.CustomerDetailsServiceConfig
import infrastructure.helpers.retryhelpers.RetryHelpers
import io.circe.generic.auto._
import io.scalaland.chimney.dsl.TransformerOps
import org.http4s.Method.GET
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.{Request, Uri}

import scala.concurrent.duration.DurationInt

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

  private def getCustomerDetailsFromElasticSearch: F[Option[CustomerDetails]] =
    Async[F].sleep(60.millis) *> Async[F].pure(None)

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
      RetryHelpers.retryOnAllErrors(customerDetailsLegacy, 2.seconds, 4).handleError(_ => None)

    OptionT(for {
      // I thought it was affordable to have the elasticSearch query ran first and then only run the other
      // two repos. First because it's fast and the hit on elastic search should be good with 100k most actives users
      // (of course depends on the total number of customers I guess) and also to avoid to have fiber spawn and canceled everywhere
      // with likely useless calls
      maybeCustomerDetails <- getCustomerDetailsFromElasticSearch.flatMap {
        // for some reason compilation was broken with Some(customerDetails)
        // found   : Option[domain.CustomerDetails] => F[_ >: Some[domain.CustomerDetails] <: Option[domain.CustomerDetails]]
        // [error]  required: Option[domain.CustomerDetails] => F[Option[domain.CustomerDetails]]
        case Some(customerDetails) => Async[F].pure(Option(customerDetails))
        case None =>
          for {
            race <- Async[F].racePair(customerDetails, customerDetailsLegacyRetry)
            maybeCustomerDetails <- race match {
              case Left((resLegacy, fiber)) =>
                handleRepositoryFibers(resLegacy, fiber)
              case Right((fiber, res)) =>
                handleRepositoryFibers(res, fiber)
            }
          } yield maybeCustomerDetails
      }

    } yield maybeCustomerDetails)
  }

}
