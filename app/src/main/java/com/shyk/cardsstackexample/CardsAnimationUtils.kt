package com.shyk.cardsstackexample

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup

/**
 * Created By: Khizzar
 * Date: 22/07/2024
 **/


/**
 * This object handles the animation logic for the views
 * **/
object CardsAnimationUtils {

    fun animateViews(
        views: List<View>,
        clickedView: View,
        translationUpDistance: Float,
        translationDownDistance: Float,
    ) {
        val clickedIndex = views.indexOf(clickedView)
        if (clickedIndex == -1) return // Clicked view not found in the list
        val moreThanOneViewOnTop = views.indexOf(clickedView) > 1

        // create the list of the x,y coordinates of the view list for later use
        val viewXYList = arrayListOf<Pair<Float, Float>>().apply {
            views.forEach {
                val viewLocation = IntArray(2)
                it.getLocationInWindow(viewLocation)
                this.add(Pair(viewLocation[0].toFloat(), it.y))
            }
        }

        val animatorSet = AnimatorSet()
        val animations = mutableListOf<Animator>()

        // Animate all views in front of the clicked view to move down
        for (i in 0 until clickedIndex) {
            val viewToMoveDown = views[i]
            val moveDown =
                ObjectAnimator.ofFloat(
                    viewToMoveDown,
                    "translationY",
                    translationDownDistance + viewToMoveDown.translationY
                )
            animations.add(moveDown)
        }

        // Animate the clicked view to move up a bit
        val moveUp = ObjectAnimator.ofFloat(
            clickedView,
            "translationY",
            translationUpDistance + clickedView.translationY
        )
        animations.add(moveUp)

        animatorSet.playTogether(animations)
        animatorSet.duration = 500 // Duration in milliseconds

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                // check if the clicked view has more than 1 view in front then run iterations other wise not
                if (moreThanOneViewOnTop) {
                    var tempView = clickedView
                    for (i in views.indexOf(clickedView) - 1 downTo 0) {
                        val viewToMoveBack = views[i]
                        moveViewToBack(
                            viewToMoveBack,
                            viewXYList[views.indexOf(tempView)],
                            viewXYList[i],
                            tempView
                        )
                        tempView = views[i]

                        if (i == 0) { // first item
                            moveViewToFront(
                                clickedView,
                                viewXYList[views.indexOf(clickedView)],
                                views[i],
                                viewXYList[i],
                            )
                        }
                    }
                } else {
                    moveViewToBack(
                        views[views.indexOf(clickedView) - 1],
                        viewXYList[views.indexOf(clickedView)],
                        viewXYList[views.indexOf(clickedView) - 1],
                        clickedView
                    )
                    moveViewToFront(
                        clickedView,
                        viewXYList[views.indexOf(clickedView)],
                        views[views.indexOf(clickedView) - 1],
                        viewXYList[views.indexOf(clickedView) - 1],
                    )
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animatorSet.start()
    }

    private fun moveViewToBack(
        viewToMoveBack: View,
        clickedViewCoordinates: Pair<Float, Float>,
        viewToMoveBackOrgCoordinates: Pair<Float, Float>,
        clickedView: View
    ) {
        val animatorSet = AnimatorSet()

        // Move view up to clickedView's Y position
        val moveUp = ObjectAnimator.ofFloat(
            viewToMoveBack,
            "y",
            clickedViewCoordinates.second
        )

        // Get initial width and margins of viewToMoveBack
        val initialWidth = viewToMoveBack.width
        val initialLeftMargin = (viewToMoveBack.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
        val initialRightMargin = (viewToMoveBack.layoutParams as ViewGroup.MarginLayoutParams).rightMargin

        // Target width and margins based on clickedView
        val targetWidth = clickedView.width
        val targetLeftMargin = (clickedView.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
        val targetRightMargin = (clickedView.layoutParams as ViewGroup.MarginLayoutParams).rightMargin

        // Create ValueAnimator to animate width
        val widthAnimator = ValueAnimator.ofInt(initialWidth, targetWidth)
        widthAnimator.addUpdateListener { animation ->
            val animatedWidth = animation.animatedValue as Int
            val layoutParams = viewToMoveBack.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = animatedWidth
            viewToMoveBack.layoutParams = layoutParams
        }

        // Create ValueAnimator to animate left and right margins
        val marginAnimator = ValueAnimator.ofInt(initialLeftMargin, targetLeftMargin)
        marginAnimator.addUpdateListener { animation ->
            val animatedMargin = animation.animatedValue as Int
            val layoutParams = viewToMoveBack.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.leftMargin = animatedMargin
            layoutParams.rightMargin = animatedMargin
            viewToMoveBack.layoutParams = layoutParams
        }

        animatorSet.playTogether(moveUp, widthAnimator, marginAnimator)
        animatorSet.duration = 500
        animatorSet.start()
    }

    private fun moveViewToFront(
        clickedView: View,
        clickedViewCoordinates: Pair<Float, Float>,
        viewAtFront: View,
        viewAtFrontCoordinates: Pair<Float, Float>,
        onEndAction: ((clickedView: View, frontView: View) -> Unit)? = null
    ) {
        val animatorSet = AnimatorSet()

        // Get the initial width and margins of the clicked view
        val initialWidth = clickedView.width
        val initialMargins = (clickedView.layoutParams as ViewGroup.MarginLayoutParams).leftMargin

        // Target width and margins based on the viewAtFront
        val targetWidth = viewAtFront.width
        val targetMargins = (viewAtFront.layoutParams as ViewGroup.MarginLayoutParams).leftMargin

        // Create ValueAnimator to animate width
        val widthAnimator = ValueAnimator.ofInt(initialWidth, targetWidth)
        widthAnimator.addUpdateListener { animation ->
            val animatedWidth = animation.animatedValue as Int
            val layoutParams = clickedView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = animatedWidth
            clickedView.layoutParams = layoutParams
        }

        // Animate margins (left and right together)
        val marginAnimator = ValueAnimator.ofInt(initialMargins, targetMargins)
        marginAnimator.addUpdateListener { animation ->
            val animatedMargin = animation.animatedValue as Int
            val layoutParams = clickedView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.leftMargin = animatedMargin
            layoutParams.rightMargin = animatedMargin
            clickedView.layoutParams = layoutParams
        }

        // Move clicked view to the front position (animate translationY)
        val moveFront = ObjectAnimator.ofFloat(
            clickedView,
            "y",
            viewAtFrontCoordinates.second,
        )

        animatorSet.playTogether(moveFront, widthAnimator, marginAnimator)
        animatorSet.duration = 500 // Duration in milliseconds

        // Handle animation end action
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                onEndAction?.invoke(viewAtFront, clickedView)
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        animatorSet.start()
        clickedView.bringToFront() // this will bring the clicked view to the front to the list and set its z-index to 1
        clickedView.z = 1f
    }



}
