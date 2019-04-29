package com.xhinliang.jugg;

/**
 * @author xhinliang
 */
public class TestBean {

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
