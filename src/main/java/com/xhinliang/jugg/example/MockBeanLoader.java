package com.xhinliang.jugg.example;

import javax.annotation.Nonnull;

import com.xhinliang.jugg.loader.IBeanLoader;

/**
 * @author xhinliang
 */
class MockBeanLoader implements IBeanLoader {

    @Override
    public Object getBeanByName(String name) {
        return new TestBean();
    }

    @Nonnull
    @Override
    public Class<?> getClassByName(String name) {
        return TestBean.class;
    }

    @Override
    public Object getBeanByClass(@Nonnull Class<?> clazz) {
        return null;
    }

    @SuppressWarnings({ "unused", "WeakerAccess" })
    public static class TestBean {

        private String value = "";

        public String getVal() {
            return value;
        }

        public long ping(long val) {
            return val;
        }

        public String ping(String val) {
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
