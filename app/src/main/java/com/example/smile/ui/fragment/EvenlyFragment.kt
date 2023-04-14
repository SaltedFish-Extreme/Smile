package com.example.smile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.smile.widget.ext.setMargins
import com.example.smile.widget.gone
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.gyf.immersionbar.ktx.navigationBarHeight

/** 划一划 */
class EvenlyFragment : Fragment() {

    private val fab: FloatingActionButton by lazy { requireView().findViewById(com.example.smile.R.id.fab) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.example.smile.R.layout.fragment_evenly, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setMargins(
            0, 0, 50, navigationBarHeight + 100
        )
        fab.setOnClickListener { it.gone() }
    }
}