package itmo.highload.infrastructure.config

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.HazelcastInstance

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
}
