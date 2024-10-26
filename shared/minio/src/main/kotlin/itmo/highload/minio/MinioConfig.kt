package itmo.highload.minio

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig @Autowired constructor(
    @Value("\${minio.username}")
    private val username: String,
    @Value("\${minio.password}")
    private val password: String,
    @Value("\${minio.url}")
    private val minioUrl: String,
    @Value("\${minio.port}")
    private val minioPort: Int,
    @Value("\${minio.bucketName}")
    var defaultBucketName: String,
    @Value("\${minio.defaultFolder}")
    var defaultBaseFolder: String,
) {
    private val minioSecure = false

    @Bean
    fun provideMinioClient(): MinioClient = MinioClient.builder()
        .credentials(username, password)
        .endpoint(minioUrl, minioPort, minioSecure)
        .build()
}
