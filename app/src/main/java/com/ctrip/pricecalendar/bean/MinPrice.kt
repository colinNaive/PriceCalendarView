package com.ctrip.pricecalendar.bean

import java.io.Serializable

/**
 * @author Zhenhua on 2018/2/2.
 * @email zhshan@ctrip.com ^.^
 */
class MinPrice : Serializable {
    var date: String? = null
    var price: Int = 0
    var priceRemark: String? = null
    var productId: Int = 0
}