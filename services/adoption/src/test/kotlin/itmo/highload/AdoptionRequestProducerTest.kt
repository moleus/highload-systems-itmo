package itmo.highload

import io.mockk.*
import itmo.highload.kafka.AdoptionRequestProducer


import itmo.highload.api.dto.response.AdoptionRequestResponse
import itmo.highload.api.dto.AdoptionStatus
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime

class AdoptionRequestProducerTest {

    private val kafkaTemplate: KafkaTemplate<String, AdoptionRequestResponse> = mockk(relaxed = true)
    private val producer = AdoptionRequestProducer(kafkaTemplate).apply {
        adoptionRequestCreatedTopic = "adoption-request-created-topic"
        adoptionRequestChangedTopic = "adoption-request-changed-topic"
    }

    @Test
    fun `sendMessageToAdoptionRequestCreatedTopic - should send message to correct topic`() {
        val dateTime = LocalDateTime.parse("2024-11-19T12:00:00")

        val status = AdoptionStatus.PENDING

        val adoptionRequest = AdoptionRequestResponse(
            id = 1,
            dateTime = dateTime,
            status = status,
            customerId = 1,
            managerId = null,
            animalId = 101
        )

        producer.sendMessageToAdoptionRequestCreatedTopic(adoptionRequest)

        verify { kafkaTemplate.send(producer.adoptionRequestCreatedTopic, adoptionRequest) }
    }

    @Test
    fun `sendMessageToAdoptionRequestChangedTopic - should send message to correct topic`() {
        val dateTime = LocalDateTime.parse("2024-11-19T12:00:00")

        val status = AdoptionStatus.APPROVED

        val adoptionRequest = AdoptionRequestResponse(
            id = 1,
            dateTime = dateTime,
            status = status,
            customerId = 1,
            managerId = 2,
            animalId = 101
        )

        every { kafkaTemplate.send(producer.adoptionRequestCreatedTopic, adoptionRequest) } just Awaits

        producer.sendMessageToAdoptionRequestChangedTopic(adoptionRequest)

        verify { kafkaTemplate.send(producer.adoptionRequestChangedTopic, adoptionRequest) }
    }
}
