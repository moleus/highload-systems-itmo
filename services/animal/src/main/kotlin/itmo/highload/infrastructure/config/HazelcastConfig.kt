package itmo.highload.infrastructure.config

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.spring.cache.HazelcastCacheManager
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class HazelcastConfig {

    @Primary
    @Bean
    fun hazelcastInstance(): HazelcastInstance {
        return HazelcastClient.newHazelcastClient()
    }

    @Bean
    fun hazelcastServerInstance(): HazelcastInstance {
        return Hazelcast.newHazelcastInstance()
    }
}
