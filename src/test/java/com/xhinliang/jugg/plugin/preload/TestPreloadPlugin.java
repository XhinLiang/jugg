package com.xhinliang.jugg.plugin.preload;

import static com.xhinliang.jugg.MockBeanLoader.mvelEvalKiller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xhinliang.jugg.parse.IJuggEvalKiller;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-04-20
 */
public class TestPreloadPlugin {

    @Test
    void test() {
        IJuggEvalKiller evalKiller = mvelEvalKiller();
        JuggPreloadHandler juggPreloadHandler = new JuggPreloadHandler(evalKiller);

        juggPreloadHandler.getPreloaderList() //
                .forEach((name, preloader) -> {
                    Assertions.assertEquals(name, preloader.packageName());

                    preloader.getScripts().forEach(s -> evalKiller.eval(s, "test"));
                    Object output = evalKiller.eval(preloader.sampleInput(), "test");
                    Assertions.assertEquals(preloader.sampleOutput(), output);
                });
    }
}
