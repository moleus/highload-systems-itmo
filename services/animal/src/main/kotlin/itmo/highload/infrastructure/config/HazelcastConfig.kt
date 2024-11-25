import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.spring.cache.HazelcastCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.cache.CacheManager

@Configuration
class HazelcastConfig {

    @Bean
    fun hazelcastClientConfig(): ClientConfig {
        val config = ClientConfig()
        config.networkConfig.addresses = listOf("localhost:5701")
        config.clusterName = "dev"
        return config
    }

    @Bean
    fun hazelcastInstance(clientConfig: ClientConfig): HazelcastInstance {
        return HazelcastClient.newHazelcastClient(clientConfig)
    }

    @Bean
    fun cacheManager(hazelcastInstance: HazelcastInstance): CacheManager {
        return HazelcastCacheManager(hazelcastInstance)
    }
}
