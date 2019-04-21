package com.xhinliang.jugg.plugin.dump;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nullable;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;

/**
 * @author xhinliang
 */
public class KryoSerializer {

    @Nullable
    public static byte[] serialize(Object obj) {
        try {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream);
            kryo.writeClassAndObject(output, obj);
            output.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static  <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream);
            input.close();
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new KryoReflectionFactorySupport();
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

}
