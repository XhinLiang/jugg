package com.xhinliang.jugg;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.xhinliang.jugg.loader.FlexibleBeanLoader;
import com.xhinliang.jugg.parse.IJuggEvalKiller;
import com.xhinliang.jugg.parse.mvel.JuggMvelEvalKiller;
import com.xhinliang.jugg.parse.ognl.JuggOgnlEvalKiller;

/**
 * @author xhinliang <xhinliang@gmail.com>
 * Created on 2019-04-20
 */
public class MockBeanLoader extends FlexibleBeanLoader {

    private TestBean testBean = new TestBean();

    public static IJuggEvalKiller ognlEvalKiller() {
        return new JuggOgnlEvalKiller(new MockBeanLoader());
    }

    public static IJuggEvalKiller mvelEvalKiller() {
        return new JuggMvelEvalKiller(new MockBeanLoader());
    }

    @Override
    public Object getBeanByClass(@Nonnull Class<?> clazz) {
        return clazz == TestBean.class ? testBean : null;
    }

    @Nullable
    @Override
    protected Object getActualBean(String name) {
        if (StringUtils.equals("testBean", name)) {
            return testBean;
        }
        return null;
    }

    /**
     * for test
     */
    @SuppressWarnings({ "unused", "WeakerAccess" })
    private static class TestBean {

        private String value = "xxx";

        private String getVal() {
            return value;
        }

        public long ping(long val) {
            return val;
        }

        public <T> T ping(T val) {
            return val;
        }

        public String add(String a, String b) {
            return a + b;
        }

        public TestBean append(String raw) {
            value += raw;
            return self();
        }

        public TestBean self() {
            return this;
        }
    }

}
