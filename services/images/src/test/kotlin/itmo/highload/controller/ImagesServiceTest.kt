package itmo.highload.controller

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.domain.ImageObjectRefRepository
import itmo.highload.domain.interactor.ImagesService
import itmo.highload.infrastructure.minio.model.S3ObjectRef
import itmo.highload.minio.MinioConfig
import itmo.highload.minio.S3Storage
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

class ImagesServiceTest {

    private val imageObjectRefRepository: ImageObjectRefRepository = mockk()
    private val minioStorage: S3Storage = mockk()
    private val minioConfig: MinioConfig = mockk()
    private lateinit var imagesService: ImagesService

    private val testS3ObjectRef = S3ObjectRef(id = 1, bucket = "bucketName", key = "fileKey")

    @BeforeEach
    fun setup() {
        // Mock the defaultBucketName property in MinioConfig
        every { minioConfig.defaultBucketName } returns "bucketName"

        imagesService = ImagesService(imageObjectRefRepository, minioStorage, minioConfig)
    }

    @Test
    fun `should return image by id when exists`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.just(testS3ObjectRef)

        imagesService.getImageById(1).test()
            .expectNext(testS3ObjectRef)
            .verifyComplete()

        verify { imageObjectRefRepository.findById(1) }
    }

    @Test
    fun `should throw exception when image by id not found`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.empty()

        imagesService.getImageById(1).test()
            .expectError(EntityNotFoundException::class.java)
            .verify()

        verify { imageObjectRefRepository.findById(1) }
    }


    @Test
    fun `should delete image by id successfully`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.just(testS3ObjectRef)
        every { imageObjectRefRepository.deleteById(1) } returns Mono.empty()
        every { minioStorage.deleteObject(any(), any()) } returns Unit

        imagesService.deleteImageById(1).test()
            .expectNext(Unit)
            .verifyComplete()

        verify { imageObjectRefRepository.findById(1) }
        verify { imageObjectRefRepository.deleteById(1) }
        verify { minioStorage.deleteObject(any(), any()) }
    }


    @Test
    fun `should throw exception when deleting non-existent image`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.empty()

        imagesService.deleteImageById(1).test()
            .expectError(EntityNotFoundException::class.java)
            .verify()

        verify { imageObjectRefRepository.findById(1) }
    }

}
