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

class ExpandableTextView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr),
    View.OnClickListener,
    ValueAnimator.AnimatorUpdateListener {

    private var originalText: CharSequence = ""
    private var isCollapsing = false
    private var isUpdatingCollapsedText = false
    private lateinit var animator: ValueAnimator

    private var attrExpandActionHint: String = ""
    private var attrCollapsedMaxLines = Int.MAX_VALUE
    private var attrAnimationDurationInMillis = 0L

    init {
        context.withStyledAttributes(attrs, R.styleable.ExpandableTextView) {
            attrExpandActionHint = "$DEFAULT_ACTION_HINT${getString(R.styleable.ExpandableTextView_expandActionHint).orEmpty()}"
            attrCollapsedMaxLines = getInteger(R.styleable.ExpandableTextView_collapsedMaxLines, maxLines)
            attrAnimationDurationInMillis = getInteger(R.styleable.ExpandableTextView_animationDurationInMillis, 0).toLong()
        }

        originalText = text
        setupView()
        setupAnimator()
    }

    // region override
    override fun setText(text: CharSequence?, type: BufferType?) {
        text?.let {
            if (!isUpdatingCollapsedText) {
                originalText = it
            }
        }
        isUpdatingCollapsedText = false
        super.setText(text, type)
    }

    override fun onClick(v: View?) {
        toggleState()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        val newHeightValue = animation?.animatedValue as? Int ?: height
        updateHeight(animatedValue = newHeightValue)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (animator.isRunning) return

        println("changed: $changed")

        if (lineCount <= maxLines) {
            text = originalText
        } else if (isCollapsed() && !text.contains(attrExpandActionHint)) {
            post { updateCollapsedText() }
        }
    }
    // endregion

    private fun setupView() {
        maxLines = attrCollapsedMaxLines
        setOnClickListener(this@ExpandableTextView)
    }

    private fun setupAnimator() {
        animator = ValueAnimator.ofInt(-1, -1).apply {
            duration = attrAnimationDurationInMillis
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener(this@ExpandableTextView)
            doOnStart { animationStarted() }
            doOnEnd { animationEnded() }
        }
    }

    private fun animationStarted() {
        if (isCollapsed()) {
            isCollapsing = false
            text = originalText
            maxLines = Int.MAX_VALUE
        } else {
            isCollapsing = true
        }
    }

    private fun animationEnded() {
        if (isExpanded() && isCollapsing) {
            maxLines = attrCollapsedMaxLines
            updateCollapsedText()
            isCollapsing = false
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
        return Int.MAX_VALUE != maxLines
    }

    private fun updateHeight(animatedValue: Int) {
        val layoutParams = layoutParams
        layoutParams.height = animatedValue
        setLayoutParams(layoutParams)
    }

    private fun updateCollapsedText() {
        isUpdatingCollapsedText = true

        val visibleTextEnd = layout.getLineVisibleEnd(maxLines - 1)
        val hintReplaceStart = visibleTextEnd - attrExpandActionHint.length

        val styleStart = hintReplaceStart + DEFAULT_ACTION_HINT.length
        val styleEnd = visibleTextEnd + 1

        if (styleEnd > text.length) return

        val finalTextWithHint = buildSpannedString {
            append(text)
            replace(hintReplaceStart, visibleTextEnd, attrExpandActionHint)
            setSpan(UnderlineSpan(), styleStart, styleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), styleStart, styleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        text = finalTextWithHint
    }

    private fun setWrapContent() {
        val layoutParams = layoutParams
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        setLayoutParams(layoutParams)
    }

    private companion object {
        const val DEFAULT_COLLAPSED_MAX_LINES = 2
        const val DEFAULT_ACTION_HINT = "... "
    }
}