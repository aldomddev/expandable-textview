package br.com.amd.expandabletextview.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import br.com.amd.expandabletextview.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViewListeners()
    }

    private fun setViewListeners() {
        with(binding) {
            btnSetLongText.setOnClickListener {
                theText.text = getString(R.string.long_text_1)
            }

            btnSetLongestText.setOnClickListener {
                theText.text = getString(R.string.long_text_2)
            }

            btnSetShortText.setOnClickListener {
                theText.text = getString(R.string.short_text)
            }

            theText.apply {
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