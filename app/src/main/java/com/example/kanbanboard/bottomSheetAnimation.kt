package com.example.kanbanboard
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd

fun bottomSheetAnimation(bottomSheet: View) {
    bottomSheet.post {
        val maxHeight = bottomSheet.height.takeIf { it > 0 } ?: 400 // Default if height isn't measured

        if (bottomSheet.visibility == View.VISIBLE) {
            // Slide down and hide
            ObjectAnimator.ofFloat(bottomSheet, "translationY", 0f, maxHeight.toFloat()).apply {
                duration = 150
                start()
                doOnEnd {
                    bottomSheet.visibility = View.GONE
                    bottomSheet.layoutParams.height = 0
                    bottomSheet.requestLayout()
                }
            }
        } else {
            // Ensure visibility before animation
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            bottomSheet.requestLayout()
            bottomSheet.visibility = View.VISIBLE

            // Slide up smoothly
            ObjectAnimator.ofFloat(bottomSheet, "translationY", maxHeight.toFloat(), 0f).apply {
                duration = 150
                start()
            }
        }
    }
}



