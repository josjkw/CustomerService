package endpoints

import cats.effect.kernel.Concurrent
import cats.implicits.{toFlatMapOps, toFunctorOps}
import io.circe.syntax._
import models.CustomersInterpreter
import models.Domain.Customer._
import models.Domain.{Customer, CustomerId}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import repo.Repository.CustomerRepository

object CustomerRoutes {

  def customerRoutes[F[_]](repository: CustomerRepository)(implicit F: Concurrent[F]): HttpRoutes[F] = {
    val dsl         = new Http4sDsl[F] {}
    val interpreter = new CustomersInterpreter(repository)

    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "customer" / id =>
        interpreter.get(CustomerId(id)) match {
          case Some(customer) => Ok(customer.asJson)
          case None           => NotFound()
        }
      case req @ POST -> Root / "customer" =>
        req.decodeJson[Customer].map(c => interpreter.create(c)).flatMap(_ => Ok())

      case req @ POST -> Root / "customers" =>
        req.decodeJson[Set[Customer]].map(customers => interpreter.createBatch(customers)).flatMap(_ => Ok())

    }
  }

}
