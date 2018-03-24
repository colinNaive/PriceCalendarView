package com.ctrip.pricecalendar.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctrip.pricecalendar.R;
import com.ctrip.pricecalendar.bean.DailyMinPrice;
import com.ctrip.pricecalendar.bean.Festival;
import com.ctrip.pricecalendar.bean.PriceCalendarInfo;
import com.ctrip.pricecalendar.component.indicatorView.IndicatorView;
import com.ctrip.pricecalendar.util.CommonUtil;
import com.ctrip.pricecalendar.util.DateUtils;
import com.ctrip.pricecalendar.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Zhenhua on 2018/1/31.
 * @email zhshan@ctrip.com ^.^
 */
public class PriceCalendarView extends FrameLayout {

    private CustomViewPager mViewPager;//高度可变的ViewPager
    private Context mContext;
    private LinearLayout headerLayout;
    private CustomHorizontalScrollView mHorizontalScrollView;//避免横向ScrollView快速滑动
    private IndicatorView mIndicatorView;
    private LinearLayout mLoadingLayout;

    private SparseArray<View> mViewMap = new SparseArray<>();

    private Map<String, PriceCalendarInfo> mYearMonthMap;
    private DatePickerController mController;
    private CalendarAdapter adapter;
    private Date maxDate;
    private Date minDate;
    private int lastVisitDateIndex = -1;
    private State currentSate = new State();

    //用于HorizontalScrollView导航栏跟随ViewPager滑动
    private int leftInvisibleNum = 0;
    private int visibleNum = 0;
    private int rightInvisibleNum = 0;
    private int singleItemWidth = 0;
    private int totalWidth = 0;
    private int localPos = 0;
    private int totalSize = 0;
    private String lastVisitDate;
    private MyGridView defaultGridView;//wrap_content
    private int defaultPosition;

    public void setMaxDate(String maxDateString) {
        this.maxDate = DateUtils.stringtoDate(maxDateString, "yyyy-MM-dd");
    }

    public void setMinDate(String minDateString) {
        this.minDate = DateUtils.stringtoDate(minDateString, "yyyy-MM-dd");
    }

    public void setPriceDataSource(TreeMap<String, PriceCalendarInfo> priceDataSource) {
        this.mYearMonthMap = priceDataSource;
    }

    public void startLoading() {
        this.post(new Runnable() {
            @Override
            public void run() {
                mLoadingLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void stopLoading() {
        this.post(new Runnable() {
            @Override
            public void run() {
                mLoadingLayout.setVisibility(View.GONE);
            }
        });
    }

    public void setLastVisitDate(String lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public PriceCalendarView(Context context) {
        this(context, null);
    }

    public PriceCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PriceCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void init(DatePickerController controller) {
        try {
            initController(controller);
            lastVisitDateIndex = initHeader();
            initViewPager();
            this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (defaultGridView == null) {
                        return;
                    }
                    AdapterView.OnItemClickListener onItemClickListener = defaultGridView.getOnItemClickListener();
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(defaultGridView, defaultGridView.getChildAt(defaultPosition), defaultPosition, 0);
                    }
                    if (lastVisitDateIndex != -1) {
                        mViewPager.setCurrentItem(lastVisitDateIndex);
                    }
                }
            }, 800);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViewPager() {
        adapter = new CalendarAdapter(mContext);
        mViewPager.resetCurPosition();
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setOffscreenPageLimit(getCount());
        CommonUtil.controlViewPagerSpeed(mContext, mViewPager, 1000);
        mViewPager.setAdapter(adapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //postion是目前所在位置，为整数：0,1,2,3,5...，positionOffset是位置偏移量，
                // positionOffsetPixels是偏移的像素。

                //设置红色底线的位置，setX是相对于父控件的X位置。（position+positionOffset）*底线宽度就
                //是底线应该在具体位置（相对于父控件的具体位置
                //pos是静止时的页号，或者是要去往的页号
                if (positionOffset == 0.0f) {
                    localPos = position;
                }
                //当要翻页时，进行判断和滑动
                if (positionOffset != 0.0f) {
                    if (position < localPos) {//如果要去往的页面是在当前页面的左边
                        localPos = position;
                        if (localPos < leftInvisibleNum)//如果去往的页面是未可见
                        {
                            //mHorizontalScrollView向右滑动
                            mHorizontalScrollView.smoothScrollBy(singleItemWidth * -1, 0);
                            computeScrollParam();//计算TextView的数量
                        }

                    } else {//如果要去往的页面是在当前页面的右边
                        localPos++;
                        if (totalSize - 1 - localPos < rightInvisibleNum)//如果去往的页面是未可见，7为最后一个页面的页号
                        {
                            //mHorizontalScrollView向左滑动
                            mHorizontalScrollView.smoothScrollBy(singleItemWidth, 0);
                            computeScrollParam();
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                mViewPager.setCurPosition(position);
                mViewPager.requestLayout();
                mIndicatorView.smoothSlideTo(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void computeScrollParam() {
        try {
            int scrollX = mHorizontalScrollView.getScrollX();
            leftInvisibleNum = 0;
            if (scrollX != 0) {
                leftInvisibleNum = scrollX / singleItemWidth + (scrollX % singleItemWidth == 0 ? 0 : 1);
            }
            visibleNum = (totalWidth - scrollX % singleItemWidth) / singleItemWidth;
            rightInvisibleNum = totalSize - leftInvisibleNum - visibleNum;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initController(DatePickerController controller) {
        mController = controller;
    }

    private int initHeader() {
        int result = -1;
        try {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(CommonUtil.dp2px(mContext, 90.33f), ViewGroup.LayoutParams.MATCH_PARENT);
            totalSize = getCount();//总共有多少个月
            headerLayout.removeAllViews();
            singleItemWidth = CommonUtil.dp2px(mContext, 90.33f);
            mIndicatorView.init(totalSize, singleItemWidth);
            totalWidth = CommonUtil.getScreenWidth(mContext);
            for (int i = 0; i < totalSize; i++) {
                final int index = i;
                View view = LayoutInflater.from(mContext).inflate(R.layout.cttour_calendar_header_item, null);
                TextView month = (TextView) view.findViewById(R.id.month);
                TextView price = (TextView) view.findViewById(R.id.price);
                String minPrice = getHeaderMonthMinPriceStr(i);
                if (containsLastVisit(lastVisitDate) && lastVisitDate.startsWith(getMonthStr(i, "%s-%s") + "-")) {
                    result = i;
                }
                price.setText(minPrice);
                final String monthStr = getMonthStr(index);
                month.setText(monthStr);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index);
                    }
                });
                headerLayout.addView(view, params);
            }
            mHorizontalScrollView.setOnOverScrolledListener(new CustomHorizontalScrollView.OnOverScrolledListener() {
                @Override
                public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
                    leftInvisibleNum = 0;
                    if (scrollX != 0) {
                        //如果左边未见的TextView没有整数数量的话，就取整加1
                        leftInvisibleNum = scrollX / singleItemWidth + (scrollX % singleItemWidth == 0 ? 0 : 1);
                    }
                    visibleNum = (totalWidth - scrollX % singleItemWidth) / singleItemWidth;
                    rightInvisibleNum = totalSize - leftInvisibleNum - visibleNum;
                }
            });
            //导航栏初始化
            leftInvisibleNum = 0;
            visibleNum = totalWidth / singleItemWidth;
            rightInvisibleNum = totalSize - visibleNum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getHeaderMonthMinPriceStr(int position) {
        String result = getMonthMinPriceStr(position);
        if (!TextUtils.equals(result, "实时计价") && !TextUtils.equals(result, "无班期")) {
            result += "起";
        }
        return result;
    }

    //每个月最低价格
    private String getMonthMinPriceStr(int position) {
        String monthStr = getMonthStr(position, "%s-%s");
        PriceCalendarInfo priceCalendarInfo = mYearMonthMap.get(monthStr);
        if (priceCalendarInfo == null || priceCalendarInfo.getDailyMinPrices() == null || priceCalendarInfo.getDailyMinPrices().size() == 0) {
            return "无班期";
        }
        List<DailyMinPrice> dailyMinPrices = priceCalendarInfo.getDailyMinPrices();
        //找出当月最小值
        int minPrice = getMinPrice(dailyMinPrices);
        if (minPrice > 0) {
            return "¥" + minPrice;
        } else {
            return "实时计价";
        }
    }

    //给出lastVisitDate，得到它存在于月数组的index
    private boolean containsLastVisit(String lastVisitDate) {
        //lastVisitDate为空
        if (TextUtils.isEmpty(lastVisitDate)) {
            return false;
        }
        //lastVisitDate不符合规则"%s-%s-%s"
        String[] parts = lastVisitDate.split("-");
        if (parts.length != 3) {
            return false;
        }
        String yearMonth = String.format("%s-%s", StringUtils.leftPad(parts[0], 2, "0"), StringUtils.leftPad(parts[1], 2, "0"));
        if (!mYearMonthMap.containsKey(yearMonth)) {
            return false;
        }
        PriceCalendarInfo priceCalendarInfo = mYearMonthMap.get(yearMonth);
        if (priceCalendarInfo == null || priceCalendarInfo.getDailyMinPrices() == null) {
            return false;
        }
        List<DailyMinPrice> minPrices = priceCalendarInfo.getDailyMinPrices();
        String revisedLastVisitDate = String.format("%s-%s-%s", StringUtils.leftPad(parts[0], 2, "0"), StringUtils.leftPad(parts[1], 2, "0"), StringUtils.leftPad(parts[2], 2, "0"));
        for (int i = 0; i < minPrices.size(); i++) {
            if (minPrices.get(i) != null && TextUtils.equals(minPrices.get(i).getDate(), revisedLastVisitDate)) {
                return true;
            }
        }
        return false;
    }

    //找出当月最小值
    private int getMinPrice(List<DailyMinPrice> dailyMinPrices) {
        //第一个有值的数
        int minPrice = getFirstValue(dailyMinPrices);
        //遍历
        for (int i = 0; i < dailyMinPrices.size(); i++) {
            if (isLess(dailyMinPrices.get(i), minPrice)) {
                minPrice = dailyMinPrices.get(i).getPrice();
            }
        }
        return minPrice;
    }

    private int getFirstValue(List<DailyMinPrice> dailyMinPrices) {
        int result = 0;
        if (dailyMinPrices == null) {
            return 0;
        }
        for (int i = 0; i < dailyMinPrices.size(); i++) {
            if (dailyMinPrices.get(i) != null && dailyMinPrices.get(i).getPrice() > 0) {
                return dailyMinPrices.get(i).getPrice();
            }
        }
        return result;
    }

    //找出当月最小值
    private boolean isLess(DailyMinPrice dailyMinPrice, int minPrice) {
        return dailyMinPrice != null && !TextUtils.isEmpty(dailyMinPrice.getDate()) && dailyMinPrice.getPrice() < minPrice && dailyMinPrice.getPrice() > 0;
    }

    //"%s年-%s月"
    private String getMonthStr(int position, String formattedStr) {
        int firstMonth = -1;
        int startYear = -1;
        if (minDate != null) {
            firstMonth = DateUtils.getMonth(minDate) - 1;
            startYear = DateUtils.getYear(minDate);
        }
        int year = position / 12 + startYear + ((firstMonth + (position % 12)) / 12);
        int month = (firstMonth + (position % 12)) % 12;
        return String.format(formattedStr, year, StringUtils.leftPad(String.valueOf(month + 1), 2, "0"));
    }

    //"%月"
    private String getMonthStr(int position) {
        int firstMonth = -1;
        if (minDate != null) {
            firstMonth = DateUtils.getMonth(minDate) - 1;
        }
        int month = (firstMonth + (position % 12)) % 12 + 1;
        return month + "月";
    }

    private String getMonthStrForBuryPoint(String monthStr) {
        try {
            return monthStr.substring(0, monthStr.indexOf("月"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private int getCount() {
        int maxYear = -1;
        int minYear = -1;
        int firstMonth = -1;
        int lastMonth = -1;
        if (maxDate != null) {
            maxYear = DateUtils.getYear(maxDate);
        }
        if (minDate != null) {
            minYear = DateUtils.getYear(minDate);
        }
        if (minDate != null) {
            firstMonth = DateUtils.getMonth(minDate) - 1;
        }
        if (maxDate != null) {
            lastMonth = DateUtils.getMonth(maxDate) - 1;
        }

        //总共多少个月
        int itemCount = (maxYear - minYear + 1) * 12;
        //减起始月份
        if (firstMonth != -1) {
            itemCount -= firstMonth;
        }
        //减结束月份
        if (lastMonth != -1) {
            itemCount -= (12 - lastMonth) - 1;
        }
        return itemCount;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = LayoutInflater.from(mContext).inflate(R.layout.cttour_calendar_price_layout, this, true);
        this.mViewPager = (CustomViewPager) view.findViewById(R.id.viewPager);
        this.headerLayout = (LinearLayout) view.findViewById(R.id.header_calendar);
        this.mHorizontalScrollView = (CustomHorizontalScrollView) view.findViewById(R.id.horizontal_scrollview);
        this.mIndicatorView = (IndicatorView) view.findViewById(R.id.indicator);
        this.mLoadingLayout = (LinearLayout) view.findViewById(R.id.price_calendar_loading);
    }

    class CalendarAdapter extends PagerAdapter implements AdapterView.OnItemClickListener {

        protected static final int MONTHS_IN_YEAR = 12;
        private final Calendar calendar = Calendar.getInstance();
        private Integer firstMonth = calendar.get(Calendar.MONTH);
        private LayoutInflater inflater;
        private Integer lastMonth = (calendar.get(Calendar.MONTH) - 1) % MONTHS_IN_YEAR;
        private Integer startYear = calendar.get(Calendar.YEAR);

        public CalendarAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            mContext = context;
            if (maxDate != null) {
                lastMonth = DateUtils.getMonth(maxDate) - 1;
            }
            if (minDate != null) {
                startYear = DateUtils.getYear(minDate);
                firstMonth = DateUtils.getMonth(minDate) - 1;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int year = position / MONTHS_IN_YEAR + startYear + ((firstMonth + (position % MONTHS_IN_YEAR)) / MONTHS_IN_YEAR);
            int month = (firstMonth + (position % MONTHS_IN_YEAR)) % MONTHS_IN_YEAR;
            return String.format("%s年%s月", year, StringUtils.leftPad(String.valueOf(month + 1), 2, "0"));
        }

        @Override
        public int getCount() {
            int maxYear = 0;
            int minYear = 0;
            if (maxDate != null) {
                maxYear = DateUtils.getYear(maxDate);
            }
            if (minDate != null) {
                minYear = DateUtils.getYear(minDate);
            }

            int itemCount = (maxYear - minYear + 1) * MONTHS_IN_YEAR;

            if (firstMonth != -1)
                itemCount -= firstMonth;

            if (lastMonth != -1)
                itemCount -= (MONTHS_IN_YEAR - lastMonth) - 1;
            return itemCount;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViewMap.get(position);
            if (view == null) {
                view = inflater.inflate(R.layout.item_page_month_day_layout, container, false);
                mViewMap.put(position, view);
            }
            int year = position / MONTHS_IN_YEAR + startYear + ((firstMonth + (position % MONTHS_IN_YEAR)) / MONTHS_IN_YEAR);
            int month = (firstMonth + (position % MONTHS_IN_YEAR)) % MONTHS_IN_YEAR;
            List<DateBean> list = new ArrayList<DateBean>();
            GregorianCalendar c = new GregorianCalendar(year, month, 0);
            //返回当前月的总天数
            int days = DateUtils.getDaysOfMonth(year, month + 1);
            //返回当月第一天是星期几
            int dayOfWeeks = c.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeeks == 7) {
                dayOfWeeks = 0;
            }
            for (int i = 0; i < days + dayOfWeeks; i++) {
                DateBean dateBean = new DateBean(year, month + 1, i, false);
                list.add(dateBean);
            }
            MyGridView mGridView = (MyGridView) view.findViewById(R.id.grid_view);
            mGridView.setOnItemClickListener(this);
            mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
            if (containsLastVisit(lastVisitDate) && lastVisitDate.startsWith(StringUtils.leftPad(year + "", 2, "0") + "-" + StringUtils.leftPad(month + 1 + "", 2, "0") + "-")) {
                defaultGridView = mGridView;
                defaultPosition = dayOfWeeks + getDayInt() - 1;
            }

            //每个月最低价格
            String minPrice = getMonthMinPriceStr(position);
            mGridView.setAdapter(new MyGridAdapter(year, month + 1, days, dayOfWeeks, list, minPrice));
            container.addView(view);
            return view;
        }

        //由last
        private int getDayInt() {
            String dayStr = "0";
            if (lastVisitDate.indexOf("-") != -1) {
                dayStr = lastVisitDate.substring(lastVisitDate.lastIndexOf("-") + 1, lastVisitDate.length());
            }
            int dayInt = 0;
            try {
                dayInt = Integer.valueOf(dayStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return dayInt;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MyGridAdapter gridAdapter = (MyGridAdapter) parent.getAdapter();
            int day = (int) gridAdapter.getItem(position);
            if (day == -1) {
                return;
            }
            if (!view.isEnabled() || isSameView(parent, view)) {
                return;
            }
            //UI变化start
            setUI(parent, view, position);

            //数据暴露出去
            DateBean bean = gridAdapter.getDateBeanList().get(position);
            PriceCalendarInfo priceCalendarInfo = gridAdapter.getPriceCalendarInfo();
            if (mController != null && priceCalendarInfo != null) {
                mController.onDayOfMonthSelected(bean.currentYear, bean.currentMonth, day + 1, priceCalendarInfo);
                mController.onViewCallBack(view);
            }
        }

        private void setUI(AdapterView<?> parent, View view, int position) {
            //设置当前状态&记录上一次状态
            State lastState = setState(parent, view, position);
            //恢复上一个状态
            restoreLastState(lastState);
            //设置当前状态
            setCurrentState(view);
        }

        private boolean isSameView(AdapterView<?> parent, View view) {
            return currentSate.view == view && parent == currentSate.parent;
        }

        private State setState(AdapterView<?> parent, View view, int position) {
            //设置上一个状态
            State lastState = new State(currentSate.position, currentSate.parent, currentSate.view, currentSate.dayColor, currentSate.priceColor, currentSate.festivalColor, currentSate.stockColor);

            //设置当前状态
            currentSate.parent = parent;
            currentSate.view = view;
            currentSate.position = position;
            currentSate.dayColor = ((TextView) view.findViewById(R.id.day_tv)).getCurrentTextColor();
            currentSate.festivalColor = ((TextView) view.findViewById(R.id.festival)).getCurrentTextColor();
            currentSate.priceColor = ((TextView) view.findViewById(R.id.price_tv)).getCurrentTextColor();
            currentSate.stockColor = ((TextView) view.findViewById(R.id.stock_tv)).getCurrentTextColor();
            return lastState;
        }

        private void restoreLastState(State lastState) {
            if (lastState != null && lastState.parent != null) {
                lastState.parent.getChildAt(lastState.position).setActivated(false);
                ((TextView) lastState.parent.getChildAt(lastState.position).findViewById(R.id.day_tv)).setTextColor(lastState.dayColor);
                ((TextView) lastState.parent.getChildAt(lastState.position).findViewById(R.id.price_tv)).setTextColor(lastState.priceColor);
                ((TextView) lastState.parent.getChildAt(lastState.position).findViewById(R.id.festival)).setTextColor(lastState.festivalColor);
                ((TextView) lastState.parent.getChildAt(lastState.position).findViewById(R.id.stock_tv)).setTextColor(lastState.stockColor);
            }
        }

        private void setCurrentState(View view) {
            view.setActivated(true);
            ((TextView) view.findViewById(R.id.day_tv)).setTextColor(Color.parseColor("#ffffff"));
            ((TextView) view.findViewById(R.id.price_tv)).setTextColor(Color.parseColor("#ffffff"));
            ((TextView) view.findViewById(R.id.festival)).setTextColor(Color.parseColor("#ffffff"));
            ((TextView) view.findViewById(R.id.stock_tv)).setTextColor(Color.parseColor("#ffffff"));
        }
    }

    private static class State {

        public State() {
        }

        public State(int position, AdapterView parent, View view, int dayColor, int priceColor, int festivalColor, int stockColor) {
            this.position = position;
            this.parent = parent;
            this.dayColor = dayColor;
            this.priceColor = priceColor;
            this.festivalColor = festivalColor;
            this.stockColor = stockColor;
            this.view = view;
        }

        int position;
        AdapterView parent;
        View view;
        int dayColor;
        int priceColor;
        int festivalColor;
        int stockColor;
    }

    class MyGridAdapter extends BaseAdapter {
        private List<DateBean> dateBeanList;
        private int days;
        private int dayOfWeeks;
        private String yearMonth;
        private PriceCalendarInfo mPriceCalendarInfo;
        private String minPrice;
        private String todayStr = DateUtils.currDay();
        private String tomorrowStr = DateUtils.afterDay(1);
        private String afterTomorrowStr = DateUtils.afterDay(2);

        public List<DateBean> getDateBeanList() {
            return dateBeanList;
        }

        public MyGridAdapter(int year, int month, int days, int dayOfWeeks, List<DateBean> dateBeanList, String minPrice) {
            this.dateBeanList = dateBeanList;
            this.yearMonth = String.format("%s-%s", year, StringUtils.leftPad(month + "", 2, "0"));
            if (mYearMonthMap != null) {
                this.mPriceCalendarInfo = mYearMonthMap.get(yearMonth);
            }
            this.days = days;
            this.dayOfWeeks = dayOfWeeks;
            this.minPrice = minPrice;
        }

        public PriceCalendarInfo getPriceCalendarInfo() {
            return mPriceCalendarInfo;
        }

        @Override
        public int getCount() {
            return days + dayOfWeeks;
        }

        @Override
        public Object getItem(int i) {
            if (i < dayOfWeeks) {
                return -1;
            } else {
                return i - dayOfWeeks;
            }
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            GridViewHolder viewHolder;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.cttour_price_calendar_item_day, viewGroup, false);
                viewHolder = new GridViewHolder();
                viewHolder.mDay = (TextView) view.findViewById(R.id.day_tv);
                viewHolder.mPriceTv = (TextView) view.findViewById(R.id.price_tv);
                viewHolder.mFestival = (TextView) view.findViewById(R.id.festival);
                viewHolder.mStock = (TextView) view.findViewById(R.id.stock_tv);
                view.setTag(viewHolder);
            } else {
                viewHolder = (GridViewHolder) view.getTag();
            }

            //数据源
            DateBean dateBean = dateBeanList.get(i);
            int item = (int) getItem(dateBean.pos);
            DailyMinPrice dailyMinPrice = containPriceDate(dateBean.currentYear, dateBean.currentMonth, item + 1);
            Festival festival = containFestivalDate(dateBean.currentYear, dateBean.currentMonth, item + 1);

            if (item == -1) {//上个月的item占位
                viewHolder.mDay.setText("");
                viewHolder.mPriceTv.setText("");
                viewHolder.mFestival.setText("");
                viewHolder.mStock.setText("");
                view.setEnabled(false);
            } else {//这个月的item
                view.setActivated(false);
                view.setEnabled(true);
                String day = String.valueOf(item + 1);
                String dayFullStr = yearMonth + "-" + StringUtils.leftPad(day, 2, "0");
                if (TextUtils.equals(dayFullStr, todayStr)) {
                    viewHolder.mDay.setText("今天");
                } else if (TextUtils.equals(dayFullStr, tomorrowStr)) {
                    viewHolder.mDay.setText("明天");
                } else if (TextUtils.equals(dayFullStr, afterTomorrowStr)) {
                    viewHolder.mDay.setText("后天");
                } else {
                    viewHolder.mDay.setText(day);
                }
                viewHolder.mPriceTv.setText("");
                if (dailyMinPrice != null) {//有价格
                    viewHolder.mDay.setSelected(dateBean.selected);
                    view.setEnabled(true);
                    viewHolder.mDay.setEnabled(true);
                    viewHolder.mPriceTv.setVisibility(VISIBLE);
                    if (dailyMinPrice.getPrice() > 0) {
                        viewHolder.mPriceTv.setText(String.format("¥%s", dailyMinPrice.getPrice()));
                    } else {
                        viewHolder.mPriceTv.setText("实时计价");
                    }
                    if (!TextUtils.equals(minPrice, "实时计价") && !TextUtils.equals(minPrice, "无班期") && TextUtils.equals(minPrice, String.format("¥%s", dailyMinPrice.getPrice()))) {
                        viewHolder.mPriceTv.setTextColor(Color.parseColor("#ff6913"));
                    } else {
                        viewHolder.mPriceTv.setTextColor(Color.parseColor("#666666"));
                    }
                    if (dailyMinPrice.getInventory() > 0 && dailyMinPrice.getInventory() < 10) {
                        viewHolder.mStock.setVisibility(VISIBLE);
                        viewHolder.mStock.setText(String.format("余%s", dailyMinPrice.getInventory()));
                    } else {
                        viewHolder.mStock.setVisibility(INVISIBLE);
                    }
                    //节日
                    if (festival != null && !TextUtils.isEmpty(festival.getFestivalName())) {
                        viewHolder.mFestival.setVisibility(VISIBLE);
                        if (festival.getIsMainFestival()) {
                            viewHolder.mFestival.setText(festival.getFestivalName());
                        } else {
                            viewHolder.mFestival.setText("休");
                        }
                        viewHolder.mDay.setTextColor(Color.parseColor("#ff3513"));
                    } else {
                        viewHolder.mFestival.setVisibility(INVISIBLE);
                        viewHolder.mDay.setTextColor(Color.parseColor("#111111"));
                    }
                    //是否选中
                    view.setSelected(dateBean.selected);
                } else {//无价格
                    viewHolder.mDay.setSelected(false);
                    view.setEnabled(false);
                    viewHolder.mDay.setTextColor(Color.parseColor("#cccccc"));
                    viewHolder.mPriceTv.setVisibility(INVISIBLE);
                    viewHolder.mStock.setVisibility(INVISIBLE);
                    viewHolder.mFestival.setVisibility(INVISIBLE);

                    //节日
                    if (festival != null && !TextUtils.isEmpty(festival.getFestivalName())) {
                        if (festival.getIsMainFestival()) {
                            viewHolder.mFestival.setText(festival.getFestivalName());
                        } else {
                            viewHolder.mFestival.setText("休");
                        }
                        viewHolder.mFestival.setVisibility(VISIBLE);
                    } else {
                        viewHolder.mFestival.setVisibility(INVISIBLE);
                    }
                }
            }
            return view;
        }

        //该日期包含价格
        private DailyMinPrice containPriceDate(int year, int month, int day) {
            if (mPriceCalendarInfo == null || mPriceCalendarInfo.getDailyMinPrices() == null) {
                return null;
            }
            List<DailyMinPrice> dailyMinPrices = mPriceCalendarInfo.getDailyMinPrices();
            for (int i = 0; i < dailyMinPrices.size(); i++) {
                if (TextUtils.equals(dailyMinPrices.get(i).getDate(), String.format("%s-%s-%s", year,
                        StringUtils.leftPad(month + "", 2, "0"), StringUtils.leftPad(String.valueOf(day), 2, "0")))) {
                    return dailyMinPrices.get(i);
                }
            }
            return null;
        }

        private Festival containFestivalDate(int year, int month, int day) {
            if (mPriceCalendarInfo == null || mPriceCalendarInfo.getFestivals() == null) {
                return null;
            }
            List<Festival> festivals = mPriceCalendarInfo.getFestivals();
            for (int i = 0; i < festivals.size(); i++) {
                if (TextUtils.equals(festivals.get(i).getFestivalDate(), String.format("%s-%s-%s", year,
                        StringUtils.leftPad(month + "", 2, "0"), StringUtils.leftPad(String.valueOf(day), 2, "0")))) {
                    return festivals.get(i);
                }
            }
            return null;
        }
    }

    static class DateBean {
        private int currentYear;
        private int currentMonth;
        private int pos;
        private boolean selected;

        public DateBean(int currentYear, int currentMonth, int pos, boolean selected) {
            this.currentYear = currentYear;
            this.currentMonth = currentMonth;
            this.pos = pos;
            this.selected = selected;
        }
    }


    public static class GridViewHolder {
        public TextView mDay;
        public TextView mPriceTv;
        public TextView mFestival;
        public TextView mStock;
    }

    class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @Override
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    public interface DatePickerController {
        void onDayOfMonthSelected(int year, int month, int day, PriceCalendarInfo priceCalendarInfo);

        void onViewCallBack(View view);
    }

}
