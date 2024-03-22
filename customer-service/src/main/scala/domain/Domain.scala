package domain

final case class CustomerId(value: String)
final case class CustomerName(value: String)
final case class CustomerDetails(data: String)
final case class Customer(id: CustomerId, name: CustomerName)
final case class CustomerWithDetails(id: CustomerId, name: CustomerName, data: Option[CustomerDetails])
