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

  private def handleRepositoryFibers(
      res: Outcome[F, Throwable, Option[CustomerDetails]],
      fiber: Fiber[F, Throwable, Option[CustomerDetails]],
  ): F[Option[CustomerDetails]] = res match {
    case Outcome.Succeeded(firstRepoDetails) =>
      firstRepoDetails.flatMap {
        case Some(_) => firstRepoDetails
        case None =>
          fiber.join.flatMap {
            case Outcome.Succeeded(secondRepoDetails) => secondRepoDetails
            case Outcome.Errored(e)                   => Async[F].pure(println(s"Error with fiber $fiber: $e")) *> Async[F].pure(None)
            case Outcome.Canceled()                   => Async[F].pure(None)
          }: F[Option[CustomerDetails]]
      }
    case Outcome.Errored(e) => Async[F].pure(println(s"Error with fiber $fiber: $e")) *> Async[F].pure(None)
    case Outcome.Canceled() => Async[F].pure(None)
  }

  override def getDetails(id: String): OptionT[F, CustomerDetails] = {

    val customerDetails = httpClient
      .expectOption[CustomerDetailsLegacyExternalApiOutput](Request[F](GET, root / id))
      .map(_.transformInto[Option[CustomerDetails]])
    val customerDetailsLegacy = httpClient
      .expectOption[CustomerDetailsExternalApiOutput](Request[F](GET, rootLegacyRepository / id))
      .map(_.transformInto[Option[CustomerDetails]])

    OptionT(for {
      race <- Async[F].racePair(customerDetails, customerDetailsLegacy)
      maybeCustomerDetails <- race match {
        case Left((resLegacy, fiber)) =>
          handleRepositoryFibers(resLegacy, fiber)
        case Right((fiber, res)) =>
          handleRepositoryFibers(res, fiber)
      }

    } yield maybeCustomerDetails)
  }

}
