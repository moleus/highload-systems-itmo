package itmo.highload.service.mapper

import itmo.highload.dto.response.CustomerResponse
import itmo.highload.dto.response.UserResponse
import itmo.highload.model.Customer
import itmo.highload.model.User

object UserMapper {
    fun toResponse(user: User?): UserResponse? {
        return user?.let {
            UserResponse(
                id = it.id,
                login = it.login
            )
        }
    }

    fun toResponse(customer: Customer): CustomerResponse {
        return customer.let {
            CustomerResponse(
                id = it.id,
                phone = it.phone,
                gender = it.gender,
                address = it.address
            )
        }
    }
}
