package com.shyk.cardsstackexample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.shyk.cardsstackexample.databinding.FrameActivityBinding

/**
 * Created By: Khizzar
 * Date: 22/07/2024
 **/
class FrameActivity : AppCompatActivity() {

    private lateinit var binding: FrameActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.frame_activity)

        performActionsV2()
    }

    private fun performActionsV2() {
        binding.card1.setOnClickListener {
            handleViewClick(binding.card1)
        }
        binding.card2.setOnClickListener {
            handleViewClick(binding.card2)
        }
        binding.card3.setOnClickListener {
            handleViewClick(binding.card3)
        }
    }

    private fun handleViewClick(clickedView: View) {
        val viewList =
            arrayListOf<View>().apply { this.addAll(binding.llParent.children) }.reversed()
        if (viewList.isEmpty() || viewList.first() == clickedView) return

        // Perform the animation
        CardsAnimationUtils.animateViews(
            viewList,
            clickedView,
            -200f,
            viewList.first().height.toFloat()
        )

        binding.root.requestLayout()
        binding.root.invalidate()
    }
}