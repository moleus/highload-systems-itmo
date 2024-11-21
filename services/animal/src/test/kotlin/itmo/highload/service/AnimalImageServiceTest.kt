package itmo.highload.service

import io.mockk.*
import itmo.highload.api.dto.response.FileUrlResponse
import itmo.highload.api.dto.response.UploadedFileResponse
import itmo.highload.model.AnimalToImage
import itmo.highload.repository.AnimalRepository
import itmo.highload.repository.ImageToAnimalRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class AnimalImageServiceTest {

    private val imageRepository: ImageToAnimalRepository = mockk()
    private val imageService: ImageService = mockk()
    private val animalRepository: AnimalRepository = mockk()
    private val service = AnimalImageService(imageRepository, imageService, animalRepository)

    @Test
    fun `getImagesByAnimalId should return a list of FileUrlResponse`() {
        val animalId = 1
        val token = "token123"
        val animalToImage = AnimalToImage(animalId = animalId, imageId = 42)
        val fileUrlResponse = FileUrlResponse(fileID = 42, url = "http://example.com/image.jpg")

        every { imageRepository.findByAnimalId(animalId) } returns Flux.just(animalToImage)
        every { imageService.getImageUrlById(token, 42) } returns Mono.just(fileUrlResponse)

        val result = service.getImagesByAnimalId(animalId, token).collectList().block()

        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals(fileUrlResponse, result?.first())

        verify { imageRepository.findByAnimalId(animalId) }
        verify { imageService.getImageUrlById(token, 42) }
    }

    @Test
    fun `saveImageByAnimalId should save image and return UploadedFileResponse`() {
        val animalId = 1
        val token = "token123"
        val filePart: FilePart = mockk()
        val uploadedFileResponse = UploadedFileResponse(fileID = 42)
        val animalToImage = AnimalToImage(animalId = animalId, imageId = 42)

        every { animalRepository.findById(animalId) } returns Mono.just(mockk())
        every { imageService.uploadImage(token, any()) } returns Mono.just(uploadedFileResponse)
        every { imageRepository.save(animalToImage) } returns Mono.just(animalToImage)

        val result = service.saveImageByAnimalId(animalId, token, Mono.just(filePart)).block()

        assertNotNull(result)
        assertEquals(uploadedFileResponse, result)

        verify { animalRepository.findById(animalId) }
        verify { imageService.uploadImage(token, any()) }
        verify { imageRepository.save(animalToImage) }
    }

    @Test
    fun `updateImageByImageId should update image and return UploadedFileResponse`() {
        val imageId = 42
        val token = "token123"
        val newFilePart: FilePart = mockk()
        val animalToImage = AnimalToImage(animalId = 1, imageId = imageId)
        val updatedResponse = UploadedFileResponse(fileID = imageId)

        every { imageRepository.findByImageId(imageId) } returns Mono.just(animalToImage)
        every { imageService.updateImage(token, imageId, any()) } returns Mono.just(updatedResponse)

        val result = service.updateImageByImageId(imageId, token, Mono.just(newFilePart)).block()

        assertNotNull(result)
        assertEquals(updatedResponse, result)

        verify { imageRepository.findByImageId(imageId) }
        verify { imageService.updateImage(token, imageId, any()) }
    }

    @Test
    fun `deleteByImageId should delete image and relations`() {
        val imageId = 42
        val token = "token123"
        val animalToImage = AnimalToImage(animalId = 1, imageId = imageId)

        every { imageRepository.findByImageId(imageId) } returns Mono.just(animalToImage)
        every { imageService.deleteImageById(token, imageId) } returns Mono.empty()
        every { imageRepository.deleteAnimalToImageByImageId(imageId) } returns Mono.empty()

        val result = service.deleteByImageId(imageId, token).block()

        assertNull(result)

        verify { imageRepository.findByImageId(imageId) }
        verify { imageService.deleteImageById(token, imageId) }
        verify { imageRepository.deleteAnimalToImageByImageId(imageId) }
    }

    @Test
    fun `deleteAllByAnimalId should delete all images and their relations`() {
        val animalId = 1
        val token = "token123"
        val image1 = AnimalToImage(animalId = animalId, imageId = 42)
        val image2 = AnimalToImage(animalId = animalId, imageId = 43)
        val animalImages = Flux.just(image1, image2)

        every { imageRepository.findByAnimalId(animalId) } returns animalImages

        every { imageService.deleteImageById(token, 42) } returns Mono.empty()
        every { imageService.deleteImageById(token, 43) } returns Mono.empty()
        every { imageRepository.delete(image1) } returns Mono.empty()
        every { imageRepository.delete(image2) } returns Mono.empty()

        val result = service.deleteAllByAnimalId(animalId, token).block()

        assertNull(result)

        verify { imageRepository.findByAnimalId(animalId) }
        verify { imageService.deleteImageById(token, 42) }
        verify { imageService.deleteImageById(token, 43) }
        verify { imageRepository.delete(image1) }
        verify { imageRepository.delete(image2) }
    }

}
