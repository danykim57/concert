package com.reservation.ticket.concert.infrastructure.event

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import java.time.Duration
import java.util.concurrent.TimeUnit

@SpringBootTest
@EmbeddedKafka(
    topics = ["test-topic"],
    partitions = 1,
    bootstrapServersProperty = "spring.kafka.bootstrap-servers",
)
class KafkaTest {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Test
    fun testKafkaConnection() {
        val topic = "test"
        val data = "Hello World"

        val consumerProps =
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaTemplate.producerFactory.configurationProperties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG],
                ConsumerConfig.GROUP_ID_CONFIG to "test",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            )

        // given
        val consumer = DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer()
        consumer.subscribe(listOf(topic))

        // when
        val result = kafkaTemplate.send(topic, data).get(2, TimeUnit.SECONDS)

        // then
        assertEquals(data, result.producerRecord.value())
        var records = consumer.poll(Duration.ofSeconds(3))
        assertTrue(!records.isEmpty)
        assertEquals(data, records.records(topic).iterator().next().value())

        //end
        consumer.close()
    }
}