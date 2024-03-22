package infrastructure.repositories

import cats.data.OptionT
import cats.effect.Async
import cats.implicits.toFunctorOps
import domain.CustomerDetails
import domain.repositories.CustomerDetailsRepository
import infrastructure.config.Configs.CustomerDetailsServiceConfig
import io.circe.generic.auto._
import io.scalaland.chimney.dsl.TransformerOps
import org.http4s.Method.GET
import org.http4s.{Request, Uri}
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.client.Client

final case class CustomerDetailsLegacyExternalApiOutput(data: String)

class CustomerDetailsLegacyHttpRepository[F[_]: Async](config: CustomerDetailsServiceConfig, httpClient: Client[F])
    extends CustomerDetailsRepository[F] {

  private val root = Uri.unsafeFromString(s"http://${config.host}:${config.port}/internal/legacy")

  override def getDetails(id: String): OptionT[F, CustomerDetails] =
    OptionT(
      httpClient
        .expectOption[CustomerDetailsLegacyExternalApiOutput](Request[F](GET, root / id))
        .map(_.transformInto[Option[CustomerDetails]])
    )

}
