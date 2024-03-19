package domain

final case class CustomerId(value: String)
final case class CustomerName(value: String)
final case class Customer(id: CustomerId, name: CustomerName)
