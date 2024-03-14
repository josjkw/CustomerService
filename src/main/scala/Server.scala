import repo.Repository.InMemoryCustomerRepository
import cats.effect._
import endpoints.CustomerRoutes.customerRoutes
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.implicits._

object Server extends IOApp {
  private val customerRepo = new InMemoryCustomerRepository

  private val httpApp: HttpApp[IO] = Router(
    "/api" -> customerRoutes[IO](customerRepo)
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO].bindHttp(6969, "localhost").withHttpApp(httpApp).serve.compile.drain.as(ExitCode.Success)

}
