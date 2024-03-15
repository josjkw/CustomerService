package gateway

import cats.effect.kernel.Concurrent
import cats.implicits.{toFlatMapOps, toFunctorOps}
import domain.{Customer, CustomerId, CustomerService}
import gateway.transformers.CustomerTransformers._
import gateway.models.CustomerApi.{CustomerApiInput, CustomerApiOutput}
import gateway.serialization.CustomerCodecs.{customerApiInput, customerApiOutput}
import io.circe.syntax._
import io.scalaland.chimney.dsl.TransformerOps
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object CustomerRoutes {

  def customerRoutes[F[_]](customerService: CustomerService[F])(implicit F: Concurrent[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "health" => Ok()
      case GET -> Root / "customer" / id =>
        customerService.get(CustomerId(id)).map(_.transformInto[Option[CustomerApiOutput]]).flatMap {
          case Some(customer) => Ok(customer.asJson)
          case None           => NotFound("Customer not found")
        }
      case req @ POST -> Root / "customer" =>
        req
          .decodeJson[CustomerApiInput]
          .map(_.transformInto[Customer])
          .flatMap(c => customerService.create(c))
          .flatMap(_ => Ok())

      case req @ POST -> Root / "customers" =>
        req
          .decodeJson[Set[CustomerApiInput]]
          .map(_.transformInto[Set[Customer]])
          .map(customers => customerService.createBatch(customers))
          .flatMap(_ => Ok())

    }
  }

}
