package com.snowble.android.widget.verticalstepper;

import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.text.TextPaint;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Java6Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class StepTest {
    private static final Paint ICON_ACTIVE_PAINT = new Paint();
    private static final Paint ICON_INACTIVE_PAINT = new Paint();
    private static final Paint ICON_COMPLETE_PAINT = new Paint();

    public static abstract class GivenChildViews extends GivenAnActivity {
        View innerView;
        VerticalStepper.LayoutParams innerLayoutParams;
        VerticalStepper.InternalTouchView touchView;
        AppCompatButton continueButton;
        VerticalStepper.LayoutParams continueLayoutParams;

        @Before
        public void givenChildViews() {
            innerView = mock(View.class);
            innerLayoutParams = createTestLayoutParams();
            when(innerView.getLayoutParams()).thenReturn(innerLayoutParams);

            touchView = mock(VerticalStepper.InternalTouchView.class);

            continueButton = mock(AppCompatButton.class);
            continueLayoutParams = createTestLayoutParams();
            when(continueButton.getLayoutParams()).thenReturn(continueLayoutParams);
        }

        Step createStep(Step.Common common) {
            return new Step(innerView, touchView, continueButton, common);
        }
    }

    public static abstract class GivenCommonValues extends GivenChildViews {
        Step.Common common;

        @Before
        public void givenCommonValues() {
            common = mock(Step.Common.class);

            when(common.getIconActiveBackgroundPaint()).thenReturn(ICON_ACTIVE_PAINT);
            when(common.getIconInactiveBackgroundPaint()).thenReturn(ICON_INACTIVE_PAINT);
            when(common.getIconCompleteBackgroundPaint()).thenReturn(ICON_COMPLETE_PAINT);
        }
    }

    public static abstract class GivenATestStep extends GivenCommonValues {
        TestStep step;

        @Before
        public void givenATestStep() {
            step = new TestStep();
        }

        class TestStep extends Step {
            private float titleWidth;
            private float titleHeight;
            private float summaryWidth;
            private float summaryHeight;

            TestStep() {
                super(innerView, touchView, continueButton, common);
            }

            @Override
            void initTextValues(@NonNull VerticalStepper.LayoutParams lp) {
                // Do nothing. Don't attempt to extract it from null layout params
            }

            @Override
            void validateTitle() {
                // Do nothing. We don't care about title validation
            }

            @Override
            void measureTitleHorizontalDimensions() {
                // Do nothing. The dimensions will be explicitly set
            }

            @Override
            void measureSummaryHorizontalDimensions() {
                // Do nothing. The dimensions will be explicitly set
            }

            @Override
            void measureTitleVerticalDimensions(int heightToCenterIn) {
                // Do nothing. The dimensions will be explicitly set
            }

            @Override
            void measureSummaryVerticalDimensions() {
                // Do nothing. The dimensions will be explicitly set
            }

            TestStep setTestStepTitleWidth(float width) {
                titleWidth = width;
                return this;
            }

            @Override
            public float getTitleWidth() {
                return titleWidth;
            }

            TestStep setTestStepTitleHeight(float height) {
                titleHeight = height;
                return this;
            }

            @Override
            float getTitleBottomRelativeToStepTop() {
                return titleHeight;
            }

            TestStep setTestStepSummaryWidth(float width) {
                summaryWidth = width;
                return this;
            }

            @Override
            public float getSummaryWidth() {
                return summaryWidth;
            }

            TestStep setTestStepSummaryHeight(float height) {
                summaryHeight = height;
                return this;
            }

            @Override
            float getSummaryBottomRelativeToTitleBottom() {
                return summaryHeight;
            }
        }
    }

    public static class GivenEmptyTestStep extends GivenATestStep {
        @Test
        public void calculateHorizontalUsedSpace_ShouldReturnPaddingPlusIconLeftAdjustment() {
            int leftMargin = 20;
            int rightMargin = 10;
            innerLayoutParams.leftMargin = leftMargin;
            innerLayoutParams.rightMargin = rightMargin;

            int horizontalPadding = step.calculateHorizontalUsedSpace(innerView);

            assertThat(horizontalPadding)
                    .isEqualTo(leftMargin + rightMargin + step.calculateStepDecoratorIconWidth());
        }

        @Test
        public void calculateVerticalUsedSpace_ShouldReturnAllMargins() {
            int topMargin = 10;
            int bottomMargin = 20;
            innerLayoutParams.topMargin = topMargin;
            innerLayoutParams.bottomMargin = bottomMargin;

            int verticalPadding = step.calculateVerticalUsedSpace(innerView);

            assertThat(verticalPadding).isEqualTo(topMargin + bottomMargin);
        }

        @Test
        public void calculateStepDecoratorWidth_ShouldReturnIconSumPlusMaxTextWidth() {
            int iconWidth = step.getIconDimension() + step.getIconMarginRight();
            final float textWidth = 10f;
            step.setTestStepTitleWidth(textWidth)
                    .setTestStepSummaryWidth(textWidth);

            int stepDecoratorWidth = step.calculateStepDecoratorWidth();

            assertThat(stepDecoratorWidth)
                    .isEqualTo(iconWidth + (int) textWidth);
        }

        @Test
        public void calculateStepDecoratorIconWidth_ShouldReturnIconWidthPlusMarginSum() {
            int iconWidth = step.calculateStepDecoratorIconWidth();

            assertThat(iconWidth)
                    .isEqualTo(step.getIconDimension() + step.getIconMarginRight());
        }

        @Test
        public void calculateStepDecoratorTextWidth_WiderTitle_ShouldReturnTitle() {
            final float titleWidth = 20f;
            final float summaryWidth = 10f;
            step.setTestStepTitleWidth(titleWidth)
                    .setTestStepSummaryWidth(summaryWidth);

            float width = step.calculateStepDecoratorTextWidth();

            assertThat(width).isEqualTo(titleWidth);
        }

        @Test
        public void calculateStepDecoratorTextWidth_WiderSummary_ShouldReturnSummary() {
            final float titleWidth = 20f;
            final float summaryWidth = 25f;
            step.setTestStepTitleWidth(titleWidth)
                    .setTestStepSummaryWidth(summaryWidth);

            float width = step.calculateStepDecoratorTextWidth();

            assertThat(width).isEqualTo(summaryWidth);
        }

        @Test
        public void measureStepDecoratorHeight_TallerIcon_ShouldReturnIconHeight() {
            int iconDimension = step.getIconDimension();
            float lessThanHalfIconHeight = (iconDimension - 2) / 2;
            step.setTestStepTitleHeight(lessThanHalfIconHeight)
                    .setTestStepSummaryHeight(lessThanHalfIconHeight);

            step.measureStepDecoratorHeight();
            int height = step.getDecoratorHeight();

            assertThat(height).isEqualTo(iconDimension);
        }

        @Test
        public void measureStepDecoratorHeight_TallerText_ShouldReturnTextHeight() {
            float twiceIconHeight = step.getIconDimension() * 2;
            step.setTestStepTitleHeight(twiceIconHeight)
                    .setTestStepSummaryHeight(twiceIconHeight);

            step.measureStepDecoratorHeight();
            int height = step.getDecoratorHeight();

            assertThat(height).isEqualTo((int) (twiceIconHeight + twiceIconHeight));
        }
    }

    public static abstract class GivenTestStepWithStandardHeights extends GivenATestStep {
        static final float STANDARD_TITLE_HEIGHT = 10f;
        static final float STANDARD_SUMMARY_HEIGHT = 10f;
        static final int STANDARD_INNER_HEIGHT = 100;
        static final int STANDARD_CONTINUE_HEIGHT = 20;

        @Before
        public void givenTestStepWithStandardTextHeights() {
            step.setTestStepTitleHeight(STANDARD_TITLE_HEIGHT)
                    .setTestStepSummaryHeight(STANDARD_SUMMARY_HEIGHT);

            when(innerView.getHeight()).thenReturn(STANDARD_INNER_HEIGHT);
            when(continueButton.getHeight()).thenReturn(STANDARD_CONTINUE_HEIGHT);
        }
    }

    public static class GivenInactiveTestStepWithStandardHeights extends GivenTestStepWithStandardHeights {
        @Before
        public void givenInactiveTestStepWithStandardHeights() {
            step.setActive(false);
        }

        @Test
        public void calculateYDistanceToNextStep_ShouldReturnTotalTextHeightPlusBottomMargin() {
            int inactiveBottomMargin = 40;
            when(common.getInactiveBottomMarginToNextStep()).thenReturn(inactiveBottomMargin);

            int yDistance = step.calculateYDistanceToNextStep();

            assertThat(yDistance)
                    .isEqualTo((int) (STANDARD_TITLE_HEIGHT + STANDARD_SUMMARY_HEIGHT
                            + inactiveBottomMargin));
        }
    }

    public static class GivenActiveTestStepWithStandardHeights extends GivenTestStepWithStandardHeights {
        @Before
        public void givenActiveTestStepWithStandardHeights() {
            step.setActive(true);
        }

        @Test
        public void
        calculateYDistanceToNextStep_ShouldReturnTitleHeightPlusTitleMarginPlusTotalInnerHeightPlusBottomMargin() {
            int titleMargin = 20;
            int activeBottomMargin = 40;
            when(common.getTitleMarginBottomToInnerView()).thenReturn(titleMargin);
            when(common.getActiveBottomMarginToNextStep()).thenReturn(activeBottomMargin);

            int yDistance = step.calculateYDistanceToNextStep();

            assertThat(yDistance)
                    .isEqualTo((int) (STANDARD_TITLE_HEIGHT + titleMargin
                            + STANDARD_INNER_HEIGHT + STANDARD_CONTINUE_HEIGHT
                            + activeBottomMargin));
        }
    }

    public static abstract class GivenAStep extends GivenCommonValues {
        Step step;

        @Before
        public void givenAStep() {
            step = createStep(common);
        }
    }

    public static class GivenStepIsInactive extends GivenAStep {
        @Before
        public void givenStepIsInactive() {
            step.setActive(false);
        }

        @Test
        public void getIconBackground_ShouldReturnInactiveStepPaint() {
            Paint paint = step.getIconBackground();

            assertThat(paint).isSameAs(ICON_INACTIVE_PAINT);
        }

        @Test
        public void getIconBackground_CompleteStep_ShouldReturnCompleteStepPaint() {
            step.setComplete(true);

            Paint paint = step.getIconBackground();

            assertThat(paint).isSameAs(ICON_COMPLETE_PAINT);
        }

        @Test
        public void getTitleTextPaint_ShouldReturnInactiveStepPaint() {
            TextPaint inactivePaint = mock(TextPaint.class);
            when(common.getTitleInactiveTextPaint()).thenReturn(inactivePaint);

            TextPaint paint = step.getTitleTextPaint();

            assertThat(paint).isSameAs(inactivePaint);
        }

        @Test
        public void getTitleTextPaint_CompleteStep_ShouldReturnCompleteStepPaint() {
            step.setComplete(true);
            TextPaint completePaint = mock(TextPaint.class);
            when(common.getTitleCompleteTextPaint()).thenReturn(completePaint);

            TextPaint paint = step.getTitleTextPaint();

            assertThat(paint).isSameAs(completePaint);
        }

        @Test
        public void getBottomMarginToNextStep_ShouldReturnInactiveMargin() {
            int inactiveBottomMargin = 40;
            when(common.getInactiveBottomMarginToNextStep()).thenReturn(inactiveBottomMargin);

            int margin = step.getBottomMarginToNextStep();

            assertThat(margin).isEqualTo(inactiveBottomMargin);
        }
    }

    public static class GivenStepIsActive extends GivenAStep {
        @Before
        public void givenStepIsActive() {
            step.setActive(true);
        }

        @Test
        public void getIconBackground_ShouldReturnActiveStepPaint() {
            Paint paint = step.getIconBackground();

            assertThat(paint).isSameAs(ICON_ACTIVE_PAINT);
        }

        @Test
        public void getIconBackground_CompleteStep_ShouldReturnActiveStepPaint() {
            step.setComplete(true);

            Paint paint = step.getIconBackground();

            assertThat(paint).isSameAs(ICON_ACTIVE_PAINT);
        }

        @Test
        public void getTitleTextPaint_ShouldReturnActiveStepPaint() {
            TextPaint activePaint = mock(TextPaint.class);
            when(common.getTitleActiveTextPaint()).thenReturn(activePaint);

            TextPaint paint = step.getTitleTextPaint();

            assertThat(paint).isSameAs(activePaint);
        }

        @Test
        public void getTitleTextPaint_CompleteStep_ShouldReturnActiveStepPaint() {
            step.setComplete(true);
            TextPaint activePaint = mock(TextPaint.class);
            when(common.getTitleActiveTextPaint()).thenReturn(activePaint);

            TextPaint paint = step.getTitleTextPaint();

            assertThat(paint).isSameAs(activePaint);
        }

        @Test
        public void getBottomMarginToNextStep_ShouldReturnActiveMargin() {
            int activeBottomMargin = 40;
            when(common.getActiveBottomMarginToNextStep()).thenReturn(activeBottomMargin);

            int margin = step.getBottomMarginToNextStep();

            assertThat(margin).isEqualTo(activeBottomMargin);
        }
    }

    public static class GivenMockedCommon extends GivenChildViews {
        private TextPaint titleInactivePaint;
        private TextPaint summaryPaint;
        private Rect titleRect;

        private Step step;
        private Step.Common common;

        @Before
        public void givenMockedCommon() {
            common = mock(Step.Common.class);

            titleInactivePaint = mock(TextPaint.class);
            titleRect = mock(Rect.class);
            when(titleInactivePaint.getFontMetrics()).thenReturn(mock(Paint.FontMetrics.class));
            when(common.getTitleInactiveTextPaint()).thenReturn(titleInactivePaint);
            when(common.getTempRectForTitleTextBounds()).thenReturn(titleRect);

            summaryPaint = mock(TextPaint.class);
            when(summaryPaint.getFontMetrics()).thenReturn(mock(Paint.FontMetrics.class));
            when(common.getSummaryTextPaint()).thenReturn(summaryPaint);

            step = createStep(common);
        }

        @Test
        public void measureTitleHorizontalDimensions_MeasuresUsingTitlePaint() {
            step.measureTitleHorizontalDimensions();

            verify(titleInactivePaint).measureText(step.getTitle());
        }

        @Test
        public void measureSummaryHorizontalDimensions_MeasuresUsingSummaryPaint() {
            step.measureSummaryHorizontalDimensions();

            verify(summaryPaint).measureText(step.getSummary());
        }

        @Test
        public void measureTitleVerticalDimensions_MeasuresUsingTitlePaint() {
            step.measureTitleVerticalDimensions(0);

            // verify that the baseline is being measured using the text bounds
            verify(titleInactivePaint).getTextBounds(step.getTitle(), 0, 1, titleRect);
            // verify that the bottom is being measured using the font metrics
            verify(titleInactivePaint).getFontMetrics();
        }

        @Test
        public void measureSummaryVerticalDimensions_MeasuresUsingSummaryPaint() {
            step.measureSummaryVerticalDimensions();

            // verify that the baseline and bottom are measured using the font metrics
            verify(summaryPaint, times(2)).getFontMetrics();
        }

        @Test
        public void calculateConnectorStartY_ShouldAccountForIcon() {
            int iconDimension = 24;
            int iconMarginVertical = 8;
            when(common.getIconDimension()).thenReturn(iconDimension);
            when(common.getIconMarginVertical()).thenReturn(iconMarginVertical);

            int startY = step.calculateConnectorStartY();

            assertThat(startY)
                    .isEqualTo(iconDimension + iconMarginVertical);
        }

        @Test
        public void calculateConnectorStopY_ShouldStopAtIconMargin() {
            int yDistance = 300;
            int iconMarginVertical = 8;
            when(common.getIconMarginVertical()).thenReturn(iconMarginVertical);

            int startY = step.calculateConnectorStopY(yDistance);

            assertThat(startY)
                    .isEqualTo(yDistance - iconMarginVertical);
        }
    }
}