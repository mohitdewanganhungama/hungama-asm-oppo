package com.hungama.music.utils.customview.scrollingpagerindicator;

import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.hungama.music.R;


public class ScrollingPagerIndicator extends View {

    @IntDef({RecyclerView.HORIZONTAL, RecyclerView.VERTICAL})
    public @interface Orientation {
    }

    private int infiniteDotCount;

    private final int dotMinimumSize;
    private final int dotNormalSize;
    private final int dotSelectedSize;
    private final int spaceBetweenDotCenters;
    private int visibleDotCount;
    private int visibleDotThreshold;
    private int orientation;

    private float visibleFramePosition;
    private float visibleFrameWidth;

    private float firstDotOffset;
    private SparseArray<Float> dotScale;

    private int itemCount;

    private final Paint paint;
    private final ArgbEvaluator colorEvaluator = new ArgbEvaluator();

    @ColorInt
    private int dotColor;

    @ColorInt
    private int selectedDotColor;

    @Nullable
    private final Drawable firstDotDrawable;

    @Nullable
    private final Drawable lastDotDrawable;

    private boolean looped;

    private Runnable attachRunnable;
    private PagerAttacher<?> currentAttacher;
    private boolean autoRtl = true;

    private boolean dotCountInitialized;

    private int currentpos = Integer.MIN_VALUE;

    public ScrollingPagerIndicator(Context context) {
        this(context, null);
    }

    public ScrollingPagerIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.scrollingPagerIndicatorStyle);
    }

    public ScrollingPagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.ScrollingPagerIndicator, defStyleAttr, R.style.ScrollingPagerIndicator);
        dotColor = attributes.getColor(R.styleable.ScrollingPagerIndicator_spi_dotColor, 0);
        selectedDotColor = attributes.getColor(R.styleable.ScrollingPagerIndicator_spi_dotSelectedColor, dotColor);
        dotNormalSize = attributes.getDimensionPixelSize(R.styleable.ScrollingPagerIndicator_spi_dotSize, 0);
        dotSelectedSize = attributes.getDimensionPixelSize(R.styleable.ScrollingPagerIndicator_spi_dotSelectedSize, 0);
        int dotMinimumSize = attributes.getDimensionPixelSize(R.styleable.ScrollingPagerIndicator_spi_dotMinimumSize, -1);
        this.dotMinimumSize = dotMinimumSize <= dotNormalSize ? dotMinimumSize : -1;

        spaceBetweenDotCenters = attributes.getDimensionPixelSize(R.styleable.ScrollingPagerIndicator_spi_dotSpacing, 0) + dotNormalSize;
        looped = attributes.getBoolean(R.styleable.ScrollingPagerIndicator_spi_looped, false);
        int visibleDotCount = attributes.getInt(R.styleable.ScrollingPagerIndicator_spi_visibleDotCount, 0);
        setVisibleDotCount(visibleDotCount);
        visibleDotThreshold = attributes.getInt(R.styleable.ScrollingPagerIndicator_spi_visibleDotThreshold, 2);
        orientation = attributes.getInt(R.styleable.ScrollingPagerIndicator_spi_orientation, RecyclerView.HORIZONTAL);

        firstDotDrawable = attributes.getDrawable(R.styleable.ScrollingPagerIndicator_spi_firstDotDrawable);
        lastDotDrawable = attributes.getDrawable(R.styleable.ScrollingPagerIndicator_spi_lastDotDrawable);
        attributes.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);

        if (isInEditMode()) {
            setDotCount(visibleDotCount);
            onPageScrolled(visibleDotCount / 2, 0);
        }
    }

    /**
     * You should make indicator looped in your PagerAttacher implementation if your custom pager is looped too
     * If pager has less items than visible_dot_count, indicator will work as usual;
     * otherwise it will always be in infinite state.
     *
     * @param looped true if pager is looped
     */
    public void setLooped(boolean looped) {
        this.looped = looped;
        reattach();
        invalidate();
    }

    /**
     * @return not selected dot color
     */
    @ColorInt
    public int getDotColor() {
        return dotColor;
    }

    /**
     * Sets dot color
     *
     * @param color dot color
     */
    public void setDotColor(@ColorInt int color) {
        this.dotColor = color;
        invalidate();
    }

    /**
     * @return the selected dot color
     */
    @ColorInt
    public int getSelectedDotColor() {
        return selectedDotColor;
    }

    /**
     * Sets selected dot color
     *
     * @param color selected dot color
     */
    public void setSelectedDotColor(@ColorInt int color) {
        this.selectedDotColor = color;
        invalidate();
    }

    /**
     * Maximum number of dots which will be visible at the same time.
     * If pager has more pages than visible_dot_count, indicator will scroll to show extra dots.
     * Must be odd number.
     *
     * @return visible dot count
     */
    public int getVisibleDotCount() {
        return visibleDotCount;
    }

    /**
     * Sets visible dot count. Maximum number of dots which will be visible at the same time.
     * If pager has more pages than visible_dot_count, indicator will scroll to show extra dots.
     * Must be odd number.
     *
     * @param visibleDotCount visible dot count
     * @throws IllegalStateException when pager is already attached
     */
    public void setVisibleDotCount(int visibleDotCount) {
        if (visibleDotCount % 2 == 0) {
            throw new IllegalArgumentException("visibleDotCount must be odd");
        }
        this.visibleDotCount = visibleDotCount;
        this.infiniteDotCount = visibleDotCount + 2;

        if (attachRunnable != null) {
            reattach();
        } else {
            requestLayout();
        }
    }

    /**
     * The minimum number of dots which should be visible.
     * If pager has less pages than visibleDotThreshold, no dots will be shown.
     *
     * @return visible dot threshold.
     */
    public int getVisibleDotThreshold() {
        return visibleDotThreshold;
    }

    /**
     * Sets the minimum number of dots which should be visible.
     * If pager has less pages than visibleDotThreshold, no dots will be shown.
     *
     * @param visibleDotThreshold visible dot threshold.
     */
    public void setVisibleDotThreshold(int visibleDotThreshold) {
        this.visibleDotThreshold = visibleDotThreshold;
        if (attachRunnable != null) {
            reattach();
        } else {
            requestLayout();
        }
    }

    /**
     * The visible orientation of the dots
     *
     * @return dot orientation (RecyclerView.HORIZONTAL, RecyclerView.VERTICAL)
     */
    @Orientation
    public int getOrientation() {
        return orientation;
    }

    /**
     * Set the dot orientation
     *
     * @param orientation dot orientation (RecyclerView.HORIZONTAL, RecyclerView.VERTICAL)
     */
    public void setOrientation(@Orientation int orientation) {
        this.orientation = orientation;
        if (attachRunnable != null) {
            reattach();
        } else {
            requestLayout();
        }
    }

    /**
     * Attaches indicator to ViewPager
     *
     * @param pager pager to attach
     */
    public void attachToPager(@NonNull ViewPager pager) {
        attachToPager(pager, new ViewPagerAttacher());
    }

    /**
     * Attaches indicator to ViewPager2
     *
     * @param pager pager to attach
     */
    public void attachToPager(@NonNull ViewPager2 pager) {
        attachToPager(pager, new ViewPager2Attacher());
    }

    /**
     * Attaches indicator to RecyclerView. Use this method if current page of the recycler is centered.
     * All pages must have the same width.
     * Like this:
     * <p>
     * +------------------------------+
     * |---+  +----------------+  +---|
     * |   |  |     current    |  |   |
     * |   |  |      page      |  |   |
     * |---+  +----------------+  +---|
     * +------------------------------+
     *
     * @param recyclerView recycler view to attach
     */
    public void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
        attachToPager(recyclerView, new RecyclerViewAttacher());
    }

    /**
     * Attaches indicator to RecyclerView. Use this method if current page of the recycler isn't centered.
     * All pages must have the same width.
     * Like this:
     * <p>
     * +-|----------------------------+
     * | +--------+  +--------+  +----|
     * | | current|  |        |  |    |
     * | |  page  |  |        |  |    |
     * | +--------+  +--------+  +----|
     * +-|----------------------------+
     * | currentPageOffset
     * |
     *
     * @param recyclerView      recycler view to attach
     * @param currentPageOffset x coordinate of current view left corner/top relative to recycler view
     */
    public void attachToRecyclerView(@NonNull RecyclerView recyclerView, int currentPageOffset) {
        attachToPager(recyclerView, new RecyclerViewAttacher(currentPageOffset));
    }

    /**
     * Attaches to any custom pager
     *
     * @param pager    pager to attach
     * @param attacher helper which should setup this indicator to work with custom pager
     */
    public <T> void attachToPager(@NonNull final T pager, @NonNull final PagerAttacher<T> attacher) {
        detachFromPager();
        attacher.attachToPager(this, pager);
        currentAttacher = attacher;

        attachRunnable = new Runnable() {
            @Override
            public void run() {
                itemCount = -1;
                attachToPager(pager, attacher);
            }
        };
    }

    /**
     * Detaches indicator from pager.
     */
    public void detachFromPager() {
        if (currentAttacher != null) {
            currentAttacher.detachFromPager();
            currentAttacher = null;
            attachRunnable = null;
            autoRtl = true;
        }
        dotCountInitialized = false;
    }

    /**
     * Detaches indicator from pager and attaches it again.
     * It may be useful for refreshing after adapter count change.
     */
    public void reattach() {
        if (attachRunnable != null) {
            attachRunnable.run();
            invalidate();
        }
    }

    /**
     * This method must be called from ViewPager.OnPageChangeListener.onPageScrolled or from some
     * similar callback if you use custom PagerAttacher.
     *
     * @param page   index of the first page currently being displayed
     *               Page position+1 will be visible if offset is nonzero
     * @param offset Value from [0, 1) indicating the offset from the page at position
     */
    public void onPageScrolled(int page, float offset) {
        if (offset < 0 || offset > 1) {
            throw new IllegalArgumentException("Offset must be [0, 1]");
        } else if (page < 0 || page != 0 && page >= itemCount) {
            throw new IndexOutOfBoundsException("page must be [0, adapter.getItemCount())");
        } else {
            currentpos = page;
        }

        if (!looped || itemCount <= visibleDotCount && itemCount > 1) {
            dotScale.clear();

            if (orientation == LinearLayout.HORIZONTAL) {
                scaleDotByOffset(page, offset);

                if (page < itemCount - 1) {
                    scaleDotByOffset(page + 1, 1 - offset);
                } else if (itemCount > 1) {
                    scaleDotByOffset(0, 1 - offset);
                }
            } else { // Vertical orientation
                scaleDotByOffset(page - 1, offset);
                scaleDotByOffset(page, 1 - offset);
            }

            invalidate();
        }
        if (orientation == LinearLayout.HORIZONTAL) {
            adjustFramePosition(offset, page);
        } else {
            adjustFramePosition(offset, page - 1);
        }
        invalidate();
    }

    /**
     * Sets dot count
     *
     * @param count new dot count
     */
    public void setDotCount(int count) {
        initDots(count);
    }

    /**
     * Sets currently selected position (according to your pager's adapter)
     *
     * @param position new current position
     */
    public void setCurrentPosition(int position) {
        if (position != 0 && (position < 0 || position >= itemCount)) {
            throw new IndexOutOfBoundsException("Position must be [0, adapter.getItemCount()]");
        }
        if (itemCount == 0) {
            return;
        }
        adjustFramePosition(0, position);
        updateScaleInIdleState(position);
    }

    /**
     * Sets Rtl direction availability when the view has Rtl direction.
     * autoRtl is on by default.
     *
     * @param autoRtl false means rtl direction doesn't be apply even if view direction is Rtl.
     */
    public void setAutoRtl(boolean autoRtl) {
        this.autoRtl = autoRtl;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Width
        int measuredWidth;
        // Height
        int measuredHeight;

        if (orientation == LinearLayoutManager.HORIZONTAL) {
            // We ignore widthMeasureSpec because width is based on visibleDotCount
            if (isInEditMode()) {
                // Maximum width with all dots visible
                measuredWidth = (visibleDotCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
            } else {
                measuredWidth = itemCount >= visibleDotCount
                        ? (int) visibleFrameWidth
                        : (itemCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
            }
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            // Height
            int desiredHeight = dotSelectedSize;

            switch (heightMode) {
                case MeasureSpec.EXACTLY:
                    measuredHeight = heightSize;
                    break;
                case MeasureSpec.AT_MOST:
                    measuredHeight = Math.min(desiredHeight, heightSize);
                    break;
                case MeasureSpec.UNSPECIFIED:
                default:
                    measuredHeight = desiredHeight;
            }
        } else {
            if (isInEditMode()) {
                measuredHeight = (visibleDotCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
            } else {
                measuredHeight = itemCount >= visibleDotCount
                        ? (int) visibleFrameWidth
                        : (itemCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
            }

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);

            // Width
            int desiredWidth = dotSelectedSize;

            switch (widthMode) {
                case MeasureSpec.EXACTLY:
                    measuredWidth = widthSize;
                    break;
                case MeasureSpec.AT_MOST:
                    measuredWidth = Math.min(desiredWidth, widthSize);
                    break;
                case MeasureSpec.UNSPECIFIED:
                default:
                    measuredWidth = desiredWidth;
            }
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onDraw(Canvas canvas) {
        int dotCount = getDotCount();
        if (dotCount < visibleDotThreshold) {
            return;
        }

        // Some empirical coefficients
        float scaleDistance = (spaceBetweenDotCenters + (dotSelectedSize - dotNormalSize) / 2) * 0.7f;
        float smallScaleDistance = dotSelectedSize / 2;
        float centerScaleDistance = 6f / 7f * spaceBetweenDotCenters;

        int firstVisibleDotPos = (int) (visibleFramePosition - firstDotOffset) / spaceBetweenDotCenters;
        int lastVisibleDotPos = firstVisibleDotPos
                + (int) (visibleFramePosition + visibleFrameWidth - getDotOffsetAt(firstVisibleDotPos))
                / spaceBetweenDotCenters;

        // If real dots count is less than we can draw inside visible frame, we move lastVisibleDotPos
        // to the last item
        if (firstVisibleDotPos == 0 && lastVisibleDotPos + 1 > dotCount) {
            lastVisibleDotPos = dotCount - 1;
        }

        for (int i = firstVisibleDotPos; i <= lastVisibleDotPos; i++) {
            float dot = getDotOffsetAt(i);
            if (dot >= visibleFramePosition && dot < visibleFramePosition + visibleFrameWidth) {
                float diameter;
                float scale;

                // Calculate scale according to current page position
                if (looped && itemCount > visibleDotCount) {
                    float frameCenter = visibleFramePosition + visibleFrameWidth / 2;
                    if (dot >= frameCenter - centerScaleDistance
                            && dot <= frameCenter) {
                        scale = (dot - frameCenter + centerScaleDistance) / centerScaleDistance;
                    } else if (dot > frameCenter
                            && dot < frameCenter + centerScaleDistance) {
                        scale = 1 - (dot - frameCenter) / centerScaleDistance;
                    } else {
                        scale = 0;
                    }
                } else {
                    scale = getDotScaleAt(i);
                }
                diameter = dotNormalSize + (dotSelectedSize - dotNormalSize) * scale;

                // Additional scale for dots at corners
                if (itemCount > visibleDotCount) {
                    float currentScaleDistance;
                    if (!looped && (i == 0 || i == dotCount - 1)) {
                        currentScaleDistance = smallScaleDistance;
                    } else {
                        currentScaleDistance = scaleDistance;
                    }

                    int size = getWidth();
                    if (orientation == LinearLayoutManager.VERTICAL) {
                        size = getHeight();
                    }
                    if (dot - visibleFramePosition < currentScaleDistance) {
                        float calculatedDiameter = diameter * (dot - visibleFramePosition) / currentScaleDistance;
                        if (calculatedDiameter <= dotMinimumSize) {
                            diameter = dotMinimumSize;
                        } else if (calculatedDiameter < diameter) {
                            diameter = calculatedDiameter;
                        }
                    } else if (dot - visibleFramePosition > size - currentScaleDistance) {
                        float calculatedDiameter = diameter * (-dot + visibleFramePosition + size) / currentScaleDistance;
                        if (calculatedDiameter <= dotMinimumSize) {
                            diameter = dotMinimumSize;
                        } else if (calculatedDiameter < diameter) {
                            diameter = calculatedDiameter;
                        }
                    }
                }

                paint.setColor(calculateDotColor(scale));

//                for(int j = firstVisibleDotPos; j<=lastVisibleDotPos; j++){
                final Drawable dotDrawable;
//                    paint.setColor(calculateDotColor(scale));

//                    if(j==i){
                if (currentpos != Integer.MIN_VALUE && i == currentpos) {
                    dotDrawable = null;
                } else
                    dotDrawable = firstDotDrawable;

                if (dotDrawable != null) {
                    if (orientation == LinearLayoutManager.HORIZONTAL) {
                        dotDrawable.setBounds((int) (dot - visibleFramePosition - dotSelectedSize / 2),
                                getMeasuredHeight() / 2 - dotSelectedSize / 2,
                                (int) (dot - visibleFramePosition + dotSelectedSize / 2),
                                getMeasuredHeight() / 2 + dotSelectedSize / 2);
                    } else {
                        dotDrawable.setBounds(getMeasuredWidth() / 2 - dotSelectedSize / 2,
                                (int) (dot - visibleFramePosition - dotSelectedSize / 2),
                                getMeasuredWidth() / 2 + dotSelectedSize / 2,
                                (int) (dot - visibleFramePosition + dotSelectedSize / 2));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        dotDrawable.setTint(paint.getColor());
                    }
                    dotDrawable.draw(canvas);
                } else if (orientation == LinearLayoutManager.HORIZONTAL) {
                    float cx = dot - visibleFramePosition;
                    if (autoRtl && isRtl()) {
                        cx = getWidth() - cx;
                    }

                    canvas.drawCircle(cx,
                            getMeasuredHeight() / 2,
                            diameter / 2,
                            paint);
                } else {
                    canvas.drawCircle(getMeasuredWidth() / 2,
                            dot - visibleFramePosition,
                            diameter / 2,
                            paint);
                }
//                }

            }
        }
    }

    private boolean isRtl() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    @ColorInt
    private int calculateDotColor(float dotScale) {
        return (Integer) colorEvaluator.evaluate(dotScale, dotColor, selectedDotColor);
    }

    private int calculateNonSelectedDotColor(float dotScale) {
        int dotColor = getResources().getColor(R.color.colorWhite1);
        int selectedDotColor = getResources().getColor(R.color.colorWhite1);
        return (Integer) colorEvaluator.evaluate(dotScale, dotColor, selectedDotColor);
    }

    private void updateScaleInIdleState(int currentPos) {
        if (!looped || itemCount < visibleDotCount) {
            dotScale.clear();
            dotScale.put(currentPos, 1f);
            invalidate();
        }
    }

    private void initDots(int itemCount) {
        if (this.itemCount == itemCount && dotCountInitialized) {
            return;
        }
        this.itemCount = itemCount;
        dotCountInitialized = true;
        dotScale = new SparseArray<>();

        if (itemCount < visibleDotThreshold) {
            requestLayout();
            invalidate();
            return;
        }

        firstDotOffset = looped && this.itemCount > visibleDotCount ? 0 : dotSelectedSize / 2;
        visibleFrameWidth = (visibleDotCount - 1) * spaceBetweenDotCenters + dotSelectedSize;

        requestLayout();
        invalidate();
    }

    private int getDotCount() {
        if (looped && itemCount > visibleDotCount) {
            return infiniteDotCount;
        } else {
            return itemCount;
        }
    }

    private void adjustFramePosition(float offset, int pos) {
        if (itemCount <= visibleDotCount) {
            // Without scroll
            visibleFramePosition = 0;
        } else if (!looped && itemCount > visibleDotCount) {
            // Not looped with scroll
            float center = getDotOffsetAt(pos) + spaceBetweenDotCenters * offset;
            visibleFramePosition = center - visibleFrameWidth / 2;

            // Block frame offset near start and end
            int firstCenteredDotIndex = visibleDotCount / 2;
            float lastCenteredDot = getDotOffsetAt(getDotCount() - 1 - firstCenteredDotIndex);
            if (visibleFramePosition + visibleFrameWidth / 2 < getDotOffsetAt(firstCenteredDotIndex)) {
                visibleFramePosition = getDotOffsetAt(firstCenteredDotIndex) - visibleFrameWidth / 2;
            } else if (visibleFramePosition + visibleFrameWidth / 2 > lastCenteredDot) {
                visibleFramePosition = lastCenteredDot - visibleFrameWidth / 2;
            }
        } else {
            // Looped with scroll
            float center = getDotOffsetAt(infiniteDotCount / 2) + spaceBetweenDotCenters * offset;
            visibleFramePosition = center - visibleFrameWidth / 2;
        }
    }

    private void scaleDotByOffset(int position, float offset) {
        if (dotScale == null || getDotCount() == 0) {
            return;
        }
        setDotScaleAt(position, 1 - Math.abs(offset));
    }

    private float getDotOffsetAt(int index) {
        return firstDotOffset + index * spaceBetweenDotCenters;
    }

    private float getDotScaleAt(int index) {
        Float scale = dotScale.get(index);
        if (scale != null) {
            return scale;
        }
        return 0;
    }

    private void setDotScaleAt(int index, float scale) {
        if (scale == 0) {
            dotScale.remove(index);
        } else {
            dotScale.put(index, scale);
        }
    }

    /**
     * Interface for attaching to custom pagers.
     *
     * @param <T> custom pager's class
     */
    public interface PagerAttacher<T> {

        /**
         * Here you should add all needed callbacks to track pager's item count, position and offset
         * You must call:
         * {@link ScrollingPagerIndicator#setDotCount(int)} - initially and after page selection,
         * {@link ScrollingPagerIndicator#setCurrentPosition(int)} - initially and after page selection,
         * {@link ScrollingPagerIndicator#onPageScrolled(int, float)} - in your pager callback to track scroll offset,
         * {@link ScrollingPagerIndicator#reattach()} - each time your adapter items change.
         *
         * @param indicator indicator
         * @param pager     pager to attach
         */
        void attachToPager(@NonNull ScrollingPagerIndicator indicator, @NonNull T pager);

        /**
         * Here you should unregister all callbacks previously added to pager and adapter
         */
        void detachFromPager();
    }
}
