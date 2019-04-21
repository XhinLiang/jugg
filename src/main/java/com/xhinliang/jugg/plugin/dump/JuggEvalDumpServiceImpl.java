package com.xhinliang.jugg.plugin.dump;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toMap;
import static org.dizitart.no2.filters.Filters.eq;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dizitart.no2.Document;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.SortOrder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author xhinliang
 */
public class JuggEvalDumpServiceImpl implements JuggEvalDumpService {

    private static final Path LOCAL_PATH = Paths //
            .get(Paths.get(System.getProperty("user.home")).toString(), ".jugg-dump.db");

    private static final int QUERY_SIZE = 200;
    private static final int RESULT_LIMIT = 20;

    private static final int KRYO_OUTPUT_BUFFER = 1000000;

    private final Nitrite database = Nitrite.builder() //
            .compressed() //
            .filePath(LOCAL_PATH.toFile()) //
            .openOrCreate();

    private final ThreadLocal<Kryo> kryo = ThreadLocal.withInitial(() -> {
        Kryo temp = new KryoReflectionFactorySupport();
        temp.setRegistrationRequired(false);
        return temp;
    });

    private final Output output = new Output(KRYO_OUTPUT_BUFFER);

    private static final JuggEvalDumpService INSTANCE = new JuggEvalDumpServiceImpl();

    public static JuggEvalDumpService getInstance() {
        return INSTANCE;
    }

    @Override
    public long save(String username, Map<String, Object> context) {
        NitriteCollection collection = getCollection(username);

        long timestamp = currentTimeMillis();
        context.forEach((fieldName, value) -> {
            Document document = Document.createDocument("fieldName", fieldName);
            document.put("fieldClass", value.getClass());
            if (value instanceof Serializable) {
                document.put("fieldValue", value);
            }
            document.put("fieldBytes", toBytes(value));
            document.put("id", timestamp);
            collection.insert(document);
        });

        database.commit();
        return timestamp;
    }

    @Override
    public void remove(String username, long id) {
    }

    @Override
    public Map<String, Object> load(String username, long id) {
        FindOptions options = FindOptions.sort("id", SortOrder.Descending) //
                .thenLimit(0, QUERY_SIZE);
        return getCollection(username) //
                .find(eq("id", id), options) //
                .toList() //
                .stream() //
                .limit(RESULT_LIMIT) //
                .map(d -> {
                    String fieldName = (String) d.get("fieldName");
                    if (StringUtils.isBlank(fieldName)) {
                        return null;
                    }
                    Object val = d.get("fieldValue");
                    if (val != null) {
                        return Tuples.of(fieldName, val);
                    }
                    byte[] bytes = (byte[]) d.get("fieldBytes");
                    Class<?> clazz = (Class<?>) d.get("fieldClass");
                    if (ArrayUtils.isNotEmpty(bytes) && clazz != null) {
                        Object obj = fromBytes(clazz, bytes);
                        if (obj != null) {
                            return Tuples.of(fieldName, obj);
                        }
                    }
                    return null;
                }) //
                .filter(Objects::nonNull) //
                .collect(toMap(Tuple2::getT1, Tuple2::getT2, (a, b) -> a));
    }

    @Override
    public List<Long> list(String username) {
        FindOptions options = FindOptions.sort("id", SortOrder.Descending) //
                .thenLimit(0, QUERY_SIZE);
        return getCollection(username) //
                .find(options) //
                .toList().stream() //
                .map(d -> (Long) d.get("id")) //
                .distinct() //
                .limit(RESULT_LIMIT) //
                .collect(toImmutableList());
    }

    @Nullable
    private byte[] toBytes(Object object) {
        try {
            // CHECKSTYLE:OFF
            output.reset();
            // CHECKSTYLE:ON
            kryo.get().writeObject(output, object);
            byte[] bytes = output.toBytes();
            output.close();
            return bytes;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private <T> T fromBytes(Class<T> clazz, byte[] bytes) {
        try {
            Input input = new Input(bytes);
            return kryo.get().readObject(input, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean dropDb() throws IOException {
        return Files.deleteIfExists(LOCAL_PATH);
    }

    private NitriteCollection getCollection(String username) {
        NitriteCollection collection = database.getCollection(username);
        if (!collection.hasIndex("id")) {
            collection.createIndex("id", IndexOptions.indexOptions(IndexType.NonUnique));
        }
        return collection;
    }
}
