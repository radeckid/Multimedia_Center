package com.damrad.multimediacenter.ui.drawer

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.damrad.multimediacenter.R
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
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
            myDrawerView.blur()
            myDrawerView.strokeWidth = 20f
            myDrawerView.paint?.strokeJoin = Paint.Join.ROUND
            myDrawerView.paint?.strokeCap = Paint.Cap.ROUND
        }

        pen.setOnClickListener {
            myDrawerView.normal()
            myDrawerView.strokeWidth = 10f
            myDrawerView.paint?.strokeJoin = Paint.Join.ROUND
            myDrawerView.paint?.strokeCap = Paint.Cap.ROUND
        }

        roller.setOnClickListener {
            myDrawerView.normal()
            myDrawerView.strokeWidth = 50f
            myDrawerView.paint?.strokeCap = Paint.Cap.SQUARE
            myDrawerView.paint?.strokeJoin = Paint.Join.MITER
        }

        colorPalete.setOnClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("Ok") { dialog, selectedColor, allColors ->
                    myDrawerView.currentColor = selectedColor
                    ViewCompat.setBackgroundTintList(colorPalete, ColorStateList.valueOf(selectedColor))
                }
                .setNegativeButton("Cancel") { dialog, which -> }
                .build()
                .show()
        }

        cleanDrawer.setOnClickListener {
            myDrawerView.clear()
        }
    }

}
