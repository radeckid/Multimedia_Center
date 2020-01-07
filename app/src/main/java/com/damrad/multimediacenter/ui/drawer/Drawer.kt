package com.damrad.multimediacenter.ui.drawer

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.damrad.multimediacenter.R
import kotlinx.android.synthetic.main.drawer_fragment.*

class Drawer : Fragment() {

    companion object {
        fun newInstance() = Drawer()
    }

    private lateinit var viewModel: DrawerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.drawer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DrawerViewModel::class.java)

        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
        myDrawerView.init(metrics)

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        brush.setOnClickListener {
            myDrawerView.emboss()
        }

        pen.setOnClickListener{

        }

        roller.setOnClickListener{

        }

        colorPalete.setOnClickListener{

        }
    }

}
