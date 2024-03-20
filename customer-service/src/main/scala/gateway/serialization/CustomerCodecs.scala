package gateway.serialization

import domain.{Customer, CustomerId, CustomerName}
import gateway.models.GatewayModels.{CustomerApiInput, CustomerApiOutput}
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.scalaland.chimney.Transformer

object CustomerCodecs {

  implicit val customerApiOutput: Codec[CustomerApiOutput] = deriveCodec
  implicit val customerApiInput: Codec[CustomerApiInput]   = deriveCodec

}
