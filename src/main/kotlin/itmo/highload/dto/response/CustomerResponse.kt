package itmo.highload.dto.response

import itmo.highload.model.enum.Gender

data class CustomerResponse(
    val id: Int,
    val phone: String,
    val gender: Gender,
    val address: String
)
