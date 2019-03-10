package com.xhinliang.jugg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.context.JuggUser;
import com.xhinliang.jugg.parse.IJuggEvalKiller;
import com.xhinliang.jugg.parse.JuggEvalKiller;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2018-12-10
 */
public class TestEvalOgnlKiller {

    @Test
    void test0() {
        IJuggEvalKiller testCommandParser = genEvalKiller();
        CommandContext guestContext = new CommandContext(new JuggUser(), "#testBean.ping(113L)");
        Object o = testCommandParser.eval(guestContext);
        System.out.println(o);
        Assertions.assertEquals(113L, o);
    }

    private IJuggEvalKiller genEvalKiller() {
        return new JuggEvalKiller(new JuggServerExample.MockBeanLoader());
    }
}
