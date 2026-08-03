package com.kolakek.pimiwidget.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.kolakek.pimiwidget.databinding.ActivityWeatherBinding

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = topInset)
            insets
        }

        binding.tvTitle.text = "Activity launched from widget"
    }
}
