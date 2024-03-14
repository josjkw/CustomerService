package models

import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto.deriveCodec

object Domain {

  final case class CustomerId(value: String)

  object CustomerId {
    implicit val encodeCustomerId: Encoder[CustomerId] = Encoder.encodeString.contramap[CustomerId](_.value)
    implicit val decodeCustomerId: Decoder[CustomerId] = Decoder.decodeString.map(CustomerId(_))
    implicit val customerIdCodec: Codec[CustomerId]    = Codec.from(decodeCustomerId, encodeCustomerId)
  }

  final case class CustomerName(value: String)

  object CustomerName {
    implicit val encodeCustomerName: Encoder[CustomerName] = Encoder.encodeString.contramap[CustomerName](_.value)
    implicit val decodeCustomerName: Decoder[CustomerName] = Decoder.decodeString.map(CustomerName(_))
    implicit val customerNameCodec: Codec[CustomerName]    = Codec.from(decodeCustomerName, encodeCustomerName)
  }

  final case class Customer(id: CustomerId, name: CustomerName)

  object Customer {
    implicit val customerCodec: Codec[Customer] = deriveCodec
  }

}
