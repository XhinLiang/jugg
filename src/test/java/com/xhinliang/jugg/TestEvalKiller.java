package com.xhinliang.jugg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xhinliang.jugg.context.CommandContext;
import com.xhinliang.jugg.context.JuggUser;
import com.xhinliang.jugg.parse.IJuggEvalKiller;
import com.xhinliang.jugg.parse.mvel.JuggMvelEvalKiller;
import com.xhinliang.jugg.parse.ognl.JuggOgnlEvalKiller;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2018-12-10
 */
public class TestEvalKiller {

    @Test
    void testOgnl() {
        IJuggEvalKiller testCommandParser = genEvalKiller();
        CommandContext guestContext = new CommandContext(new JuggUser(), "#testBean.ping(113L)");
        Object o = testCommandParser.eval(guestContext);
        System.out.println(o);
        Assertions.assertEquals(113L, o);
    }

    @Test
    void testMvel() {
        IJuggEvalKiller testCommandParser = mvelEvalKiller();
        CommandContext guestContext = new CommandContext(new JuggUser(), "foreach (x : 9) { System.out.print(x); }");
        Object o = testCommandParser.eval(guestContext);
        System.out.println(o);
        o = testCommandParser.eval(new CommandContext(new JuggUser(), "s = 'abc'"));
        System.out.println(o);
        o = testCommandParser.eval(new CommandContext(new JuggUser(), "s"));
        System.out.println(o);
        o = testCommandParser.eval(new CommandContext(new JuggUser(), "testBean.value"));
        System.out.println(o);
        o = testCommandParser.eval(new CommandContext(new JuggUser(), "def addTwo(a, b){ a + b; }"));
        System.out.println(o);
    }

    private IJuggEvalKiller genEvalKiller() {
        return new JuggOgnlEvalKiller(new JuggServerExample.MockBeanLoader());
    }

    private IJuggEvalKiller mvelEvalKiller() {
        return new JuggMvelEvalKiller(new JuggServerExample.MockBeanLoader());
    }
}
