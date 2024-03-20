import cats.effect._
import cats.implicits.toBifunctorOps
import domain.{CustomerService, HttpClient}
import gateway.CustomerRoutes.customerRoutes
import infrastructure.repositories.InMemoryCustomerRepository
import infrastructure.repositories.config.ServerConf
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Server extends IOApp {

  private def loadConfig: IO[ServerConf] =
    IO.fromEither(
      ConfigSource.default.load[ServerConf].leftMap(error => new RuntimeException(s"Failed to load config $error"))
    )

  private def buildCustomerService: CustomerService[IO] = {
    val client       = HttpClient[IO](8080, "127.0.0.1")
    val customerRepo = new InMemoryCustomerRepository[IO]
    new CustomerService[IO](customerRepo, client)
  }

  override def run(args: List[String]): IO[ExitCode] = {

    for {
      serverConfig    <- loadConfig
      customerService <- IO.pure(buildCustomerService)
      httpApp <- IO.pure(
        Router(
          "/api" -> customerRoutes[IO](customerService)
        ).orNotFound
      )
      server <- BlazeServerBuilder[IO]
        .bindHttp(serverConfig.port, serverConfig.host)
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    } yield server

  }

}
