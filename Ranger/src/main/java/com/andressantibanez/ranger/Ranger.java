package com.andressantibanez.ranger;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class Ranger extends HorizontalScrollView implements View.OnClickListener {

    public final static String TAG = Ranger.class.getSimpleName();

    /**
     * Constants
     */
    //Layouts
    private final int WIDGET_LAYOUT_RES_ID = R.layout.ranger_layout;
    private final int DAY_VIEW_LAYOUT_RES_ID = R.layout.day_layout;
    //Resource ids
    public static final int DAYS_CONTAINER_RES_ID = R.id.days_container;
    public static final int DAY_OF_WEEK_RES_ID = R.id.day_of_week;
    public static final int DAY_NUMBER_RES_ID = R.id.day_number;
    public static final int MONTH_NAME_RES_ID = R.id.month_short_name;
    //Delay
    public static final int DELAY_SELECTION = 300;
    public static final int NO_DELAY_SELECTION = 0;


    /**
     * Variables
     */
    //State
    Context mContext;
    DateTime mStartDate;
    DateTime mEndDate;
    int mSelectedDay;
    List<DateTime> mDisabledDates = new ArrayList<>();

    //Colors
    int mDayTextColor;
    int mDayUnavailableTextColor;
    int mSelectedDayTextColor;
    int mDaysContainerBackgroundColor;
    int mSelectedDayBackgroundColor;

    //Titles
    boolean mAlwaysDisplayMonth;
    boolean mDisplayDayOfWeek;

    //Listener
    DayViewOnClickListener mListener;
    public void setDayViewOnClickListener(DayViewOnClickListener listener) {
        mListener = listener;
    }

    public DateTime getSelectedDate() {
        //Cycle from start day
        DateTime startDate = mStartDate;
        DateTime endDate = mEndDate;

        DateTime selectedDate = null;
        Boolean found = false;
        while (startDate.isBefore(endDate.plusDays(1)) && !found) {
            if (startDate.getDayOfMonth() == mSelectedDay) {
                selectedDate = startDate;
                found = true;
            }

            //Next day
            startDate = startDate.plusDays(1);
        }
        return selectedDate;
    }

    public interface DayViewOnClickListener {
        public void onDaySelected(DateTime date);
    }

    //Day View
    DayView mSelectedDayView;


    /**
     * Controls
     */
    Space mLeftSpace;
    LinearLayout mDaysContainer;
    Space mRightSpace;


    /**
     * Constructors
     */
    public Ranger(Context context) {
        super(context);
        init(context, null);
    }

    public Ranger(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Ranger(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initialization
     */
    public void init(Context context, AttributeSet attributeSet) {
        mContext = context;

        //Init JodaTime
        JodaTimeAndroid.init(context);

        //Inflate view
        View view = LayoutInflater.from(mContext).inflate(WIDGET_LAYOUT_RES_ID, this, true);

        //Get controls
        mDaysContainer = (LinearLayout) view.findViewById(DAYS_CONTAINER_RES_ID);

        //Get custom attributes
        mDisplayDayOfWeek = true;
        if(attributeSet != null) {
            TypedArray a = mContext.getTheme().obtainStyledAttributes(attributeSet, R.styleable.Ranger, 0, 0);

            try {

                //Colors
                mDayTextColor = a.getColor(R.styleable.Ranger_dayTextColor, getColor(R.color.default_day_text_color));
                mDayUnavailableTextColor = a.getColor(R.styleable.Ranger_dayUnavailableTextColor, getColor(R.color.default_unavailable_day_text_color));
                mSelectedDayTextColor = a.getColor(R.styleable.Ranger_selectedDayTextColor, getColor(R.color.default_selected_day_text_color));

                mDaysContainerBackgroundColor = a.getColor(R.styleable.Ranger_daysContainerBackgroundColor, getColor(R.color.default_days_container_background_color));
                mSelectedDayBackgroundColor = a.getColor(R.styleable.Ranger_selectedDayBackgroundColor, getColor(R.color.default_selected_day_background_color));

                //Labels
                mAlwaysDisplayMonth = a.getBoolean(R.styleable.Ranger_alwaysDisplayMonth, false);
                mDisplayDayOfWeek = a.getBoolean(R.styleable.Ranger_displayDayOfWeek, true);

            } finally {
                a.recycle();
            }
        }

        //Setup styling
        //Days Container
        mDaysContainer.setBackgroundColor(mDaysContainerBackgroundColor);
    }

    /***
     * State modification
     */
    public void setStartAndEndDateWithParts(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        setStartDateWithParts(startYear, startMonth, startDay);
        setEndDateWithParts(endYear, endMonth, endDay);

        render();
        //Set Selection. Default is today.
        setSelectedDay(startDay, false);
    }

    public void setStartAndEndDateWithDisabledDates(DateTime startDate, DateTime endDate, List<DateTime> disabledDates) {
        mStartDate = startDate;
        mEndDate = endDate;
        mDisabledDates = new ArrayList<>(disabledDates);

        //Add left padding
        mLeftSpace = new Space(mContext);
        mDaysContainer.addView(mLeftSpace);

        //Cycle from start day
        DateTime date = mStartDate;
        int startDay = 0;

        while (startDay == 0 && date.isBefore(mEndDate.plusDays(1))) {

            if (!disabledDatesContains(date)) {
                startDay = date.getDayOfMonth();
            }
            date = date.plusDays(1);
        }

        render();

        setSelectedDay(startDay, false);
    }

    private boolean disabledDatesContains(DateTime date) {
        if (mDisabledDates == null) {
            return false;
        }

        boolean found = false;
        int pos = 0;
        while (!found && pos < mDisabledDates.size()) {
            if (mDisabledDates.get(pos).getMillis() == date.getMillis()) {
                found = true;
            }
            pos++;
        }
        return found;
    }

    public void setDisabledDates(List<DateTime> disabledDates) {
        mDisabledDates = new ArrayList<>(disabledDates);

        //Add left padding
        mLeftSpace = new Space(mContext);
        mDaysContainer.addView(mLeftSpace);

        DateTime date = mStartDate;
        int startDay = 0;

        while (startDay == 0 && date.isBefore(mEndDate.plusDays(1))) {

            if (!disabledDatesContains(date)) {
                startDay = date.getDayOfMonth();
            }

            date = date.plusDays(1);
        }

        date = mStartDate;
        boolean isSelectedDayDisabled = false;
        while (!isSelectedDayDisabled && date.isBefore(mEndDate.plusDays(1))) {

            if (disabledDatesContains(date) && date.getDayOfMonth() == mSelectedDay) {
                isSelectedDayDisabled = true;
            }

            date = date.plusDays(1);
        }

        render();

        int selectedDay = !isSelectedDayDisabled && mSelectedDay > 0 ? mSelectedDay : startDay;
        setSelectedDay(selectedDay, false);
    }

    private void setStartDateWithParts(int year, int month, int day) {
        mStartDate = new DateTime(year, month, day, 0, 0, 0);
    }

    private void setEndDateWithParts(int year, int month, int day) {
        mEndDate = new DateTime(year, month, day, 23, 59, 59);
    }

    public void setSelectedDay(final int day, final boolean notifyListeners) {
        //Deselect day selected
        if(mSelectedDay > 0)
            unSelectDay(mSelectedDay);

        //Set selected day
        mSelectedDay = day;
        selectDay(mSelectedDay);

        //Call listener
        if(notifyListeners && mListener != null)
            mListener.onDaySelected(getSelectedDate());
    }

    public int getSelectedDay() {
        return mSelectedDay;
    }


    /**
     * Ui
     */
    private int getColor(int colorResId) {
        return getResources().getColor(colorResId);
    }

    private void render() {
        mDaysContainer.removeAllViews();

        //Get inflater for view
        LayoutInflater inflater = LayoutInflater.from(mContext);

        //Add left padding
        mLeftSpace = new Space(mContext);
        mDaysContainer.addView(mLeftSpace);

        //Cycle from start day
        DateTime startDate = mStartDate;
        DateTime endDate = mEndDate;

        boolean isDayDisabled;
        while (startDate.isBefore(endDate)) {

            //Inflate view
            LinearLayout view = (LinearLayout) inflater.inflate(DAY_VIEW_LAYOUT_RES_ID, mDaysContainer, false);

            //new DayView
            DayView dayView = new DayView(view);

            //Set texts and listener
            dayView.setDayOfWeek(startDate.dayOfWeek().getAsShortText().substring(0, 3));
            if(!mDisplayDayOfWeek)
                dayView.hideDayOfWeek();

            dayView.setDay(startDate.getDayOfMonth());
            dayView.setMonthShortName(startDate.monthOfYear().getAsShortText().substring(0, 3));

            isDayDisabled = disabledDatesContains(startDate);

            //Hide month if range in same month
            if (!mAlwaysDisplayMonth && startDate.getMonthOfYear() == endDate.getMonthOfYear()) {
                dayView.hideMonthShortName();
            }

            //Set style
            if (!isDayDisabled) {
                dayView.setTextColor(mDayTextColor);
            } else {
                dayView.setTextColor(mDayUnavailableTextColor);
            }

            //Set listener
            if (!isDayDisabled) {
                dayView.setOnClickListener(this);
            }

            //Add to container
            mDaysContainer.addView(dayView.getView());

            //Next day
            startDate = startDate.plusDays(1);
        }

        //Add right padding
        mRightSpace = new Space(mContext);
        mDaysContainer.addView(mRightSpace);
    }

    private void unSelectDay(int day) {
        DateTime currentDate = getCurrentDateFromDay(day);
        boolean isDayDisabled = disabledDatesContains(currentDate);
        for (int i = 1; i < mDaysContainer.getChildCount() - 1; i++) {
            DayView dayView = new DayView(mDaysContainer.getChildAt(i));
            if(dayView.getDay() == day) {
                if (!isDayDisabled) {
                    dayView.setTextColor(mDayTextColor);
                }
                else {
                    dayView.setTextColor(mDayUnavailableTextColor);
                }
                dayView.setBackgroundColor(0);
                return;
            }
        }
    }

    private DateTime getCurrentDateFromDay(int day) {
        boolean found = false;
        DateTime date = mStartDate;
        while (!found && date.isBefore(mEndDate)) {
            if (date.getDayOfMonth() == day) {
                found = true;
            }
            else {
                date = date.plusDays(1);
            }
        }
        return date;
    }

    private void selectDay(int day) {
        for (int i = 1; i < mDaysContainer.getChildCount() - 1; i++) {
            DayView dayView = new DayView(mDaysContainer.getChildAt(i));
            if(dayView.getDay() == day) {

                dayView.setTextColor(mSelectedDayTextColor);
                dayView.setBackgroundColor(mSelectedDayBackgroundColor);

                mSelectedDayView = dayView;

                return;
            }
        }
    }

    /**
     * On DayView click listener
     */
    @Override
    public void onClick(View view) {
        //Get day view
        DayView dayView = new DayView(view);

        //Get selected day and set selection
        int selectedDay = dayView.getDay();
        setSelectedDay(selectedDay, true);
    }

    /**
     * Configuration change handling
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);
        savedState.setSelectedDay(mSelectedDay);
        savedState.setStartDateString(mStartDate != null ? mStartDate.toString() : null);
        savedState.setEndDateString(mEndDate != null ? mEndDate.toString() : null);
        savedState.setDisabledDates(TextUtils.join(",", mDisabledDates));

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mSelectedDay = savedState.getSelectedDay();
        mStartDate = DateTime.parse(savedState.getStartDateString());
        mEndDate = DateTime.parse(savedState.getEndDateDateString());
        String disabledDatesString = savedState.getDisabledDates();
        mDisabledDates.clear();

        if (disabledDatesString != null && !disabledDatesString.isEmpty()) {
            for(String dateString : disabledDatesString.split(",")) {
                mDisabledDates.add(DateTime.parse(dateString));
            }
        }

        render();

        setSelectedDay(mSelectedDay, false);
    }

    protected static class SavedState extends BaseSavedState {
        int mSelectedDay;
        String mStartDateString;
        String mEndDateString;
        String mDisabledDates;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel in) {
            super(in);
            mSelectedDay = in.readInt();
            mStartDateString = in.readString();
            mEndDateString = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mSelectedDay);
            out.writeString(mStartDateString);
            out.writeString(mEndDateString);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        public void setSelectedDay(int selectedDay) {
            mSelectedDay = selectedDay;
        }

        public void setStartDateString(String startDateString) {
            mStartDateString = startDateString;
        }

        public void setEndDateString(String endDateString) {
            mEndDateString = endDateString;
        }

        public int getSelectedDay() {
            return mSelectedDay;
        }

        public String getStartDateString() {
            return mStartDateString;
        }

        public String getEndDateDateString() {
            return mEndDateString;
        }

        public String getDisabledDates() {
            return mDisabledDates;
        }

        public void setDisabledDates(String mDisabledDates) {
            this.mDisabledDates = mDisabledDates;
        }
    }


    /**
     * DateView class
     */
    public static class DayView {

        int mDay;

        LinearLayout mView;
        TextView mDayOfWeek;
        TextView mDayNumber;
        TextView mMonthShortName;

        public DayView(View view) {
            mView = (LinearLayout) view;

            mDayOfWeek = (TextView) mView.findViewById(DAY_OF_WEEK_RES_ID);
            mDayNumber = (TextView) mView.findViewById(DAY_NUMBER_RES_ID);
            mMonthShortName = (TextView) mView.findViewById(MONTH_NAME_RES_ID);
        }

        public int getDay() {
            return Integer.parseInt(mDayNumber.getText().toString());
        }

        public void setDay(int day) {
            mDay = day;
            setDayNumber(String.format("%02d", day));
        }

        public void setDayOfWeek(String dayOfWeek) {
            mDayOfWeek.setText(dayOfWeek);
        }

        public void setDayNumber(String dayNumber) {
            mDayNumber.setText(dayNumber);
        }

        public void setMonthShortName(String monthShortName) {
            mMonthShortName.setText(monthShortName);
        }

        public void setBackgroundColor(int color) {
            mView.setBackgroundColor(color);
        }

        public void setTextColor(int color) {
            mDayOfWeek.setTextColor(color);
            mDayNumber.setTextColor(color);
            mMonthShortName.setTextColor(color);
        }

        public void setOnClickListener(OnClickListener listener) {
            mView.setOnClickListener(listener);
        }

        public View getView() {
            return mView;
        }

        public void hideDayOfWeek() {
            mDayOfWeek.setVisibility(View.GONE);
        }

        public void hideMonthShortName() {
            mMonthShortName.setVisibility(View.GONE);
        }
    }
}
