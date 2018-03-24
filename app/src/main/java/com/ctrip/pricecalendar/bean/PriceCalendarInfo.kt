package com.ctrip.pricecalendar.bean

import java.io.Serializable

/**
 * @author Zhenhua on 2018/2/2.
 * @email zhshan@ctrip.com ^.^
 */
class PriceCalendarInfo : Serializable {
    var dailyMinPrices: ArrayList<DailyMinPrice>? = null
    var festivals: List<Festival>? = null
}