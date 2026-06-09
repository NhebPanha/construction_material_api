package org.example.construction_material_api.customer.service

import org.example.construction_material_api.customer.model.*
import org.example.construction_material_api.customer.repository.*
import org.example.construction_material_api.customer.dto.*

import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService(private val customerRepository: CustomerRepository) {

    @Transactional(readOnly = true)
    fun list(q: String?, page: Int?, pageSize: Int?): PageResponse<CustomerResponse> =
        PageResponse.from(
            customerRepository.search(q, Paging.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name"))),
        ) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): CustomerResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: CustomerRequest): CustomerResponse {
        val customer = Customer(
            name = request.name,
            phone = request.phone,
            email = request.email,
            address = request.address,
            loyaltyPoints = request.loyaltyPoints,
            balance = request.balance,
            discountRate = request.discountRate,
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
            loyaltyPoints = request.loyaltyPoints
            balance = request.balance
            discountRate = request.discountRate
        }
        return customerRepository.save(customer).toResponse()
    }

    @Transactional
    fun delete(id: Long) = customerRepository.delete(findOrThrow(id))

    private fun findOrThrow(id: Long): Customer =
        customerRepository.findById(id).orElseThrow { NotFoundException("Customer $id not found") }
}
