package com.snowble.android.verticalstepper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VerticalStepper extends ViewGroup {

    private Context context;
    private Resources resources;

    private List<View> innerViews;

    private int outerHorizontalPadding;
    private int outerVerticalPadding;

    private int innerInactiveVerticalMargin;
    private int innerActiveVerticalMargin;

    private int iconDimension;
    private int iconMarginRight;
    private int iconMarginVertical;
    private int iconActiveColor;
    private int iconInactiveColor;
    private Paint iconActiveBackgroundPaint;
    private Paint iconInactiveBackgroundPaint;
    private RectF tmpRectIconBackground;
    private TextPaint iconTextPaint;
    private Rect tmpRectIconTextBounds;

    private TextPaint titleActiveTextPaint;
    private TextPaint titleInactiveTextPaint;
    private TextPaint summaryTextPaint;
    private int titleMarginBottom;

    private int touchViewHeight;
    private int touchViewBackground;

    private int connectorWidth;
    private Paint connectorPaint;

    public VerticalStepper(Context context) {
        super(context);
        init();
    }

    public VerticalStepper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public VerticalStepper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VerticalStepper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        init(null);
    }

    private void init(@Nullable AttributeSet attrs) {
        init(attrs, 0);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        init(attrs, defStyleAttr, 0);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setWillNotDraw(false);

        context = getContext();
        resources = getResources();

        initPropertiesFromAttrs(attrs, defStyleAttr, defStyleRes);
        initPadding();
        initIconProperties();
        initTitleProperties();
        initSummaryProperties();
        initTouchViewProperties();
        initConnectorProperties();
    }

    private void initPropertiesFromAttrs(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VerticalStepper,
                    defStyleAttr, defStyleRes);
            try {
                initIconPropertiesFromAttrs(a);
            } finally {
                a.recycle();
            }
        }
    }

    private void initIconPropertiesFromAttrs(TypedArray a) {
        int defaultActiveColor = getResolvedAttributeData(R.attr.colorPrimary, R.color.bg_active_icon);
        iconActiveColor = a.getColor(R.styleable.VerticalStepper_iconColorActive,
                ResourcesCompat.getColor(resources, defaultActiveColor, context.getTheme()));
        iconInactiveColor = a.getColor(R.styleable.VerticalStepper_iconColorInactive,
                ResourcesCompat.getColor(resources, R.color.bg_inactive_icon, context.getTheme()));
    }

    private void initPadding() {
        outerHorizontalPadding = resources.getDimensionPixelSize(R.dimen.outer_padding_horizontal);
        outerVerticalPadding = resources.getDimensionPixelSize(R.dimen.outer_padding_vertical);
        innerInactiveVerticalMargin = resources.getDimensionPixelSize(R.dimen.inner_inactive_margin_vertical);
        innerActiveVerticalMargin = resources.getDimensionPixelSize(R.dimen.inner_active_margin_vertical);
    }

    private void initIconProperties() {
        initIconDimension();
        initIconMargins();
        initIconBackground();
        initIconTextPaint();
        initIconTmpObjects();
    }

    private void initIconDimension() {
        iconDimension = resources.getDimensionPixelSize(R.dimen.icon_diameter);
    }

    private void initIconMargins() {
        iconMarginRight = resources.getDimensionPixelSize(R.dimen.icon_margin_right);
        iconMarginVertical = resources.getDimensionPixelSize(R.dimen.icon_margin_vertical);
    }

    private void initIconBackground() {
        iconActiveBackgroundPaint = createIconBackground(iconActiveColor);
        iconInactiveBackgroundPaint = createIconBackground(iconInactiveColor);
    }

    private Paint createIconBackground(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        return paint;
    }

    private void initIconTextPaint() {
        iconTextPaint = createTextPaint(R.color.white, R.dimen.icon_font_size);
    }

    private void initIconTmpObjects() {
        tmpRectIconBackground = new RectF(0, 0, iconDimension, iconDimension);
        tmpRectIconTextBounds = new Rect();
    }

    private void initTitleProperties() {
        initTitleDimensions();
        initTitleTextPaint();
    }

    private void initTitleDimensions() {
        titleMarginBottom = resources.getDimensionPixelSize(R.dimen.title_margin_bottom);
    }

    private void initTitleTextPaint() {
        titleActiveTextPaint = createTextPaint(R.color.title_active_color, R.dimen.title_font_size);
        titleActiveTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        titleInactiveTextPaint = createTextPaint(R.color.title_inactive_color, R.dimen.title_font_size);
    }

    private void initSummaryProperties() {
        initSummaryTextPaint();
    }

    private void initSummaryTextPaint() {
        summaryTextPaint = createTextPaint(R.color.summary_color, R.dimen.summary_font_size);
    }

    private TextPaint createTextPaint(int colorRes, int fontDimenRes) {
        TextPaint textPaint = new TextPaint();
        setPaintColor(textPaint, colorRes);
        textPaint.setAntiAlias(true);
        int titleTextSize = resources.getDimensionPixelSize(fontDimenRes);
        textPaint.setTextSize(titleTextSize);
        return textPaint;
    }

    private void initTouchViewProperties() {
        touchViewHeight = resources.getDimensionPixelSize(R.dimen.touch_height);
        touchViewBackground = getResolvedAttributeData(R.attr.selectableItemBackground, 0);
    }

    private int getResolvedAttributeData(int attr, int defaultData) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, false);
        int resolvedAttributeData;
        if (value.type != TypedValue.TYPE_NULL) {
            resolvedAttributeData = value.data;
        } else {
            resolvedAttributeData = defaultData;
        }
        return resolvedAttributeData;
    }

    private void initConnectorProperties() {
        initConnectorDimension();
        initConnectorPaint();
    }

    private void initConnectorDimension() {
        connectorWidth = resources.getDimensionPixelSize(R.dimen.connector_width);
    }

    private void initConnectorPaint() {
        connectorPaint = new Paint();
        setPaintColor(connectorPaint, R.color.connector_color);
        connectorPaint.setAntiAlias(true);
        connectorPaint.setStrokeWidth(connectorWidth);
    }

    private void setPaintColor(Paint paint, int colorRes) {
        int color = ResourcesCompat.getColor(resources, colorRes, context.getTheme());
        paint.setColor(color);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        int childCount = getChildCount();
        innerViews = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            initInnerView(getChildAt(i));
        }

        for (View v : innerViews) {
            initTouchView(v);
        }
    }

    private void initInnerView(final View innerView) {
        innerView.setVisibility(View.GONE);
        innerViews.add(innerView);

        createAndAttachTouchView(innerView);
    }

    private void createAndAttachTouchView(View innerView) {
        getInternalLayoutParams(innerView).touchView = new InternalTouchView(context);
    }

    private void initTouchView(final View innerView) {
        InternalTouchView touchView = getTouchView(innerView);
        touchView.setBackgroundResource(touchViewBackground);
        // TODO See if the anonymous inner class can be avoided
        touchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStepActiveState(innerView);
            }
        });
        addView(touchView);
    }

    private void toggleStepActiveState(View innerView) {
        LayoutParams lp = getInternalLayoutParams(innerView);
        lp.active = !lp.active;

        int visibility = innerView.getVisibility();
        if (visibility == VISIBLE) {
            innerView.setVisibility(GONE);
        } else {
            innerView.setVisibility(VISIBLE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int horizontalPadding = outerHorizontalPadding + outerHorizontalPadding + getPaddingLeft() + getPaddingRight();
        int verticalPadding = outerVerticalPadding + outerVerticalPadding + getPaddingTop() + getPaddingBottom();

        int width = horizontalPadding;
        int height = verticalPadding;

        int widthWithoutPadding = 0;
        for (int i = 0, innerViewsSize = innerViews.size(); i < innerViewsSize; i++) {
            View v = innerViews.get(i);
            LayoutParams lp = getInternalLayoutParams(v);

            int innerViewHorizontalPadding = iconDimension + iconMarginRight + lp.leftMargin + lp.rightMargin;
            int innerViewVerticalPadding = lp.topMargin + lp.bottomMargin;

            int stepDecoratorWidth = measureStepDecoratorWidth(lp);
            widthWithoutPadding = Math.max(widthWithoutPadding, stepDecoratorWidth);

            int innerWms =
                    getChildMeasureSpec(widthMeasureSpec, horizontalPadding + innerViewHorizontalPadding, lp.width);

            int stepDecoratorHeight = measureStepDecoratorHeight(lp);
            height += stepDecoratorHeight;

            int usedHeight = innerViewVerticalPadding + height;
            int innerHms = getChildMeasureSpec(heightMeasureSpec, usedHeight, lp.height);

            boolean hasMoreSteps = i + 1 < innerViewsSize;
            if (hasMoreSteps) {
                height += getInnerVerticalMargin(lp);
            }

            v.measure(innerWms, innerHms);
            widthWithoutPadding = Math.max(widthWithoutPadding, v.getMeasuredWidth() + innerViewHorizontalPadding);
            if (lp.active) {
                height += v.getMeasuredHeight() + innerViewVerticalPadding;
            }
        }
        width += widthWithoutPadding;

        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());

        width = resolveSize(width, widthMeasureSpec);
        height = resolveSize(height, heightMeasureSpec);

        for (View v : innerViews) {
            measureTouchView(width, getTouchView(v));
        }

        setMeasuredDimension(width, height);
    }

    private int measureStepDecoratorWidth(LayoutParams lp) {
        int stepDecoratorWidth = iconDimension;
        stepDecoratorWidth += iconMarginRight;

        lp.measureTitleHorizontalDimensions(getTitleTextPaint(lp));
        lp.measureSummaryHorizontalDimensions(summaryTextPaint);
        stepDecoratorWidth += Math.max(lp.titleWidth, lp.summaryWidth);

        return stepDecoratorWidth;
    }

    private int measureStepDecoratorHeight(LayoutParams lp) {
        lp.measureTitleVerticalDimensions(getTitleTextPaint(lp), iconDimension);
        lp.measureSummaryVerticalDimensions(summaryTextPaint);
        int textTotalHeight = (int) (lp.titleBottomRelativeToStepTop + lp.summaryBottomRelativeToTitleBottom);
        return Math.max(iconDimension, textTotalHeight);
    }

    private void measureTouchView(int width, InternalTouchView view) {
        int wms = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int hms = MeasureSpec.makeMeasureSpec(touchViewHeight, MeasureSpec.EXACTLY);
        view.measure(wms, hms);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int currentTop = top;
        for (int i = 0, innerViewsSize = innerViews.size(); i < innerViewsSize; i++) {
            View v = innerViews.get(i);
            boolean isFirstStep = i == 0;
            boolean isLastStep = i == innerViewsSize - 1;

            if (isFirstStep) {
                currentTop += getPaddingTop() + outerVerticalPadding;
            }
            layoutTouchView(left, currentTop, right, bottom, v, isFirstStep, isLastStep);

            LayoutParams lp = getInternalLayoutParams(v);
            layoutInnerView(left, currentTop, right, bottom, v, lp, isLastStep);
            currentTop += getYDistanceToNextStep(v, lp);
        }
    }

    private void layoutTouchView(int left, int topAdjustedForPadding, int right, int bottom,
                                 View innerView, boolean isFirstStep, boolean isLastStep) {
        InternalTouchView touchView = getTouchView(innerView);

        int touchLeft = left + getPaddingLeft();

        int touchTop = topAdjustedForPadding;
        if (isFirstStep) {
            // touch view is not clipped by the outer padding
            touchTop -= outerVerticalPadding;
        }

        int touchRight = right - left - getPaddingRight();

        int touchBottomMax;
        if (isLastStep) {
            touchBottomMax = bottom - getPaddingBottom();
        } else {
            touchBottomMax = bottom;
        }
        int touchBottom = Math.min(touchTop + touchView.getMeasuredHeight(), touchBottomMax);

        touchView.layout(touchLeft, touchTop, touchRight, touchBottom);
    }

    private void layoutInnerView(int left, int topAdjustedForPadding, int right, int bottom,
                                 View innerView, LayoutParams lp, boolean isLastStep) {
        if (lp.active) {
            int innerLeft = left + outerHorizontalPadding + getPaddingLeft() + lp.leftMargin
                    + iconDimension + iconMarginRight;

            int innerTop =
                    (int) (topAdjustedForPadding + lp.topMargin + lp.titleBottomRelativeToStepTop + titleMarginBottom);

            int innerRightMax = right - outerHorizontalPadding - getPaddingRight() - lp.rightMargin;
            int innerRight = Math.min(innerLeft + innerView.getMeasuredWidth(), innerRightMax);

            int innerBottomMax;
            if (isLastStep) {
                innerBottomMax = bottom - outerVerticalPadding - getPaddingBottom() - lp.bottomMargin;
            } else {
                innerBottomMax = bottom;
            }
            int innerBottom = Math.min(innerTop + innerView.getMeasuredHeight(), innerBottomMax);

            innerView.layout(innerLeft, innerTop, innerRight, innerBottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(outerHorizontalPadding + getPaddingLeft(), outerVerticalPadding + getPaddingTop());
        for (int i = 0, innerViewsSize = innerViews.size(); i < innerViewsSize; i++) {
            canvas.save();

            int stepNumber = i + 1;
            View innerView = innerViews.get(i);
            LayoutParams lp = getInternalLayoutParams(innerView);

            canvas.save();
            drawIcon(canvas, lp, stepNumber);
            canvas.restore();

            canvas.save();
            drawText(canvas, lp);
            canvas.restore();

            int dyToNextStep = getYDistanceToNextStep(innerView, lp);
            boolean hasMoreSteps = stepNumber < innerViewsSize;
            if (hasMoreSteps) {
                canvas.save();
                drawConnector(canvas, lp, dyToNextStep);
                canvas.restore();
            }

            canvas.restore();
            if (hasMoreSteps) {
                canvas.translate(0, dyToNextStep);
            }
        }
        canvas.restore();
    }

    private int getYDistanceToNextStep(View innerView, LayoutParams lp) {
        int dyToNextStep = (int) lp.titleBaselineRelativeToStepTop;
        if (lp.active) {
            dyToNextStep += titleMarginBottom + innerView.getHeight();
        } else {
            dyToNextStep += lp.summaryBottomRelativeToTitleBottom;
        }
        dyToNextStep += getInnerVerticalMargin(lp);
        return dyToNextStep;
    }

    private void drawIcon(Canvas canvas, LayoutParams lp, int stepNumber) {
        drawIconBackground(canvas, lp);
        drawIconText(canvas, stepNumber);
    }

    private void drawIconBackground(Canvas canvas, LayoutParams lp) {
        canvas.drawArc(tmpRectIconBackground, 0f, 360f, true, getIconColor(lp));
    }

    private void drawIconText(Canvas canvas, int stepNumber) {
        String stepNumberString = String.format(Locale.getDefault(), "%d", stepNumber);

        float width = iconTextPaint.measureText(stepNumberString);
        float centeredTextX = (iconDimension / 2) - (width / 2);

        iconTextPaint.getTextBounds(stepNumberString, 0, 1, tmpRectIconTextBounds);
        float centeredTextY = (iconDimension / 2) + (tmpRectIconTextBounds.height() / 2);

        canvas.drawText(stepNumberString, centeredTextX, centeredTextY, iconTextPaint);
    }

    private void drawText(Canvas canvas, LayoutParams lp) {
        canvas.translate(iconDimension + iconMarginRight, 0);

        TextPaint paint = getTitleTextPaint(lp);
        canvas.drawText(lp.title, 0, lp.titleBaselineRelativeToStepTop, paint);

        if (!TextUtils.isEmpty(lp.summary) && !lp.active) {
            canvas.translate(0, lp.titleBottomRelativeToStepTop);
            canvas.drawText(lp.summary, 0, lp.summaryBaselineRelativeToTitleBottom, summaryTextPaint);
        }
        // TODO Handle optional case
    }

    private void drawConnector(Canvas canvas, LayoutParams lp, int yDistanceToNextStep) {
        canvas.translate((iconDimension - connectorWidth) / 2, 0);
        float startY = iconDimension + iconMarginVertical;
        float stopY = yDistanceToNextStep - iconMarginVertical;
        canvas.drawLine(0, startY, 0, stopY, connectorPaint);
    }

    private TextPaint getTitleTextPaint(LayoutParams lp) {
        return lp.active ? titleActiveTextPaint : titleInactiveTextPaint;
    }

    private int getInnerVerticalMargin(LayoutParams lp) {
        return lp.active ? innerActiveVerticalMargin : innerInactiveVerticalMargin;
    }

    private Paint getIconColor(LayoutParams lp) {
        return lp.active ? iconActiveBackgroundPaint : iconInactiveBackgroundPaint;
    }
    private static InternalTouchView getTouchView(View innerView) {
        return getInternalLayoutParams(innerView).touchView;
    }

    private static LayoutParams getInternalLayoutParams(View innerView) {
        return (LayoutParams) innerView.getLayoutParams();
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(context, attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {

        private static Rect tmpRectTitleTextBounds = new Rect();

        InternalTouchView touchView;

        @SuppressWarnings("NullableProblems")
        @NonNull
        String title;
        float titleWidth;
        float titleBaselineRelativeToStepTop;
        float titleBottomRelativeToStepTop;

        @Nullable
        String summary;
        float summaryWidth;
        float summaryBaselineRelativeToTitleBottom;
        float summaryBottomRelativeToTitleBottom;

        boolean active;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.VerticalStepper_Layout);
            try {
                //noinspection ConstantConditions
                title = a.getString(R.styleable.VerticalStepper_Layout_step_title);
                summary = a.getString(R.styleable.VerticalStepper_Layout_step_summary);
            } finally {
                a.recycle();
            }
            if (TextUtils.isEmpty(title)) {
                throw new IllegalArgumentException("step_title cannot be empty.");
            }
            active = false;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        void measureTitleHorizontalDimensions(TextPaint titlePaint) {
            float width = 0f;
            if (!TextUtils.isEmpty(title)) {
                width = titlePaint.measureText(title);
            }
            titleWidth = width;
        }

        void measureSummaryHorizontalDimensions(TextPaint summaryPaint) {
            float width = 0f;
            if (!TextUtils.isEmpty(summary)) {
                width = summaryPaint.measureText(summary);
            }
            summaryWidth = width;
        }

        void measureTitleVerticalDimensions(TextPaint titlePaint, int heightToCenterIn) {
            measureTitleBaseline(titlePaint, heightToCenterIn);
            titleBottomRelativeToStepTop = titleBaselineRelativeToStepTop + titlePaint.getFontMetrics().bottom;
        }

        private void measureTitleBaseline(TextPaint titlePaint, int heightToCenterIn) {
            titlePaint.getTextBounds(title, 0, 1, tmpRectTitleTextBounds);
            titleBaselineRelativeToStepTop = (heightToCenterIn / 2) + (tmpRectTitleTextBounds.height() / 2);
        }

        void measureSummaryVerticalDimensions(TextPaint summaryPaint) {
            measureSummaryBaseline(summaryPaint);
            summaryBottomRelativeToTitleBottom =
                    summaryBaselineRelativeToTitleBottom + summaryPaint.getFontMetrics().bottom;
        }

        private void measureSummaryBaseline(TextPaint summaryPaint) {
            summaryBaselineRelativeToTitleBottom = -summaryPaint.getFontMetrics().ascent;
        }
    }

    private static class InternalTouchView extends View {
        public InternalTouchView(Context context) {
            super(context);
        }
    }
}
