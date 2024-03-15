import cats.effect._
import domain.CustomerService
import gateway.CustomerRoutes.customerRoutes
import infrastructure.repositories.InMemoryCustomerRepository
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.implicits._

object Server extends IOApp {
  private val customerRepo = new InMemoryCustomerRepository
  val customerService      = new CustomerService[IO](customerRepo)

  private val httpApp: HttpApp[IO] = Router(
    "/api" -> customerRoutes[IO](customerService)
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO].bindHttp(6969, "localhost").withHttpApp(httpApp).serve.compile.drain.as(ExitCode.Success)

}
