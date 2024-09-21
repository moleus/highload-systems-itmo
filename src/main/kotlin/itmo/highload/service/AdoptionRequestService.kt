@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.UpdateAdoptionRequestStatusDto
import itmo.highload.dto.response.AdoptionRequestResponse
import org.springframework.stereotype.Service

@Service
class AdoptionRequestService {
    fun save(animalId: Int): AdoptionRequestResponse {
        return null!!
    }

    fun update(request: UpdateAdoptionRequestStatusDto): AdoptionRequestResponse {
        return null!!
    }

    fun delete(animalId: Int) {
        // только если статус PENDING
    }
}
