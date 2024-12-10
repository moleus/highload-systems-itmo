package itmo.highload.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import itmo.highload.domain.ImageObjectRefRepository
import itmo.highload.domain.interactor.ImageServiceException
import itmo.highload.domain.interactor.ImagesService
import itmo.highload.infrastructure.minio.model.S3ObjectRef
import itmo.highload.minio.MinioConfig
import itmo.highload.minio.ObjectPutResult
import itmo.highload.minio.PartDataStream
import itmo.highload.minio.S3Storage
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import java.net.ConnectException

class ImagesServiceTest {

    private val imageObjectRefRepository: ImageObjectRefRepository = mockk()
    private val minioStorage: S3Storage = mockk()
    private val minioConfig: MinioConfig = mockk()
    private lateinit var imagesService: ImagesService

    private val testS3ObjectRef = S3ObjectRef(id = 1, bucket = "bucketName", key = "fileKey")
    private val httpHeaders: HttpHeaders = mockk()

    @BeforeEach
    fun setup() {
        every { minioConfig.defaultBucketName } returns "bucketName"
        every { httpHeaders.contentType } returns MediaType.parseMediaType(IMAGE_PNG_VALUE)

        imagesService = ImagesService(imageObjectRefRepository, minioStorage, minioConfig)
    }

    @Test
    fun `should return image by id when exists`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.just(testS3ObjectRef)

        imagesService.getImageById(1).test().expectNext(testS3ObjectRef).verifyComplete()

        verify { imageObjectRefRepository.findById(1) }
    }

    @Test
    fun `should throw exception when image by id not found`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.empty()

        imagesService.getImageById(1).test().expectError(EntityNotFoundException::class.java).verify()

        verify { imageObjectRefRepository.findById(1) }
    }


    @Test
    fun `should delete image by id successfully`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.just(testS3ObjectRef)
        every { imageObjectRefRepository.deleteById(1) } returns Mono.empty()
        every { minioStorage.deleteObject(any(), any()) } returns Unit

        imagesService.deleteImageById(1).test().expectNext(Unit).verifyComplete()

        verify { imageObjectRefRepository.findById(1) }
        verify { imageObjectRefRepository.deleteById(1) }
        verify { minioStorage.deleteObject(any(), any()) }
    }


    @Test
    fun `should throw exception when deleting non-existent image`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.empty()

        imagesService.deleteImageById(1).test().expectError(EntityNotFoundException::class.java).verify()

        verify { imageObjectRefRepository.findById(1) }
    }

    @Test
    fun `should update image by id successfully`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.just(testS3ObjectRef)
        every { minioStorage.putObject(any(), any(), any(), any()) } returns ObjectPutResult("etag")

        val stream: Mono<PartDataStream> =
            PartDataStream(stream = ByteArray(1).inputStream(), size = 0, partSize = -1).toMono()

        imagesService.updateImageById(1, "image/png", stream).test().expectNext(testS3ObjectRef).verifyComplete()

        verify { imageObjectRefRepository.findById(1) }
        verify { minioStorage.putObject(any(), any(), any(), any()) }
    }

    @Test
    fun `should throw exception when image by id not found on update`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.empty()

        val stream: Mono<PartDataStream> = PartDataStream(
            stream = ByteArray(1).inputStream(), size = 0, partSize = -1
        ).toMono()

        imagesService.updateImageById(1, "image/png", stream).test().expectError(EntityNotFoundException::class.java)
            .verify()

        verify { imageObjectRefRepository.findById(1) }
    }

    @Test
    fun `should throw exception when failed to connect to MinIO`() {
        every { imageObjectRefRepository.findById(1) } returns Mono.just(testS3ObjectRef)
        every { minioStorage.putObject(any(), any(), any(), any()) } throws ConnectException("Failed to connect")
        every { imageObjectRefRepository.save(any()) } returns Mono.just(testS3ObjectRef)

        val filePart: FilePart = mockk()
        every { filePart.filename() } returns "test.png"
        every { filePart.headers() } returns httpHeaders
        every { filePart.content() } returns Flux.empty()

        imagesService.updateImageById(1, filePart).test().expectError(ImageServiceException::class.java)
            .verify()

        verify { imageObjectRefRepository.findById(1) }
        verify { minioStorage.putObject(any(), any(), any(), any()) }
    }

    @Test
    fun `should throw exception when failed to connect to MinIO on save`() {
        every { minioStorage.putObject(any(), any(), any(), any()) } throws ConnectException("Failed to connect")
        every { imageObjectRefRepository.save(any()) } returns Mono.just(testS3ObjectRef)

        val filePart: FilePart = mockk()
        every { filePart.filename() } returns "test.png"
        every { filePart.headers() } returns httpHeaders
        every { filePart.content() } returns Flux.empty()

        imagesService.saveImage(filePart).test().expectError(ImageServiceException::class.java).verify()

        verify { minioStorage.putObject(any(), any(), any(), any()) }
    }
}
