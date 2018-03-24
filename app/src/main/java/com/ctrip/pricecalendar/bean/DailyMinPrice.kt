package com.ctrip.pricecalendar.bean

import java.io.Serializable

/**
 * @author Zhenhua on 2018/2/2.
 * @email zhshan@ctrip.com ^.^
 */
class DailyMinPrice : Serializable {
    var date: String? = null
    var inventory: Int = 0
    var price: Int = 0
    var productPrices: List<ProductPrice>? = null
}