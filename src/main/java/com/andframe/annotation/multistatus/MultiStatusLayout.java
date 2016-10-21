package com.andframe.annotation.multistatus;


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
public @interface MultiStatusLayout {
    @LayoutRes int progress();
    @LayoutRes int empty();
    @LayoutRes int error() default -1;
    @LayoutRes int invalidnet() default -1;
}
