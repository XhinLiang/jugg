package com.xhinliang.jugg.plugin.preload;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-04-20
 */
public class CollectionFunctionPreloader implements IJuggPreloader {

    @Override
    public List<String> getScripts() {
        return ImmutableList.<String> builder() //
                .add("" //
                        + "def filterToList(tempCollection, tempFilterFun) { " //
                        + "    tempResult = []; " //
                        + "    foreach (item : tempCollection) { " //
                        + "        if (tempFilterFun(item)) { " //
                        + "            tempResult.add(item); " //
                        + "        } " //
                        + "    } " //
                        + "    return tempResult;" //
                        + "} ")
                .build();
    }

    @Override
    public String packageName() {
        return "collectionFunction";
    }

    @Override
    public String desc() {
        return "functions for collection.";
    }

    @Override
    public String sampleInput() {
        return "filterToList([4L, 23, 22, 21], def (number) { number > 10 })";
    }

    @Override
    public Object sampleOutput() {
        // CHECKSTYLE:OFF
        return ImmutableList.of(23, 22, 21);
        // CHECKSTYLE:ON
    }
}
