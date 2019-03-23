package com.xhinliang.jugg.plugin.alias;

import static java.util.Collections.emptyMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.xhinliang.jugg.loader.IBeanLoader;

/**
 * @author ligang03
 */
public class AliasServiceFileImpl implements AliasService {

    private static final Logger logger = LoggerFactory.getLogger(AliasServiceFileImpl.class);
    private static final Path LOCAL_PATH = Paths.get(Paths.get(System.getProperty("user.home")).toString(), ".jugg-configs");

    private static final String LOCAL_TARGET_ALIAS = LOCAL_PATH + "jugg-alias-target.properties";

    private static final String BUILDIN_TARGET_ALIAS = "buildin-jugg-alias-target.properties";

    private static AliasServiceFileImpl instance;

    private final Map<String, String> targetAliasMap = new ConcurrentHashMap<>();

    private AliasServiceFileImpl() {
        File localPath = LOCAL_PATH.toFile();
        if (localPath.mkdirs()) {
            logger.info("mkdir: {}", LOCAL_PATH);
        }
        Map<String, String> localTargetAlias = loadPropertiesFromLocalFile(LOCAL_TARGET_ALIAS);
        Map<String, String> buildinTargetAlias = loadPropertiesFromResource(BUILDIN_TARGET_ALIAS);

        // 如果有重复，以 local 为准
        targetAliasMap.putAll(buildinTargetAlias);
        targetAliasMap.putAll(localTargetAlias);

        // 回写本地文件
        flushTargetProperties();
    }

    public static AliasService instance() {
        if (instance == null) {
            synchronized (AliasServiceFileImpl.class) {
                if (instance == null) {
                    instance = new AliasServiceFileImpl();
                }
            }
        }
        return instance;
    }

    private static Map<String, String> loadPropertiesFromResource(String resourcePath) {
        Properties prop = new Properties();
        try (InputStream input = AliasServiceFileImpl.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                return emptyMap();
            }
            prop.load(input);
        } catch (IOException ex) {
            logger.info("local file not exist");
        }
        return new ConcurrentHashMap<>(Maps.fromProperties(prop));
    }

    private static Map<String, String> loadPropertiesFromLocalFile(String filePath) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            prop.load(input);
        } catch (IOException ex) {
            logger.info("local file not exist");
        }
        return new ConcurrentHashMap<>(Maps.fromProperties(prop));
    }

    private synchronized void flushTargetProperties() {
        Properties properties = new Properties();
        properties.putAll(targetAliasMap);
        flushProperties(properties, LOCAL_TARGET_ALIAS);
    }

    private static void flushProperties(Properties properties, String path) {
        try (OutputStream out = new FileOutputStream(path)) {
            properties.store(out, null);
        } catch (IOException e) {
            logger.warn("ops", e);
        }
    }

    @Override
    public void addLocalTargetAlia(String alia, String realName, IBeanLoader beanLoader) throws ClassNotFoundException {
        realName = realName.trim();
        Object beanOrClazz = beanLoader.getBeanByName(realName);
        if (beanOrClazz == null) {
            beanOrClazz = beanLoader.getClassByName(realName);
        }
        logger.info("target: {} found, put in target alias. alia:{}, real:{}", beanOrClazz, alia, realName);
        targetAliasMap.put(alia, realName);
        flushTargetProperties();
    }

    @Override
    public String getTargetRealNameIfNeed(String rawName) {
        rawName = rawName.trim();
        return targetAliasMap.getOrDefault(rawName, rawName);
    }

    @Override
    public Map<String, String> getTargetAlias() {
        return Maps.newHashMap(targetAliasMap);
    }
}
