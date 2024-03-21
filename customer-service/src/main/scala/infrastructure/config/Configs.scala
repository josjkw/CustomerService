package infrastructure.config

object Configs {
  final case class ServerConf(port: Int, host: String)
  final case class CustomerDetailsServiceConfig(port: Int, host: String)

  final case class CustomerServiceConfig(
      serverConf: ServerConf,
      customerDetailsServiceConfig: CustomerDetailsServiceConfig,
  )

}
