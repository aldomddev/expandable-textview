package br.com.amd.expandabletextview.example

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.Toast
import androidx.core.text.buildSpannedString
import br.com.amd.expandabletextview.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with (binding.theText) {
            // action hint with custom style
//            val actionHintStr = "click for more"
//            val actionHint = buildSpannedString {
//                append(actionHintStr)
//                setSpan(StyleSpan(Typeface.BOLD), 0, actionHintStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                setSpan(UnderlineSpan(), 0, actionHintStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                setSpan(ForegroundColorSpan(Color.RED), 0, actionHintStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//            }
//            setExpandActionHint(actionHint)

            setAnimationDurationTime(
                lifecycleOwner = this@MainActivity,
                timeInMillis = 300
            )

            setOnCollapsedListener {
                Toast.makeText(this@MainActivity, "Collapsed!", Toast.LENGTH_SHORT).show()
            }

            setOnExpandedListener {
                Toast.makeText(this@MainActivity, "Expanded!", Toast.LENGTH_SHORT).show()
            }
        }

        setViewListeners()
    }

    private fun setViewListeners() {
        with(binding) {
            btnSetLongText.setOnClickListener {
                theText.apply {
                    setCollapsedMaxLines(lines = 3)
                    text = getString(R.string.long_text_1)
                    setAnimationDurationTime(
                        lifecycleOwner = this@MainActivity,
                        timeInMillis = 300
                    )
                }
            }

            btnSetLongestText.setOnClickListener {
                theText.apply {
                    setCollapsedMaxLines(lines = 5)
                    text = getString(R.string.long_text_2)
                    setAnimationDurationTime(
                        lifecycleOwner = this@MainActivity,
                        timeInMillis = 600
                    )
                }
            }

            btnSetShortText.setOnClickListener {
                theText.text = getString(R.string.short_text)
            }

            theText.apply {
                setCollapsedMaxLines(lines = 3)
                setAnimationDurationTime(
                    lifecycleOwner = this@MainActivity,
                    timeInMillis = 300
                )

                setOnCollapsedListener {
                    Toast.makeText(this@MainActivity, "Collapsed!", Toast.LENGTH_SHORT).show()
                }

                setOnExpandedListener {
                    Toast.makeText(this@MainActivity, "Expanded!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}