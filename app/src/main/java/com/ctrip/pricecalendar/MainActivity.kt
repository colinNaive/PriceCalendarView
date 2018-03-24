package com.ctrip.pricecalendar

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var tv = findViewById<TextView>(R.id.click)
        startFlick(tv)
    }

    fun test(view: View) {
        var intent = Intent(this, PriceCalendarActivity().javaClass)
        startActivity(intent)
    }

    private fun startFlick(tv: TextView?) {
        var alphaAnimation = AlphaAnimation(1f, 0.1f)
        alphaAnimation.setDuration(100)
        alphaAnimation.setInterpolator(LinearInterpolator())
        alphaAnimation.setRepeatCount(8)
        alphaAnimation.setRepeatMode(Animation.REVERSE)
        tv?.startAnimation(alphaAnimation)
    }
}
