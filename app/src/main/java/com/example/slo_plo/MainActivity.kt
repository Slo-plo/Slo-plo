package com.example.slo_plo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.slo_plo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // navController 연결
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // bottomNavigationView 연결
        binding.bottomNavigationView.setupWithNavController(navController)

        // 이동할 Fragment에 따라 BottomNavigationView 숨기거나 보이게 하기
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.ploggingFragment) {  // 바텀 네비 숨겨야 하는 경우 여기에 추가
                // PloggingFragment로 이동 시 BottomNavigationView 숨기기
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }
}
