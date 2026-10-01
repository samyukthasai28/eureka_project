package com.ecommerce.common.kafka;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    public static final String AVRO_RECORD_CLASS = "avro.record.class";
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
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            T record = (targetType != null) ? targetType.getDeclaredConstructor().newInstance() : null;
            if (record == null) {
                throw new SerializationException("Target type not configured for AvroDeserializer on topic: " + topic);
            }
            DatumReader<T> reader = new SpecificDatumReader<>(record.getSchema());
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(data), null);
            return reader.read(null, decoder);
        } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new SerializationException("Error deserializing Avro message for topic: " + topic, e);
        }
    }

    @Override
    public void close() {
    }
}