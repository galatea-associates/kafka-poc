package kafka.poc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;
import java.util.Properties;
import java.util.stream.IntStream;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SimpleProducer {
    private SimpleProducer() {
    }

    private static Producer producer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "3.8.1.159:9092");
        properties.put("acks", "1");
        properties.put("retries", 0);
        properties.put("compression.type", "gzip");
        properties.put("batch.size", 16384);
        properties.put("linger.ms", 1);
        properties.put("buffer.memory", 33554432);
        properties.put("key.serializer",
                "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put("value.serializer",
                "org.apache.kafka.common.serialization.ByteArraySerializer");

        return new KafkaProducer(properties);
    }

    public static <T> byte[] serializeMessage(T eventMessage, org.apache.avro.Schema classSchema)
            throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        DatumWriter<T> writer = new SpecificDatumWriter<T>(classSchema);
        writer.write(eventMessage, encoder);
        encoder.flush();
        out.close();
        return out.toByteArray();
    }

    private static List<Map<String, String>> read_file(String csvFile) {
        File file = new File(csvFile);
        List<Map<String, String>> response = new LinkedList<Map<String, String>>();
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        MappingIterator<Map<String, String>> iterator = null;
        try {
            iterator = mapper.reader(Map.class).with(schema).readValues(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (iterator.hasNext()) {
            response.add(iterator.next());
        }
        return response;
    }

    private static String getTopicName(Topic topic) {
        switch (topic) {
            case INST_REF:
                return "inst_ref";
            case PRICES:
                return "prices";
            case POSITION:
                return "position";
            default:
                return "";
        }

    }

    private static void startSending(Producer kafkaProducer, Topic topic,
            List<Map<String, String>> dataList) {
        org.apache.avro.specific.SpecificRecord[] recordObj = generateClasses(topic);
        try {
            for (Map<String, String> data : dataList) {
                recordObj = PopulateAvroTopic.populateData(topic, recordObj, data);

                ProducerRecord<Object, Object> record = new ProducerRecord<>(getTopicName(topic),
                        serializeMessage(recordObj[0], recordObj[0].getSchema()),
                        serializeMessage(recordObj[1], recordObj[1].getSchema()));

                kafkaProducer.send(record);
                System.out.println("I sent a message");

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

    }


    public static org.apache.avro.specific.SpecificRecord[] generateClasses(Topic topic) {
        switch (topic) {
            case INST_REF:
                return new org.apache.avro.specific.SpecificRecord[] {new instrument_reference_data_keys(), new instrument_reference_data_values()};
            case PRICES:
                return new org.apache.avro.specific.SpecificRecord[] {new prices_keys(), new prices_values()};
            case POSITION:
                return new org.apache.avro.specific.SpecificRecord[] {new position_data_keys(), new position_data_values()};
            default:
                return null;
        }
    }

    public static void main(String[] args) {

        Producer kafkaProducer = producer();
        Topic topic = Topic.INST_REF;

        String csvFile = "./out/inst-ref.csv";
        List<Map<String, String>> data = read_file(csvFile);
        startSending(kafkaProducer, topic, data);

        kafkaProducer.flush();
        kafkaProducer.close();
    }
}
