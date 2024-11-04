package itmo.highload.minio

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.SetBucketPolicyArgs
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
    val minioUrl: String,
    @Value("\${minio.bucketName}")
    var defaultBucketName: String,
    @Value("\${minio.publicEndpoint")
    val publicEndpoint: String,
) {

    @Bean
    fun provideMinioClient(): MinioClient {
        val client = MinioClient.builder()
        .credentials(username, password)
        .endpoint(minioUrl)
        .build()
        createDefaultBucketAndPolicy(client)
        return client
    }

    private fun createDefaultBucketAndPolicy(client: MinioClient) {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(defaultBucketName).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(defaultBucketName).build())
            val policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {"AWS": ["*"]},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::$defaultBucketName/*"]
                        }
                    ]
                }
            """.trimIndent()
            client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(defaultBucketName).config(policy).build())
        }
    }
}
