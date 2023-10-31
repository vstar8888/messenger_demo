@file:JvmName("ColorChangingAnimation")
package ru.demo.messenger.helpers

import android.animation.ValueAnimator
import androidx.annotation.ColorInt
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import android.view.View

fun change(view: View, @ColorInt startColor: Int, @ColorInt endColor: Int) {
    val evaluator = ArgbEvaluator()
    val animator = ValueAnimator()
    animator.setIntValues(startColor, endColor)
    animator.setEvaluator(evaluator)
    animator.addUpdateListener { animation -> view.setBackgroundColor(animation.animatedValue as Int) }
    animator.start()
}
