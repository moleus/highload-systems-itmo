package itmo.highload.controller

import io.mockk.*
import itmo.highload.model.S3ObjectRef
import itmo.highload.minio.S3Storage
import itmo.highload.repository.ImageObjectRefRepository
import itmo.highload.service.ImagesService
import itmo.highload.minio.MinioConfig
import itmo.highload.minio.ObjectPutResult
import itmo.highload.minio.PartDataStream
import jakarta.persistence.EntityNotFoundException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.io.ByteArrayInputStream
import java.net.http.HttpHeaders

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
