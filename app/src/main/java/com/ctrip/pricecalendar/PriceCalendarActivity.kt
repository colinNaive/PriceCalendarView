package com.ctrip.pricecalendar

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.ctrip.pricecalendar.bean.DailyMinPrice
import com.ctrip.pricecalendar.bean.PriceCalendarInfo
import com.ctrip.pricecalendar.component.PriceCalendarView
import com.ctrip.pricecalendar.util.StringUtils
import java.util.*

/**
 * @author Zhenhua on 2018/3/21.
 * @email zhshan@ctrip.com ^.^
 */
class PriceCalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        Handler().postDelayed(Runnable {
            initPriceCalendarView()
        }, 4000)
    }

    private fun initPriceCalendarView() {
        var map = TreeMap<String, PriceCalendarInfo>()

        //构建数据源
        for (i in 1..12) {
            var priceCalendarInfo = PriceCalendarInfo()
            var dailyMinPrices = ArrayList<DailyMinPrice>()
            for (j in 1..17) {
                var dailyMinPrice = DailyMinPrice()
                dailyMinPrice.date = String.format("2018-%s-%s", StringUtils.leftPad(i.toString(), 2, "0"), StringUtils.leftPad(j.toString(), 2, "0"))
                dailyMinPrice.price = Random().nextInt(100)
                dailyMinPrice.inventory = Random().nextInt(16)
                dailyMinPrices.add(dailyMinPrice)
            }
            priceCalendarInfo.dailyMinPrices = dailyMinPrices
            var yearMonth = String.format("2018-%s", StringUtils.leftPad(i.toString(), 2, "0"))
            map.put(yearMonth, priceCalendarInfo)
        }

        //初始化priceCalendarView
        var priceCalendarView = findViewById<PriceCalendarView>(R.id.calendarView)
        priceCalendarView.setMinDate("2018-04-10")
        priceCalendarView.setMaxDate("2018-12-30")
        priceCalendarView.setPriceDataSource(map)
        priceCalendarView.setLastVisitDate("2018-08-10")
        priceCalendarView.stopLoading()
        priceCalendarView.init(object : PriceCalendarView.DatePickerController {
            override fun onViewCallBack(view: View?) {

            }

            override fun onDayOfMonthSelected(year: Int, month: Int, day: Int, priceCalendarInfo: PriceCalendarInfo?) {
                try {
                    //得到价格
                    var minPrices = priceCalendarInfo!!.dailyMinPrices
                            ?: java.util.ArrayList<DailyMinPrice>()
                    val priceDate = String.format("%s-%s-%s", year,
                            StringUtils.leftPad(month.toString() + "", 2,
                                    "0"), StringUtils.leftPad(day.toString(), 2, "0"))

                    for (i in minPrices.indices) {
                        if (TextUtils.equals(minPrices[i].date, priceDate)) {
                            //得到出发日期
                            var toastStr = "选中日期=" + minPrices[i].date + ",选中价格=" + minPrices[i].price + ",库存=" + minPrices[i].inventory
                            Toast.makeText(this@PriceCalendarActivity, toastStr, Toast.LENGTH_SHORT)
                        }
                    }
                } catch (e: Exception) {
                }
            }

        })
    }
}