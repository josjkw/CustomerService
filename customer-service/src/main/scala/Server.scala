import cats.effect._
import cats.implicits.toBifunctorOps
import gateway.CustomerRoutes.customerRoutes
import infrastructure.config.Configs.CustomerServiceConfig
import infrastructure.modules.{CustomerDetailsModule, CustomerModule, HttpClientModule}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Server extends IOApp {

  private def loadConfig: IO[CustomerServiceConfig] =
    IO.fromEither(
      ConfigSource.default
        .load[CustomerServiceConfig]
        .leftMap(error => new RuntimeException(s"Failed to load config $error"))
    )

  override def run(args: List[String]): IO[ExitCode] = {

    for {
      config             <- loadConfig
      customerRepository <- CustomerModule.customerInMemoryRepository[IO](Map())
      httpClient         <- HttpClientModule.getHttpClient[IO]
      customerDetailsRepository <- CustomerDetailsModule.customerDetailsRepository[IO](
        config.customerDetailsServiceConfig,
        httpClient,
      )
      customerDetailsService <- CustomerDetailsModule.customerDetails(1, customerDetailsRepository)
      customerDetailsLegacyRepository <- CustomerDetailsModule.customerDetailsLegacyRepository[IO](
        config.customerDetailsLegacyConfig,
        httpClient,
      )
      customerDetailsLegacyService <- CustomerDetailsModule.customerDetails(2, customerDetailsLegacyRepository)
      customerService <- CustomerModule
        .customerService[IO](customerRepository, customerDetailsService, customerDetailsLegacyService)
      httpApp <- IO.pure(
        Router(
          "/api" -> customerRoutes[IO](customerService)
        ).orNotFound
      )
      server <- BlazeServerBuilder[IO]
        .bindHttp(config.serverConf.port, config.serverConf.host)
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    } yield server

  }

}
