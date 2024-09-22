@file:Suppress("UnusedParameter")

package itmo.highload.service

import itmo.highload.dto.AnimalDto
import itmo.highload.dto.response.AnimalResponse
import org.springframework.stereotype.Service

@Service
class AnimalService {
    fun get(animalId: Int): AnimalResponse {
        return null!!
    }

    fun save(request: AnimalDto): AnimalResponse {
        return null!!
    }

    fun update(animalId: Int, request: AnimalDto): AnimalResponse {
        return null!!
    }

    fun delete(animalId: Int) {
        return
    }
}
