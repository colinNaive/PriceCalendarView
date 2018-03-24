package com.ctrip.pricecalendar.bean

import java.io.Serializable

/**
 * @author Zhenhua on 2018/2/28.
 * @email zhshan@ctrip.com ^.^
 */
class ProductPrice : Serializable {
    var alias: String? = null
    var inventory = 0
    var price: Int? = 0
    var productId: Int? = 0
}