package com.andframe.annotation.multistatus;


import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定多状态页面的各个布局ID
 * Created by SCWANG on 2016/10/20.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiStatusInvalidNet {
    @LayoutRes int value();
    @IdRes int txtId() default 0;
    @IdRes int btnId() default 0;
}
