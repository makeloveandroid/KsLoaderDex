package com.ks.loaderdex

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ks.model.JavaUtil
import com.ks.model.KotlinUtil

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val text = view.findViewById<TextView>(R.id.textview_second)
        view.findViewById<Button>(R.id.button_java).setOnClickListener {
            val str = JavaUtil.showJava(this@FirstFragment.context)
            text.text = str
        }
        view.findViewById<Button>(R.id.button_kt).setOnClickListener {
            val str = KotlinUtil.showKt(this@FirstFragment.context)
            text.text = str
        }
    }
}
