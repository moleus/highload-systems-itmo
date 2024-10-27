package itmo.highload.service

import io.github.oshai.kotlinlogging.KotlinLogging
import itmo.highload.minio.MinioConfig
import itmo.highload.minio.PartDataStream
import itmo.highload.minio.S3Storage
import itmo.highload.model.ImageRef
import itmo.highload.repository.ImageRefRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.net.ConnectException
import java.util.*

class ImageServiceException(message: String) : RuntimeException(message)

@Service
class ImagesService @Autowired constructor(
    private val imageRefRepository: ImageRefRepository,
    private val minioStorage: S3Storage,
    private val minioConfig: MinioConfig
) {
    private val logger = KotlinLogging.logger {}

    fun getImageById(id: Int): Mono<ImageRef> {
        return imageRefRepository.findById(id)
    }

    fun saveImage(data: FilePart): Mono<ImageRef> {
        val bucketName = minioConfig.defaultBucketName
        val minioHost = minioConfig.minioUrl + ":" + minioConfig.minioPort
        val uuid = UUID.randomUUID().toString()
        val fileName = "$uuid/${data.filename()}"
        val minioImageUrl = "$minioHost/$bucketName/$fileName"
        val imageRef = ImageRef(url = minioImageUrl)

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
                minioStorage.putObject(
                    minioConfig.defaultBucketName, fileName, data.headers().contentType.toString(), partDataStream
                )
            } catch (e: ConnectException) {
                sink.error(ImageServiceException("Failed to connect to MinIO"))
            }
        }.doOnNext {
            logger.info { "Uploaded ${it.size} bytes to $fileName" }
            logger.info { "Image can be viewed in '$minioImageUrl'" }
        }.then(imageRefRepository.save(imageRef))
    }
}
