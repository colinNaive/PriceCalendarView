package com.ctrip.pricecalendar.bean

import java.io.Serializable

/**
 * @author Zhenhua on 2018/2/2.
 * @email zhshan@ctrip.com ^.^
 */
class Festival : Serializable {
    var festivalDate: String? = null
    var festivalName: String? = null
    var festivalType: Int = 0
    private var isMainFestival: Boolean = false
    var orderShow: Boolean = true

    fun setIsMainFestival(isMainFestival: Boolean) {
        this.isMainFestival = isMainFestival
    }

    fun getIsMainFestival(): Boolean {
        return this.isMainFestival
    }
}