package com.ecommerce.common.kafka;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    public static final String AVRO_RECORD_CLASS = "avro.record.class";
    public static final String AVRO_SCHEMA_HEADER = "avro_schema";
    private Class<T> targetType;

    public AvroDeserializer() {
    }

    public AvroDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (targetType == null && configs.containsKey(AVRO_RECORD_CLASS)) {
            Object configValue = configs.get(AVRO_RECORD_CLASS);
            if (configValue instanceof Class) {
                this.targetType = (Class<T>) configValue;
            } else if (configValue instanceof String) {
                try {
                    this.targetType = (Class<T>) Class.forName((String) configValue);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Could not find class: " + configValue, e);
                }
            }
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        return deserialize(topic, null, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(String topic, Headers headers, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        Class<?> recordClass = this.targetType;
        if (headers != null) {
            Header header = headers.lastHeader(AVRO_SCHEMA_HEADER);
            if (header != null && header.value() != null) {
                String className = new String(header.value(), StandardCharsets.UTF_8);
                try {
                    recordClass = Class.forName(className);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        if (recordClass == null) {
            throw new SerializationException("Target type not configured and no avro_schema header for topic: " + topic);
        }

        try {
            DatumReader<?> reader = new SpecificDatumReader<>(recordClass);
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(data), null);
            return (T) reader.read(null, decoder);
        } catch (IOException e) {
            throw new SerializationException("Error deserializing Avro message for topic: " + topic, e);
        }
    }

    @Override
    public void close() {
    }
}