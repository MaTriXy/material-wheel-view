package io.blackbox_vision.wheelview.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.blackbox_vision.wheelview.R;

import static io.blackbox_vision.wheelview.utils.DateUtils.*;


public final class DatePickerPopUpWindow extends PopupWindow {
    private static final String TAG = DatePickerPopUpWindow.class.getSimpleName();

    private static final String SHORT_MONTH_FORMAT = "MMM";
    private static final String YEAR_FORMAT = "yyyy";
    private static final String MONTH_FORMAT = "MM";
    private static final String DAY_FORMAT = "dd";

    private static final int DEFAULT_MIN_YEAR = 1900;

    private Context context;

    private View rootView;
    private View container;

    private WheelView yearSpinner;
    private WheelView monthSpinner;
    private WheelView daySpinner;

    private Button cancelButton;
    private Button confirmButton;

    private String cancelButtonText;
    private String confirmButtonText;

    private int cancelButtonTextColor;
    private int confirmButtonTextColor;
    private int buttonTextSize;

    private int viewTextSize;

    private int minYear;
    private int maxYear;

    private boolean showDayMonthYear;
    private boolean showShortMonths;

    @NonNull
    private final List<String> years = new ArrayList<>();

    @NonNull
    private final List<String> months = new ArrayList<>();

    @NonNull
    private final List<String> days = new ArrayList<>();

    private int yearPos = 0;
    private int monthPos = 0;
    private int dayPos = 0;

    @Nullable
    private OnDateSelectedListener onDateSelectedListener;

    private Calendar calendar;
    private Locale locale;

    public DatePickerPopUpWindow(@NonNull final Builder builder) {
        this.locale = builder.locale;
        this.context = builder.context;
        this.minYear = builder.minYear;
        this.maxYear = builder.maxYear;
        this.calendar = builder.selectedDate;
        this.viewTextSize = builder.viewTextSize;
        this.buttonTextSize = builder.buttonTextSize;
        this.onDateSelectedListener = builder.listener;
        this.showShortMonths = builder.showShortMonths;
        this.cancelButtonText = builder.cancelButtonText;
        this.showDayMonthYear = builder.showDayMonthYear;
        this.confirmButtonText = builder.confirmButtonText;
        this.cancelButtonTextColor = builder.cancelButtonTextColor;
        this.confirmButtonTextColor = builder.confirmButtonTextColor;
        this.yearPos = calendar.get(Calendar.YEAR) - minYear;
        this.monthPos = calendar.get(Calendar.MONTH);
        this.dayPos = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        initView();
    }

    private void initView() {
        rootView = LayoutInflater.from(context).inflate(showDayMonthYear ? R.layout.layout_date_picker_inverted : R.layout.layout_date_picker, null);

        container = rootView.findViewById(R.id.container_picker);

        cancelButton = (Button) rootView.findViewById(R.id.btn_cancel);
        confirmButton = (Button) rootView.findViewById(R.id.btn_confirm);

        yearSpinner = (WheelView) rootView.findViewById(R.id.picker_year);
        monthSpinner = (WheelView) rootView.findViewById(R.id.picker_month);
        daySpinner = (WheelView) rootView.findViewById(R.id.picker_day);

        yearSpinner.setCanLoop(false);
        monthSpinner.setCanLoop(false);
        daySpinner.setCanLoop(false);

        yearSpinner.setTextSize(viewTextSize);
        monthSpinner.setTextSize(viewTextSize);
        daySpinner.setTextSize(viewTextSize);

        cancelButton.setTextColor(cancelButtonTextColor);
        cancelButton.setTextSize(buttonTextSize);

        confirmButton.setTextColor(confirmButtonTextColor);
        confirmButton.setTextSize(buttonTextSize);

        yearSpinner.addOnLoopScrollListener((item, position) -> yearPos = position);

        monthSpinner.addOnLoopScrollListener((item, position) -> monthPos = position);
        monthSpinner.addOnLoopScrollListener((item, position) -> drawDayPickerView());

        daySpinner.addOnLoopScrollListener((item, position) -> dayPos = position);

        drawYearPickerView();
        drawMonthPickerView();
        drawDayPickerView();

        cancelButton.setOnClickListener(this::onClick);
        confirmButton.setOnClickListener(this::onClick);

        rootView.setOnClickListener(this::onClick);

        if (!TextUtils.isEmpty(confirmButtonText)) {
            confirmButton.setText(confirmButtonText);
        }

        if (!TextUtils.isEmpty(cancelButtonText)) {
            cancelButton.setText(cancelButtonText);
        }

        setTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setAnimationStyle(R.style.FadeInPopWin);
        setContentView(rootView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void drawYearPickerView() {
        calendar = Calendar.getInstance(locale);
        final int yearCount = maxYear - minYear;

        for (int i = 0; i < yearCount; i++) {
            calendar.set(Calendar.YEAR, minYear + i);

            years.add(i, formatDate(calendar, YEAR_FORMAT));
        }

        yearSpinner.setItems(years);
        yearSpinner.setInitialPosition(yearPos);
    }

    private void drawMonthPickerView() {
        calendar = Calendar.getInstance(locale);

        for (int j = 0; j <= calendar.getActualMaximum(Calendar.MONTH); j++) {
            calendar.set(Calendar.YEAR, Integer.valueOf(years.get(yearPos)));
            calendar.set(Calendar.MONTH, j);
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            months.add(j, formatDate(calendar, showShortMonths ? SHORT_MONTH_FORMAT : MONTH_FORMAT).toUpperCase());
        }

        monthSpinner.setItems(months);
        monthSpinner.setInitialPosition(monthPos);
    }

    private void drawDayPickerView() {
        for (Iterator<String> iterator = days.listIterator(); iterator.hasNext(); ) {
            iterator.next();
            iterator.remove();
        }

        final int year = Integer.valueOf(years.get(yearPos));
        final int month = showShortMonths ? months.indexOf(months.get(monthPos)) + 1 : Integer.valueOf(months.get(monthPos));

        calendar = Calendar.getInstance(locale);

        for (int i = 0; i < calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);

            days.add(i, formatDate(calendar, DAY_FORMAT));
        }

        daySpinner.setItems(days);
        daySpinner.setInitialPosition(dayPos);
    }

    public void show(@Nullable Activity activity) {
        if (null != activity) {
            final int relative = Animation.RELATIVE_TO_SELF;
            final TranslateAnimation animation = new TranslateAnimation(relative, 0, relative, 0, relative, 1, relative, 0);

            showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
            animation.setDuration(400);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());

            container.startAnimation(animation);
        }
    }

    public void dismiss() {
        final int relative = Animation.RELATIVE_TO_SELF;
        final TranslateAnimation animation = new TranslateAnimation(relative, 0, relative, 0, relative, 0, relative, 1);

        animation.setDuration(400);
        animation.setInterpolator(new AccelerateInterpolator());

        container.startAnimation(animation);
        super.dismiss();
    }

    private void onClick(View v) {
        if (v == rootView || v == cancelButton) {
            dismiss();
        } else if (v == confirmButton) {
            if (null != onDateSelectedListener) {
                final int year = Integer.valueOf(years.get(yearPos));
                final int month = showShortMonths ? months.indexOf(months.get(monthPos)) : Integer.valueOf(months.get(monthPos)) - 1;
                final int dayOfMonth = Integer.valueOf(days.get(dayPos));

                onDateSelectedListener.onDateSelected(year, month, dayOfMonth);
            }

            dismiss();
        }
    }

    public interface OnDateSelectedListener {

        void onDateSelected(int year, int month, int dayOfMonth);
    }

    public static class Builder {

        @Nullable
        private OnDateSelectedListener listener;

        private Context context;

        private boolean showDayMonthYear = false;
        private boolean showShortMonths = false;

        private int minYear = DEFAULT_MIN_YEAR;
        private int maxYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        private String cancelButtonText = "Cancel";
        private String confirmButtonText = "Confirm";

        private int cancelButtonTextColor = Color.parseColor("#999999");
        private int confirmButtonTextColor = Color.parseColor("#303F9F");

        private Locale locale = Locale.getDefault();
        private Calendar selectedDate = Calendar.getInstance(locale);

        private int buttonTextSize = 16;
        private int viewTextSize = 25;

        public Builder(@NonNull final Context context) {
            this.context = context;
        }

        public Builder setMinYear(int minYear) {
            this.minYear = minYear;
            return this;
        }

        public Builder setMaxYear(int maxYear) {
            this.maxYear = maxYear;
            return this;
        }

        public Builder setCancelButtonText(@NonNull String cancelButtonText) {
            this.cancelButtonText = cancelButtonText;
            return this;
        }

        public Builder setConfirmButtonText(@NonNull String confirmButtonText) {
            this.confirmButtonText = confirmButtonText;
            return this;
        }

        public Builder setLocale(@NonNull Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder setSelectedDate(@NonNull String selectedDate) {
            final Calendar calendar = Calendar.getInstance(locale);
            calendar.setTimeInMillis(toMilliseconds(selectedDate));

            this.selectedDate = calendar;
            return this;
        }

        public Builder setCancelButtonTextColor(int cancelButtonTextColor) {
            this.cancelButtonTextColor = cancelButtonTextColor;
            return this;
        }

        public Builder setConfirmButtonTextColor(int confirmButtonTextColor) {
            this.confirmButtonTextColor = confirmButtonTextColor;
            return this;
        }

        public Builder setButtonTextSize(int buttonTextSize) {
            this.buttonTextSize = buttonTextSize;
            return this;
        }

        public Builder setViewTextSize(int viewTextSize) {
            this.viewTextSize = viewTextSize;
            return this;
        }

        public Builder setShowDayMonthYear(boolean showDayMonthYear) {
            this.showDayMonthYear = showDayMonthYear;
            return this;
        }

        public Builder setShowShortMonths(boolean showShortMonths) {
            this.showShortMonths = showShortMonths;
            return this;
        }

        public Builder setOnDateSelectedListener(@Nullable OnDateSelectedListener onDateSelectedListener) {
            this.listener = onDateSelectedListener;
            return this;
        }

        public DatePickerPopUpWindow build() {
            if (minYear > maxYear) {
                throw new IllegalArgumentException("minYear can't be major to maxYear");
            }

            return new DatePickerPopUpWindow(this);
        }
    }
}
