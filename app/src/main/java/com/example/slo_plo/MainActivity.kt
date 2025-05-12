package com.example.slo_plo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.slo_plo.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱에서 파이어베이스 초기화
        FirebaseApp.initializeApp(this)

        // Android 13+ 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.POST_NOTIFICATIONS"),
                    1001
                )
            }
        }

        // NavHostFragment & NavController 초기화
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 온보딩 여부 확인 후 startDestination 설정
        val prefs = getSharedPreferences("slo_plo_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("isFirstLaunch", true)

        val navInflater = navController.navInflater
        val navGraph: NavGraph = navInflater.inflate(R.navigation.nav_graph)

        navGraph.setStartDestination(
            if (isFirstLaunch) R.id.onboardingIntroFragment else R.id.homeFragment
        )
        navController.graph = navGraph

        // 바텀 네비게이션 설정
        binding.bottomNavigationView.setupWithNavController(navController)

        // 특정 화면에서는 바텀 네비 숨김
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNav = when (destination.id) {
                R.id.ploggingFragment,
                R.id.logWriteFragment,
                R.id.onboardingIntroFragment,
                R.id.onboardingGuideFragment -> true
                else -> false
            }
            binding.bottomNavigationView.visibility = if (hideNav) View.GONE else View.VISIBLE

        }
    }
}
