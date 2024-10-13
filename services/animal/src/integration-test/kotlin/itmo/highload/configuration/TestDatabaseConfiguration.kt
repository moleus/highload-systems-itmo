package itmo.highload.configuration


//@Configuration
//class TestDatabaseConfiguration {
//    @Bean
//    fun initializer(connectionFactory: ConnectionFactory?): ConnectionFactoryInitializer {
//        val initializer = ConnectionFactoryInitializer()
//        initializer.setConnectionFactory(connectionFactory!!)
//
//        val populator = CompositeDatabasePopulator()
//        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("test-data.sql")))
//        initializer.setDatabasePopulator(populator)
//
//        return initializer
//    }
//}
