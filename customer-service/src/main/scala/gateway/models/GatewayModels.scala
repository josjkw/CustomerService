package gateway.models

import io.circe.generic.semiauto.{deriveCodec, deriveDecoder}
import io.circe.{Codec, Encoder}

object GatewayModels {
  final case class CustomerApiOutput(id: String, name: String)

  object CustomerApiOutput {
    implicit val customerApiOutput: Codec[CustomerApiOutput] = deriveCodec
  }

  final case class CustomerApiInput(id: String, name: String)

  object CustomerApiInput {
    implicit val customerApiInput: Codec[CustomerApiInput] = deriveCodec
  }

  final case class CustomerDetailsApiOutput(data: String)

  object CustomerDetailsApiOutput {

    implicit val customerDetailsApiOutputEncoder: Encoder[CustomerDetailsApiOutput] =
      Encoder.encodeString.contramap(_.data)

    implicit val customerDetailsApiOutputCodec: Codec[CustomerDetailsApiOutput] = Codec.from(
      deriveDecoder,
      customerDetailsApiOutputEncoder,
    )

  }

  final case class CustomerWithDetailsApiOutput(id: String, name: String, data: Option[CustomerDetailsApiOutput])

  object CustomerWithDetailsApiOutput {

    implicit val customerWithDetailsApiOutputCodec: Codec[CustomerWithDetailsApiOutput] =
      Codec.from(deriveDecoder, deriveCodec[CustomerWithDetailsApiOutput].mapJson(_.deepDropNullValues))

  }

}
