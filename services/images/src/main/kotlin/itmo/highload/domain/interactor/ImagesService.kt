package itmo.highload.domain.interactor

import io.github.oshai.kotlinlogging.KotlinLogging
import itmo.highload.domain.ImageObjectRefRepository
import itmo.highload.infrastructure.minio.model.S3ObjectRef
import itmo.highload.minio.MinioConfig
import itmo.highload.minio.PartDataStream
import itmo.highload.minio.S3Storage
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.net.ConnectException
import java.util.*

class ImageServiceException(message: String, e: Exception) : RuntimeException(message, e)

@Service
class ImagesService @Autowired constructor(
    private val imageObjectRefRepository: ImageObjectRefRepository,
    private val minioStorage: S3Storage,
    private val minioConfig: MinioConfig
) {
    private val logger = KotlinLogging.logger {}
    private val bucketName = minioConfig.defaultBucketName

    fun getImageById(id: Int): Mono<S3ObjectRef> {
        return imageObjectRefRepository.findById(id)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Image with ID $id not found")))
    }

    fun constructPublicEndpointFromPath(objectRef: S3ObjectRef): String {
        return "${minioConfig.publicEndpoint}/${objectRef.bucket}/${objectRef.key}"
    }

    fun saveImage(data: FilePart): Mono<S3ObjectRef> {
        val uuid = UUID.randomUUID().toString()
        val fileName = "$uuid/${data.filename()}"
        val s3ObjectRef = S3ObjectRef(
            bucket = bucketName,
            key = fileName,
        )

        return data.content().collectList().map { parts ->
            val byteArray = parts.flatMap { it.asInputStream().readAllBytes().toList() }.toByteArray()
            PartDataStream(
                stream = byteArray.inputStream(),
                size = byteArray.size.toLong(),
                partSize = -1,
            )
        }.doOnNext {
            logger.info { "Uploading ${it.size} bytes to $fileName" }
        }.handle<PartDataStream> { partDataStream, sink ->
            try {
                val putResult = minioStorage.putObject(
                    bucketName, fileName, data.headers().contentType.toString(), partDataStream
                )
                logger.info { "Uploaded object with etag ${putResult.etag} to $fileName" }
            } catch (e: ConnectException) {
                logger.warn { "Failed to connect to MinIO: $e" }
                sink.error(ImageServiceException("Failed to connect to MinIO", e))
            }
        }.then(try {
            imageObjectRefRepository.save(s3ObjectRef)
        } catch (e: DataAccessException) {
            logger.error { "Failed to save image reference: $e" }
            Mono.error(ImageServiceException("Failed to save image reference", e))
        }).doOnNext {
                logger.info { "Saved image reference with ID ${it.id}" }
            }
    }

    fun deleteImageById(id: Int): Mono<Unit> {
        return imageObjectRefRepository.findById(id)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Image with ID $id not found"))).flatMap { obj ->
                imageObjectRefRepository.deleteById(id)
                    .then(Mono.fromCallable { minioStorage.deleteObject(obj.bucket, obj.key) })
            }
    }

    fun updateImageById(id: Int, data: FilePart): Mono<S3ObjectRef> {
        return imageObjectRefRepository.findById(id)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Image with ID $id not found")))
            .flatMap { existingObjectRef ->
                val fileName = existingObjectRef.key
                val bucketName = existingObjectRef.bucket

                data.content().collectList().map { parts ->
                    val byteArray = parts.flatMap { it.asInputStream().readAllBytes().toList() }.toByteArray()
                    PartDataStream(
                        stream = byteArray.inputStream(),
                        size = byteArray.size.toLong(),
                        partSize = -1,
                    )
                }.doOnNext {
                    logger.info { "Updating ${it.size} bytes to $fileName" }
                }.handle<PartDataStream> { partDataStream, sink ->
                    try {
                        minioStorage.putObject(
                            bucketName, fileName, data.headers().contentType.toString(), partDataStream
                        )
                    } catch (e: ConnectException) {
                        sink.error(ImageServiceException("Failed to connect to MinIO", e))
                    }
                }.doOnNext {
                    logger.info { "Updated ${it.size} bytes in $fileName" }
                }.thenReturn(existingObjectRef)
            }
    }

}
