package com.ks.model

import android.content.Context
import android.widget.Toast

object KotlinUtil {
    fun showKt(context: Context?): String {
        val text = "KT 1"
        Toast.makeText(context,text, Toast.LENGTH_SHORT).show()
        return text
    }
}
