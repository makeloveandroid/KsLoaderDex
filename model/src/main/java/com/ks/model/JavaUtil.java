package com.ks.model;

import android.content.Context;
import android.widget.Toast;

public class JavaUtil {
    public static String showJava(Context context) {
        String text = "Java 1";
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        return text;
    }
}
