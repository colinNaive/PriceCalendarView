package com.ctrip.pricecalendar.component

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

/**
 * Created by helun on 2018/3/15.
 */
class CustomHorizontalScrollView : HorizontalScrollView {

    private var mOnOverScrolledListener: CustomHorizontalScrollView.OnOverScrolledListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        mOnOverScrolledListener?.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }

    override fun fling(velocityX: Int) {
        super.fling(velocityX / 4)
    }

    interface OnOverScrolledListener {
        fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean)
    }

    fun setOnOverScrolledListener(listener: CustomHorizontalScrollView.OnOverScrolledListener) {
        mOnOverScrolledListener = listener
    }

}