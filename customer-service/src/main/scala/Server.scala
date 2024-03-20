import cats.effect._
import cats.implicits.toBifunctorOps
import domain.{CustomerService, HttpClient}
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

  private def loadConfig: IO[ServerConf] =
    IO.fromEither(
      ConfigSource.default.load[ServerConf].leftMap(error => new RuntimeException(s"Failed to load config $error"))
    )

  private def buildCustomerService = {
    val client       = HttpClient[IO](8080, "127.0.0.1")
    val customerRepo = new InMemoryCustomerRepository[IO]
    val customerService = client.use { httpClient =>
      IO.pure(new CustomerService[IO](customerRepo, httpClient))
    }
    customerService
  }

  override def run(args: List[String]): IO[ExitCode] = {

    for {
      serverConfig    <- loadConfig
      customerService <- buildCustomerService
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
