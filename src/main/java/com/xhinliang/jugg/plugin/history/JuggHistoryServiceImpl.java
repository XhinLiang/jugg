package com.xhinliang.jugg.plugin.history;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.dizitart.no2.filters.Filters.text;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dizitart.no2.Document;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.SortOrder;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-03-21
 */
public class JuggHistoryServiceImpl implements JuggHistoryService {

    private static final Path LOCAL_PATH = Paths.get(Paths.get(System.getProperty("user.home")).toString(), ".jugg-history.db");

    private static final int QUERY_SIZE = 200;
    private static final int RESULT_LIMIT = 20;

    private final Nitrite database = Nitrite.builder() //
            .compressed() //
            .filePath(LOCAL_PATH.toFile()) //
            .openOrCreate();

    private static final JuggHistoryService INSTANCE = new JuggHistoryServiceImpl();

    public static JuggHistoryService getInstance() {
        return INSTANCE;
    }

    @Override
    public void addHistory(String username, String command) {
        NitriteCollection collection = getCollection(username);

        Document document = Document.createDocument("command", command);
        document.put("timestamp", System.currentTimeMillis());

        collection.insert(document);
        database.commit();
    }

    @Override
    public List<String> query(String username, String keyword) {
        FindOptions options = FindOptions.sort("timestamp", SortOrder.Descending) //
                .thenLimit(0, QUERY_SIZE);

        if (StringUtils.isNotEmpty(keyword)) {
            return getCollection(username) //
                    .find(text("command", "*" + keyword + "*"), options) //
                    .toList().stream() //
                    .map(d -> (String) d.get("command")) //
                    .distinct() //
                    .limit(RESULT_LIMIT) //
                    .collect(toImmutableList());
        }
        return getCollection(username) //
                .find(options) //
                .toList().stream() //
                .map(d -> (String) d.get("command")) //
                .distinct() //
                .limit(RESULT_LIMIT) //
                .collect(toImmutableList());
    }

    private NitriteCollection getCollection(String username) {
        NitriteCollection collection = database.getCollection(username);
        if (!collection.hasIndex("command")) {
            collection.createIndex("command", IndexOptions.indexOptions(IndexType.Fulltext));
        }
        if (!collection.hasIndex("username")) {
            collection.createIndex("username", IndexOptions.indexOptions(IndexType.NonUnique));
        }
        return collection;
    }
}
