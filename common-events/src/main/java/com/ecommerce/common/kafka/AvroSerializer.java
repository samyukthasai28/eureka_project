package com.ecommerce.common.kafka;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AvroSerializer<T extends SpecificRecordBase> implements Serializer<T> {

    public static final String AVRO_SCHEMA_HEADER = "avro_schema";

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            DatumWriter<T> datumWriter = new SpecificDatumWriter<>(data.getSchema());
            datumWriter.write(data, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Error serializing Avro message for topic: " + topic, e);
        }
    }

    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        if (data != null && headers != null) {
            headers.remove(AVRO_SCHEMA_HEADER);
            headers.add(AVRO_SCHEMA_HEADER, data.getClass().getName().getBytes(StandardCharsets.UTF_8));
        }
        return serialize(topic, data);
    }

    @Override
    public void close() {
    }
}