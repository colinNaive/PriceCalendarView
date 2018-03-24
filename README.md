###一. 引言
最近刚刚实现了日历页，感触颇多。这个日历页跟我之前开发的任何一个页面都不太像，这个页面的所有模块之间都有关联，一个模块的数据层有任何修改，其他模块都要跟着改变，关联性非常强。具体如下图所示：
<img src="http://img.blog.csdn.net/20180324102133580?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY29saW5hbmRyb2lk/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="60%" height="30%" />
这只是一个基本流程的截图，当满足某些条件还会出现“帮我下单”等模块。这里说一下我对开发完这个页面的感想：
####1. MVP架构的优势完全凸显出来。
（1）可以看到这些模块几乎都是关联的，我这里列举一个场景——当用户选择了一个日期，（a）多线路模块会刷新所有线路信息并根据日期属性判断是否自动选择某一个线路（b）多行程模块会根据该班期刷新日期和成团信息，（c）优惠券会根据多线路模块及选择的班期确定请求参数重新请求优惠券数据并刷新UI，（d）成人儿童数会根据班期信息展示不同默认值、最大最小值、成人儿童婴儿数关联规则，人数选择模块的tips信息也要跟着刷新，（e）预定按钮会根据人数模块、线路模块及选择的日期确定是否展示，并且人数、线路、日期任意数据发生变化都要重新请求预定按钮状态信息。
（2）试想，如果不是采用MVP架构，这些所有模块夹杂在一起，如果哪天我离职了，接手我任务的同事维护起来得有多抓狂。
（3）在请求日历信息时，需要逐月请求每个月的信息，然后再拼接到一起，也就是说如果有效日期是12个月，那就要请求12个接口再把12个接口的数据拼接到一起。正是由于MVP架构，我只需要在model层做具体处理，p层看到的就只是发起了一个网络请求，而不用在意具体我如何发送这个12个请求，也不用关系我数据如何进行拼接的。
###二. 自定义价格日历
哈哈，引言写的有些长。。做这个页面真是各种心酸，交互实在太复杂了。。这里我先给一个价格日历的效果图，先给大家看下效果。
<img src="http://img.blog.csdn.net/20180324104600417?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY29saW5hbmRyb2lk/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="60%" height="30%" />
####该价格日历主要有以下功能
（1）展示节假日、价格、日期、库存；
（2）实现日历横向滑动；
（3）**支持选择默认班期**，如效果图中默认选择班期是“2018-11-08”，所以进到日历加载完成后会自动锚点到11月，并选中该月日期；
（4）支持起始日期和结束日期，如果起始日期前或结束日期后有价格也置灰；
（5）自动调整每个月的高度；
（6）带有loading图。
这里附上<font color=red>**[该demo的github地址](https://github.com/colinNaive/PriceCalendarView)**</font>，欢迎下载使用。如使用人多，我会再具体介绍一下我的实现方法。
