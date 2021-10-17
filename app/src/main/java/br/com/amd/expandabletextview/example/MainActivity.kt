package br.com.amd.expandabletextview.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.amd.expandabletextview.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnSetLongText.setOnClickListener {
                theText.text = getString(R.string.long_text)
            }

            btnSetShortText.setOnClickListener {
                theText.text = getString(R.string.short_text)
            }
        }
    }
}