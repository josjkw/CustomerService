package gateway.models

object CustomerApi {
  case class CustomerApiOutput(id: String, name: String)
  case class CustomerApiInput(id: String, name: String)
}
