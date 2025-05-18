package com.example.slo_plo.model

// 각 속성에 기본값을 추가합니다.
data class RecommendVolunteer(
    val title: String = "", // 기본값 "" 추가
    val description: String = "", // 기본값 "" 추가
    val location: String = "", // 기본값 "" 추가
    val date: String = "", // 기본값 "" 추가
    val link: String = "" // 기본값 "" 추가
)