package com.snowble.android.widget.verticalstepper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VerticalStepper extends ViewGroup {
    private Context context;
    private Resources resources;
    private Step.Common commonStepValues;

    @VisibleForTesting
    List<Step> steps;

    @VisibleForTesting
    int outerHorizontalPadding;
    @VisibleForTesting
    int outerVerticalPadding;
    @VisibleForTesting
    int iconInactiveColor;
    @VisibleForTesting
    int iconActiveColor;
    @VisibleForTesting
    int continueButtonStyle;

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

        commonStepValues = new Step.Common(context, iconActiveColor, iconInactiveColor);
        steps = new ArrayList<>();
    }

    @VisibleForTesting
    Step.Common getCommonStepValues() {
        return commonStepValues;
    }

    @VisibleForTesting
    void initPropertiesFromAttrs(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalStepper,
                    defStyleAttr, defStyleRes);
        try {
            initIconPropertiesFromAttrs(a);
            initNavButtonPropertiesFromAttrs(a);
        } finally {
            a.recycle();
        }
    }

    private void initIconPropertiesFromAttrs(TypedArray a) {
        int defaultActiveColor =
                ThemeUtils.getResolvedAttributeData(context.getTheme(), R.attr.colorPrimary, R.color.bg_active_icon);
        iconActiveColor = a.getColor(R.styleable.VerticalStepper_iconColorActive,
                ResourcesCompat.getColor(resources, defaultActiveColor, context.getTheme()));
        iconInactiveColor = a.getColor(R.styleable.VerticalStepper_iconColorInactive,
                ResourcesCompat.getColor(resources, R.color.bg_inactive_icon, context.getTheme()));
    }

    @SuppressLint("PrivateResource") // https://code.google.com/p/android/issues/detail?id=230985
    private void initNavButtonPropertiesFromAttrs(TypedArray a) {
        continueButtonStyle = a.getResourceId(
                R.styleable.VerticalStepper_continueButtonStyle, R.style.Widget_AppCompat_Button_Colored);
    }

    private void initPadding() {
        outerHorizontalPadding = resources.getDimensionPixelSize(R.dimen.outer_padding_horizontal);
        outerVerticalPadding = resources.getDimensionPixelSize(R.dimen.outer_padding_vertical);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initSteps();
    }

    @VisibleForTesting
    void initSteps() {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, continueButtonStyle);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            initStep(new Step(getChildAt(i), new InternalTouchView(context),
                    new AppCompatButton(contextWrapper, null, 0), commonStepValues));
        }

        for (Step s : steps) {
            initTouchView(s);
            initNavButtons(s);
        }
    }

    @VisibleForTesting
    void initStep(Step step) {
        steps.add(step);
        step.getInnerView().setVisibility(View.GONE);
    }

    @VisibleForTesting
    void initTouchView(final Step step) {
        InternalTouchView touchView = step.getTouchView();
        touchView.setBackgroundResource(commonStepValues.getTouchViewBackground());
        touchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStepExpandedState(step);
            }
        });
        addView(touchView);
    }

    @VisibleForTesting
    void initNavButtons(Step step) {
        AppCompatButton continueButton = step.getContinueButton();
        continueButton.setVisibility(GONE);
        continueButton.setText(R.string.continue_button);
        // TODO Add Margins
        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO collapse current view and expand next view
            }
        });
        addView(continueButton);
    }

    @VisibleForTesting
    void toggleStepExpandedState(Step step) {
        toggleActiveState(step);
        toggleViewVisibility(step.getInnerView());
        toggleViewVisibility(step.getContinueButton());
    }

    private void toggleActiveState(Step step) {
        step.setActive(!step.isActive());
    }

    private void toggleViewVisibility(View view) {
        int visibility = view.getVisibility();
        if (visibility == VISIBLE) {
            view.setVisibility(GONE);
        } else {
            view.setVisibility(VISIBLE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        doMeasurement(widthMeasureSpec, heightMeasureSpec);
    }

    @VisibleForTesting
    void doMeasurement(int widthMeasureSpec, int heightMeasureSpec) {
        measureStepDecoratorHeights();
        measureStepBottomMarginHeights();
        measureChildViews(widthMeasureSpec, heightMeasureSpec);
        int width = calculateWidth();
        int height = calculateHeight();

        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());

        width = resolveSize(width, widthMeasureSpec);
        height = resolveSize(height, heightMeasureSpec);

        measureTouchViews(width, commonStepValues.getTouchViewHeight());

        setMeasuredDimension(width, height);
    }

    @VisibleForTesting
    void measureStepDecoratorHeights() {
        for (int i = 0, innerViewsSize = steps.size(); i < innerViewsSize; i++) {
            steps.get(i).measureStepDecoratorHeight();
        }
    }

    @VisibleForTesting
    void measureStepBottomMarginHeights() {
        for (int i = 0, innerViewsSize = steps.size(); i < innerViewsSize - 1; i++) {
            steps.get(i).measureBottomMarginToNextStep();
        }
    }

    @VisibleForTesting
    void measureChildViews(int widthMeasureSpec, int heightMeasureSpec) {
        int currentHeight = calculateVerticalPadding();
        for (int i = 0, innerViewsSize = steps.size(); i < innerViewsSize; i++) {
            Step step = steps.get(i);
            int childrenHeight = 0;

            currentHeight += step.getDecoratorHeight();

            View innerView = step.getInnerView();
            measureChild(innerView, widthMeasureSpec, heightMeasureSpec, currentHeight);
            childrenHeight += calculateChildHeight(step, innerView);
            currentHeight += childrenHeight;

            View continueButton = step.getContinueButton();
            measureChild(continueButton, widthMeasureSpec, heightMeasureSpec, currentHeight);
            childrenHeight += calculateChildHeight(step, continueButton);
            currentHeight += step.getBottomMarginHeight();

            step.setChildrenVisibleHeight(childrenHeight);
        }
    }

    private void measureChild(View child, int parentWms, int parentHms, int currentHeight) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int childUsedWidth = calculateHorizontalPadding() + calculateHorizontalUsedSpace(child);
        int childWms = getChildMeasureSpec(parentWms, childUsedWidth, lp.width);

        int childVerticalPadding = calculateVerticalUsedSpace(child);
        int childUsedHeight = childVerticalPadding + currentHeight;
        int childHms = getChildMeasureSpec(parentHms, childUsedHeight, lp.height);

        child.measure(childWms, childHms);
    }

    private int calculateChildHeight(Step step, View child) {
        if (step.isActive()) {
            return child.getMeasuredHeight() + calculateVerticalUsedSpace(child);
        }
        return 0;
    }

    @VisibleForTesting
    int calculateVerticalUsedSpace(View view) {
        VerticalStepper.LayoutParams lp = (VerticalStepper.LayoutParams) view.getLayoutParams();
        return lp.topMargin + lp.bottomMargin;
    }

    @VisibleForTesting
    int calculateWidth() {
        return calculateHorizontalPadding() + calculateMaxStepWidth();
    }

    @VisibleForTesting
    int calculateMaxStepWidth() {
        int width = 0;
        for (int i = 0, innerViewsSize = steps.size(); i < innerViewsSize; i++) {
            Step step = steps.get(i);

            width = Math.max(width, step.calculateStepDecoratorWidth());

            View innerView = step.getInnerView();
            int innerViewHorizontalPadding = calculateHorizontalUsedSpace(innerView);
            width = Math.max(width, innerView.getMeasuredWidth() + innerViewHorizontalPadding);

            AppCompatButton continueButton = step.getContinueButton();
            int continueHorizontalPadding = calculateHorizontalUsedSpace(continueButton);
            width = Math.max(width, continueButton.getMeasuredWidth() + continueHorizontalPadding);
        }
        return width;
    }

    @VisibleForTesting
    int calculateHorizontalUsedSpace(View view) {
        VerticalStepper.LayoutParams lp = (VerticalStepper.LayoutParams) view.getLayoutParams();
        return commonStepValues.calculateStepDecoratorIconWidth() + lp.leftMargin + lp.rightMargin;
    }

    @VisibleForTesting
    int calculateHeight() {
        int height = calculateVerticalPadding();
        for (Step step : steps) {
            height += step.getDecoratorHeight();
            height += step.getChildrenVisibleHeight();
            height += step.getBottomMarginHeight();
        }
        return height;
    }

    @VisibleForTesting
    void measureTouchViews(int width, int height) {
        for (Step v : steps) {
            measureTouchView(width, height, v.getTouchView());
        }
    }

    private void measureTouchView(int width, int height, InternalTouchView view) {
        int wms = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int hms = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        view.measure(wms, hms);
    }

    @VisibleForTesting
    int calculateHorizontalPadding() {
        return outerHorizontalPadding + outerHorizontalPadding + getPaddingLeft() + getPaddingRight();
    }

    @VisibleForTesting
    int calculateVerticalPadding() {
        return outerVerticalPadding + outerVerticalPadding + getPaddingTop() + getPaddingBottom();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Rect rect = commonStepValues.getTempRectForLayout();
        rect.set(getPaddingLeft() + outerHorizontalPadding,
                getPaddingTop() + outerVerticalPadding,
                right - left - getPaddingRight() - outerHorizontalPadding,
                bottom - top - getPaddingBottom() - outerVerticalPadding);
        for (int i = 0, innerViewsSize = steps.size(); i < innerViewsSize; i++) {
            Step step = steps.get(i);

            layoutTouchView(rect, step.getTouchView());

            if (step.isActive()) {
                layoutActiveViews(rect, step);
            }
            rect.top += step.calculateYDistanceToNextStep();
        }
    }

    @VisibleForTesting
    void layoutTouchView(Rect rect, InternalTouchView touchView) {
        // The touch view isn't clipped to the outer padding for so offset it.
        int touchLeft = rect.left - outerHorizontalPadding;

        int touchTop = rect.top - outerVerticalPadding;

        int touchRight = rect.right + outerHorizontalPadding;

        int touchBottomMax = rect.bottom + outerVerticalPadding;
        int touchBottom = Math.min(touchTop + touchView.getMeasuredHeight(), touchBottomMax);

        touchView.layout(touchLeft, touchTop, touchRight, touchBottom);
    }

    private void layoutActiveViews(Rect rect, Step step) {
        int originalLeft = rect.left;
        int originalTop = rect.top;

        rect.left += commonStepValues.calculateStepDecoratorIconWidth();
        rect.top += step.calculateYDistanceToTextBottom();

        layoutInnerView(rect, step);

        rect.top += step.getInnerView().getHeight();
        layoutNavButtons(rect, step);

        rect.left = originalLeft;
        rect.top = originalTop;
    }

    @VisibleForTesting
    void layoutInnerView(Rect rect, Step step) {
        layoutChildView(rect, step.getInnerView());
    }

    @VisibleForTesting
    void layoutNavButtons(Rect rect, Step step) {
        layoutChildView(rect, step.getContinueButton());
    }

    @VisibleForTesting
    void layoutChildView(Rect rect, View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        int childLeft = rect.left + lp.leftMargin;

        int childTop = rect.top + lp.topMargin;

        int childRightMax = rect.right - lp.rightMargin;
        int childRight = Math.min(childLeft + child.getMeasuredWidth(), childRightMax);

        int childBottomMax = rect.bottom - lp.bottomMargin;
        int childBottom = Math.min(childTop + child.getMeasuredHeight(), childBottomMax);

        child.layout(childLeft, childTop, childRight, childBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(outerHorizontalPadding + getPaddingLeft(), outerVerticalPadding + getPaddingTop());
        int dyToNextStep = 0;
        for (int i = 0, innerViewsSize = steps.size(); i < innerViewsSize; i++) {
            canvas.translate(0, dyToNextStep);
            canvas.save();

            int stepNumber = i + 1;
            Step step = steps.get(i);

            canvas.save();
            drawIcon(canvas, step, stepNumber);
            canvas.restore();

            canvas.save();
            drawText(canvas, step);
            canvas.restore();

            boolean hasMoreSteps = stepNumber < innerViewsSize;
            if (hasMoreSteps) {
                dyToNextStep = step.calculateYDistanceToNextStep();

                canvas.save();
                drawConnector(canvas, dyToNextStep);
                canvas.restore();
            }

            canvas.restore();
        }
        canvas.translate(outerHorizontalPadding + getPaddingRight(), outerVerticalPadding + getPaddingBottom());
        canvas.restore();
    }

    private void drawIcon(Canvas canvas, Step step, int stepNumber) {
        drawIconBackground(canvas, step);
        drawIconText(canvas, stepNumber);
    }

    private void drawIconBackground(Canvas canvas, Step step) {
        canvas.drawArc(commonStepValues.getTempRectForIconBackground(), 0f, 360f, true, step.getIconColor());
    }

    private void drawIconText(Canvas canvas, int stepNumber) {
        String stepNumberString = String.format(Locale.getDefault(), "%d", stepNumber);
        TextPaint iconTextPaint = commonStepValues.getIconTextPaint();
        int iconDimension = commonStepValues.getIconDimension();

        float width = iconTextPaint.measureText(stepNumberString);
        float centeredTextX = (iconDimension / 2) - (width / 2);

        Rect tmpRectIconTextBounds = commonStepValues.getTempRectForIconTextBounds();
        iconTextPaint.getTextBounds(stepNumberString, 0, 1, tmpRectIconTextBounds);
        float centeredTextY = (iconDimension / 2) + (tmpRectIconTextBounds.height() / 2);

        canvas.drawText(stepNumberString, centeredTextX, centeredTextY, iconTextPaint);
    }

    private void drawText(Canvas canvas, Step step) {
        canvas.translate(commonStepValues.calculateStepDecoratorIconWidth(), 0);

        TextPaint paint = step.getTitleTextPaint();
        canvas.drawText(step.getTitle(), 0, step.getTitleBaselineRelativeToStepTop(), paint);

        if (!TextUtils.isEmpty(step.getSummary()) && !step.isActive()) {
            canvas.translate(0, step.getTitleBottomRelativeToStepTop());
            canvas.drawText(step.getSummary(), 0,
                    step.getSummaryBaselineRelativeToTitleBottom(), commonStepValues.getSummaryTextPaint());
        }
        // TODO Handle optional case
    }

    private void drawConnector(Canvas canvas, int yDistanceToNextStep) {
        int iconDimension = commonStepValues.getIconDimension();
        int iconMarginVertical = commonStepValues.getIconMarginVertical();
        int connectorWidth = commonStepValues.getConnectorWidth();
        Paint connectorPaint = commonStepValues.getConnectorPaint();

        canvas.translate((iconDimension - connectorWidth) / 2, 0);
        float startY = iconDimension + iconMarginVertical;
        float stopY = yDistanceToNextStep - iconMarginVertical;
        canvas.drawLine(0, startY, 0, stopY, connectorPaint);
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

        private static final String EMPTY_TITLE = " ";
        private String title;
        private String summary;

        LayoutParams(Context c, AttributeSet attrs) {
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
        }

        LayoutParams(int width, int height) {
            super(width, height);
            title = EMPTY_TITLE;
        }

        LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            title = EMPTY_TITLE;
        }

        String getTitle() {
            return title;
        }

        String getSummary() {
            return summary;
        }
    }

    @VisibleForTesting
    static class InternalTouchView extends View {
        public InternalTouchView(Context context) {
            super(context);
        }
    }
}
