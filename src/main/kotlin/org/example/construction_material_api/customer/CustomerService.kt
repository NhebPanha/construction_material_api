package org.example.construction_material_api.customer

import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService(private val customerRepository: CustomerRepository) {

    @Transactional(readOnly = true)
    fun list(search: String?, page: Int?, size: Int?, sort: String?): PageResponse<CustomerResponse> =
        PageResponse.from(customerRepository.search(search, Paging.of(page, size, sort ?: "name,asc"))) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): CustomerResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: CustomerRequest): CustomerResponse {
        val customer = Customer(
            name = request.name,
            phone = request.phone,
            email = request.email,
            address = request.address,
            creditLimit = request.creditLimit,
            active = request.active,
        )
        return customerRepository.save(customer).toResponse()
    }

    @Transactional
    fun update(id: Long, request: CustomerRequest): CustomerResponse {
        val customer = findOrThrow(id).apply {
            name = request.name
            phone = request.phone
            email = request.email
            address = request.address
            creditLimit = request.creditLimit
            active = request.active
        }
        return customerRepository.save(customer).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val customer = findOrThrow(id)
        customer.active = false
        customerRepository.save(customer)
    }

    private fun findOrThrow(id: Long): Customer =
        customerRepository.findById(id).orElseThrow { NotFoundException("Customer $id not found") }
}

fun Customer.toResponse(): CustomerResponse = CustomerResponse(
    id = id ?: 0,
    name = name,
    phone = phone,
    email = email,
    address = address,
    creditLimit = creditLimit,
    active = active,
)
