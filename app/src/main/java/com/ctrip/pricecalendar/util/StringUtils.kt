package com.ctrip.pricecalendar.util

import android.text.TextUtils

/**
 * @author Zhenhua on 2018/2/1.
 * @email zhshan@ctrip.com ^.^
 */
object StringUtils {
    private val PAD_LIMIT = 8192

    @JvmStatic
    fun leftPad(str: String?, size: Int, padStr: String): String? {
        var padStr = padStr
        if (str == null) {
            return null
        }
        if (TextUtils.isEmpty(padStr)) {
            padStr = " "
        }
        val padLen = padStr.length
        val strLen = str.length
        val pads = size - strLen
        if (pads <= 0) {
            return str // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr[0])
        }

        if (pads == padLen) {
            return padStr + str
        } else if (pads < padLen) {
            return padStr.substring(0, pads) + str
        } else {
            val padding = CharArray(pads)
            val padChars = padStr.toCharArray()
            for (i in 0 until pads) {
                padding[i] = padChars[i % padLen]
            }
            return String(padding) + str
        }
    }

    @JvmStatic
    fun leftPad(str: String?, size: Int, padChar: Char): String? {
        if (str == null) {
            return null
        }
        val pads = size - str.length
        if (pads <= 0) {
            return str // returns original String when possible
        }
        return if (pads > PAD_LIMIT) {
            leftPad(str, size, padChar.toString())
        } else padding(pads, padChar) + str
    }

    private fun padding(repeat: Int, padChar: Char): String {
        if (repeat < 0) {
            throw IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat)
        }
        val buf = CharArray(repeat)
        for (i in buf.indices) {
            buf[i] = padChar
        }
        return String(buf)
    }
}