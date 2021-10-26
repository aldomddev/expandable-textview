package br.com.amd.expandabletextview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.withStyledAttributes
import androidx.core.text.buildSpannedString
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

private typealias onStateChangeListener = (() -> Unit)

class ExpandableTextView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr),
    LifecycleEventObserver,
    View.OnClickListener,
    ValueAnimator.AnimatorUpdateListener {

    private var originalText: CharSequence = ""
    private var isCollapsing = false
    private var isUpdatingCollapsedText = false
    private lateinit var animator: ValueAnimator

    private var attrExpandActionHint: String = ""
    private var attrCollapsedMaxLines = Int.MAX_VALUE

    private var onExpandedListener: onStateChangeListener? = null
    private var onCollapsedListener: onStateChangeListener? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.ExpandableTextView) {
            val expandActionHint = getString(R.styleable.ExpandableTextView_expandActionHint).orEmpty()
            setExpandActionHint(expandActionHint)

            val collapsedMaxLines = getInteger(R.styleable.ExpandableTextView_collapsedMaxLines, maxLines)
            setCollapsedMaxLines(collapsedMaxLines)
        }

        setupView()
        setupAnimator()
    }

    // region override
    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)

        if (text != null && !isUpdatingCollapsedText) {
            originalText = text
        }

        isUpdatingCollapsedText = false
    }

    override fun onClick(v: View?) {
        toggleState()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val newHeightValue = animation.animatedValue as Int
        updateHeight(newHeight = newHeightValue)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (animator.isRunning || isUpdatingCollapsedText) {
            return
        }

        if (lineCount <= attrCollapsedMaxLines) {
            isClickable = false
        } else {
            isClickable = true
            if (isCollapsed()) {
                updateCollapsedText()
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            onExpandedListener =  null
            onCollapsedListener = null
            animator.cancel()
        }
    }
    // endregion

    fun setAnimationDurationTime(lifecycleOwner: LifecycleOwner, timeInMillis: Long = 300) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }

        lifecycleOwner.lifecycle.addObserver(this)
        setAnimationDuration(timeInMillis)
    }

    fun setOnExpandedListener(onExpanded: (() -> Unit)?) {
        this.onExpandedListener = onExpanded
    }

    fun setOnCollapsedListener(onCollapsed: (() -> Unit)?) {
        this.onCollapsedListener = onCollapsed
    }

    private fun setupView() {
        originalText = text
        maxLines = attrCollapsedMaxLines
        setOnClickListener(this@ExpandableTextView)
    }

    private fun setupAnimator() {
        animator = ValueAnimator.ofInt(0, 0).apply {
            duration = DEFAULT_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener(this@ExpandableTextView)
            doOnStart { animationStarted() }
            doOnEnd { animationEnded() }
        }
    }

    private fun setExpandActionHint(actionHint: String) {
        attrExpandActionHint = "$DEFAULT_ACTION_HINT$actionHint"
    }

    private fun setCollapsedMaxLines(collapsedMaxLines: Int) {
        attrCollapsedMaxLines = if (collapsedMaxLines > 0) {
            collapsedMaxLines
        } else {
            DEFAULT_COLLAPSED_MAX_LINES
        }
    }

    private fun setAnimationDuration(durationInMillis: Long) {
        animator.duration = if (durationInMillis >= 0) {
            durationInMillis
        } else {
            DEFAULT_ANIMATION_DURATION
        }
    }

    private fun animationStarted() {
        if (isCollapsed()) {
            maxLines = Int.MAX_VALUE
            text = originalText
        } else {
            isCollapsing = true
        }
    }

    private fun animationEnded() {
        if (isExpanded() && isCollapsing) {
            isCollapsing = false
            maxLines = attrCollapsedMaxLines
            this.onCollapsedListener?.invoke()
        }

        if (isExpanded()) {
            this.onExpandedListener?.invoke()
        }

        setWrapContent()
    }

    private fun toggleState() {
        if (animator.isRunning) {
            return
        }

        animator.setIntValues(getInitialAnimationHeight(), getFinalAnimationHeight())
        animator.start()
    }

    private fun getInitialAnimationHeight(): Int = height

    private fun getFinalAnimationHeight(): Int {
        return if (isCollapsed()) {
            layout.height + getPaddingHeight()
        } else {
            layout.getLineBottom(attrCollapsedMaxLines - 1) + layout.bottomPadding + getPaddingHeight()
        }
    }

    private fun getPaddingHeight(): Int {
        return compoundPaddingBottom + compoundPaddingTop
    }

    private fun isExpanded() = !isCollapsed()

    private fun isCollapsed(): Boolean {
        return maxLines != Int.MAX_VALUE
    }

    private fun updateHeight(newHeight: Int) {
        layoutParams = layoutParams.apply { height = newHeight }
    }

    private fun updateCollapsedText() {
        isUpdatingCollapsedText = true

        val visibleTextEnd = layout.getLineVisibleEnd(maxLines - 1)
        val hintReplaceStart = visibleTextEnd - attrExpandActionHint.length

        val styleStart = hintReplaceStart + DEFAULT_ACTION_HINT.length

        val finalTextWithActionHint = buildSpannedString {
            append(originalText)
            replace(hintReplaceStart, visibleTextEnd, "$attrExpandActionHint\n")
            setSpan(UnderlineSpan(), styleStart, visibleTextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), styleStart, visibleTextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        text = finalTextWithActionHint
    }

    private fun setWrapContent() {
        layoutParams = layoutParams.apply { height = ViewGroup.LayoutParams.WRAP_CONTENT }
    }

    private companion object {
        const val DEFAULT_ACTION_HINT = "${Typography.ellipsis} "
        const val DEFAULT_COLLAPSED_MAX_LINES = 1
        const val DEFAULT_ANIMATION_DURATION = 0L
    }
}