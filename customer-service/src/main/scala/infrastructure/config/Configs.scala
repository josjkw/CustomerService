package infrastructure.config

object Configs {
  final case class ServerConf(port: Int, host: String)

  final case class CustomerDetailsConfig(port: Int, host: String)
  final case class CustomerDetailsLegacyConfig(port: Int, host: String)

  final case class CustomerDetailsServiceConfig(
      customerDetailsConfig: CustomerDetailsConfig,
      customerDetailsLegacyConfig: CustomerDetailsLegacyConfig,
  )

  final case class CustomerServiceConfig(
      serverConf: ServerConf,
      customerDetailsServiceConfig: CustomerDetailsServiceConfig,
  )

}
