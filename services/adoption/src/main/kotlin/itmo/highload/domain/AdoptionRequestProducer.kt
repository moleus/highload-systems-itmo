package itmo.highload.domain

import itmo.highload.api.dto.response.AdoptionRequestResponse

interface AdoptionRequestProducer {
    fun sendMessageToAdoptionRequestCreatedTopic(adoptionRequest: AdoptionRequestResponse)
    fun sendMessageToAdoptionRequestChangedTopic(adoptionRequest: AdoptionRequestResponse)
}
