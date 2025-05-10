package com.example.slo_plo

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
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

        // navController 연결
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // bottomNavigationView 연결
        binding.bottomNavigationView.setupWithNavController(navController)

        // 이동할 Fragment에 따라 BottomNavigationView 숨기거나 보이게 하기
        navController.addOnDestinationChangedListener { _, destination, _ ->
//            if (destination.id == R.id.ploggingFragment) {  // 바텀 네비 숨겨야 하는 경우 여기에 추가
//                // PloggingFragment로 이동 시 BottomNavigationView 숨기기
//                binding.bottomNavigationView.visibility = View.GONE
//            }
            when (destination.id) {
                R.id.ploggingFragment,
                R.id.logWriteFragment -> {
                    // 플로깅 or 일지 작성 화면일 때는 숨김
                    binding.bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}
