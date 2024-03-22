import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toBifunctorOps
import gateway.CustomerDetailsLegacyRoutes.customerDetailsLegacyRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object CustomerDetailsLegacyServer extends IOApp {

  // inlined for simplicity
  private final case class ServerCustomerDetailsLegacyConf(port: Int, host: String)

  private def loadConfig: IO[ServerCustomerDetailsLegacyConf] =
    IO.fromEither(
      ConfigSource.default
        .load[ServerCustomerDetailsLegacyConf]
        .leftMap(error => new RuntimeException(s"Failed to load config $error"))
    )

  private val httpApp = Router(
    "/internal/legacy" -> customerDetailsLegacyRoutes[IO]
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    loadConfig.flatMap { conf =>
      BlazeServerBuilder[IO]
        .withHttpApp(httpApp)
        .bindHttp(conf.port, conf.host)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }
  }

}
