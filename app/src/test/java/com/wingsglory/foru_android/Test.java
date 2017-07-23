package com.wingsglory.foru_android;

import java.util.Arrays;

/**
 * Created by hezhujun on 2017/7/23.
 */

public class Test {

    @org.junit.Test
    public void test() {
        String str = "0.0";
        String[] numbers = str.split("\\.");
        System.out.println(Arrays.toString(numbers));
    }
}
