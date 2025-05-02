package com.example.slo_plo

import android.app.Application
import com.naver.maps.map.NaverMapSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Naver Map SDK 클라이언트 ID 설정
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NcpKeyClient(BuildConfig.NAVER_CLIENT_ID)
    }
}