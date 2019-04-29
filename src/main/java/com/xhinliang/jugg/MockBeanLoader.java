package com.xhinliang.jugg;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.xhinliang.jugg.loader.FlexibleBeanLoader;

/**
 * @author xhinliang
 */
public class MockBeanLoader extends FlexibleBeanLoader {

    private TestBean testBean = new TestBean();

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
}
