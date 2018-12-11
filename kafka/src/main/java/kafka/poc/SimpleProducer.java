package kafka.poc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;

public final class SimpleProducer {
    private SimpleProducer() {
    }

    private static KafkaProducer producer(String serverIP) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", serverIP+":9092");
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("compression.type", "gzip");
        properties.put("batch.size", 16384);
        properties.put("linger.ms", 1);
        properties.put("buffer.memory", 33554432);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        return new KafkaProducer(properties);
    }

    public static <T> byte[] serializeMessage(T eventMessage, org.apache.avro.Schema classSchema) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        DatumWriter<T> writer = new SpecificDatumWriter<T>(classSchema);
        writer.write(eventMessage, encoder);
        encoder.flush();
        out.close();
        return out.toByteArray();
    }

    public static void startSending(KafkaProducer kafkaProducer, TopicProperties topicProperties,
            List<Map<String, String>> job) {
        try {
            for (Map<String, String> data : job) {
                while (topicProperties.getCounters().get(Counter.SENT.toString()).get() > topicProperties.getMaxSendInPeriod()) {
                    continue;
                }
                topicProperties.setRecordObj(PopulateAvroTopic.populateData(topicProperties.getTopic(),
                        topicProperties.getRecordObj(), data));

                ProducerRecord<Object, Object> record = new ProducerRecord<>(topicProperties.getTopic().toString(),
                        serializeMessage(topicProperties.getRecordObj()[0],
                                topicProperties.getRecordObj()[0].getSchema()),
                        serializeMessage(topicProperties.getRecordObj()[1],
                                topicProperties.getRecordObj()[1].getSchema()));

                kafkaProducer.send(record, (metadata, exception) -> {
                    if (exception != null){
                        topicProperties.getCounters().get(Counter.ERROR.toString()).incrementAndGet();

                    } else{
                        topicProperties.getCounters().get(Counter.RECEIVED.toString()).incrementAndGet();
                    }
                });
                topicProperties.getCounters().get(Counter.SENT.toString()).incrementAndGet();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
    }

    public static void main(String[] args) {

        KafkaProducer kafkaProducer = producer(args[0]);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                kafkaProducer.flush();
                kafkaProducer.close();
            }
        });

        CyclicBarrier cyclicBarrier = new CyclicBarrier(4);
        HashMap<String, TopicProperties> topics = new HashMap<String, TopicProperties>() {
            {
                {
                    put(Topic.INST_REF.toString(), new TopicProperties(Topic.INST_REF, "./out/inst-ref.csv", 100, 60));
                    put(Topic.PRICES.toString(), new TopicProperties(Topic.PRICES, "./out/prices.csv", 20000, 1));
                    put(Topic.POSITION.toString(), new TopicProperties(Topic.POSITION, "./out/position.csv", 40000, 1));
                }
                ;
            };
        };

        new Thread(new RunTopics(kafkaProducer, topics.get(Topic.INST_REF.toString()), 100, 1, 1, cyclicBarrier)).start();
        new Thread(new RunTopics(kafkaProducer, topics.get(Topic.PRICES.toString()), 1200000, 1, 1, cyclicBarrier)).start();
        new Thread(new RunTopics(kafkaProducer, topics.get(Topic.POSITION.toString()), 2400000, 1, 1, cyclicBarrier)).start();
        new Thread(new Timer(topics, cyclicBarrier)).start();

    }
}
