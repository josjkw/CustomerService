import cats.effect._
import cats.implicits.toBifunctorOps
import domain.CustomerService
import gateway.CustomerRoutes.customerRoutes
import infrastructure.repositories.InMemoryCustomerRepository
import infrastructure.repositories.config.ServerConf
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.implicits._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Server extends IOApp {

  private val customerRepo = new InMemoryCustomerRepository[IO]
  val customerService      = new CustomerService[IO](customerRepo)

  private val httpApp: HttpApp[IO] = Router(
    "/api" -> customerRoutes[IO](customerService)
  ).orNotFound

  private def loadConfig: IO[ServerConf] =
    IO.fromEither(
      ConfigSource.default.load[ServerConf].leftMap(error => new RuntimeException(s"Failed to load config $error"))
    )

  override def run(args: List[String]): IO[ExitCode] =
    loadConfig.flatMap { serverConfig =>
      BlazeServerBuilder[IO]
        .bindHttp(serverConfig.port, serverConfig.host)
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }

}
