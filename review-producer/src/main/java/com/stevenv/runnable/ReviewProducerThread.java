package com.stevenv.runnable;

import com.stevenv.AppConfig;
import com.stevenv.Review;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ReviewProducerThread implements Runnable {

    private final String targetTopic;
    private Logger log = LoggerFactory.getLogger(ReviewFetcherThread.class.getSimpleName());

    private final AppConfig appConfig;
    private final ArrayBlockingQueue<Review> reviewsQueue;
    private final CountDownLatch latch;
    private final KafkaProducer<Long, Review> reviewProducer;

    public ReviewProducerThread(AppConfig appConfig, ArrayBlockingQueue<Review> reviewsQueue, CountDownLatch latch) {
        this.appConfig = appConfig;
        this.reviewsQueue = reviewsQueue;
        this.latch = latch;

        this.reviewProducer = createKafkaProducer(appConfig);
        this.targetTopic = appConfig.getTopicName();
    }

    private KafkaProducer<Long, Review> createKafkaProducer(AppConfig appConfig) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getBootstrapServers());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, appConfig.getSchemaRegistryUrl());

        KafkaProducer<Long, Review> producer = new KafkaProducer<>(properties);
        return producer;
    }

    @Override
    public void run() {
        int reviewCount = 0;
        try {
            while (latch.getCount() > 1 || reviewsQueue.size() > 0) {
                Review review = reviewsQueue.poll();
                if (review == null) {
                    Thread.sleep(200);
                } else {
                    reviewCount += 1;
                    log.info("Sending review " + reviewCount + ": " + review);
                    reviewProducer.send(new ProducerRecord<>(targetTopic, review));
                    Thread.sleep(appConfig.getProducerFrequencyMs());
                }
            }
        } catch (InterruptedException e) {
            log.warn("Avro Review Producer interupted");
        } finally {
            closed();
        }
    }

    private void closed() {
        log.info("Closing");
        reviewProducer.close();
        latch.countDown();
        log.info("Closed");
    }
}
