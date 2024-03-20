package gateway.models

object GatewayModels {
  final case class CustomerApiOutput(id: String, name: String)
  final case class CustomerWithDetailsApiOutput(id: String, name: String, data: String)
  final case class CustomerApiInput(id: String, name: String)
}
