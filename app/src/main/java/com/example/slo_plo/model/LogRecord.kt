package com.example.slo_plo.model
import java.io.Serializable

import java.time.LocalDate

/**
 * 공통으로 사용하는 플로깅 일지 데이터 모델
 */
data class LogRecord(
    // Firestore 문서 ID(yyyy.MM.dd)
    val dateId: String = "",
    // 출발지·도착지
    val startAddress: String = "",
    val endAddress: String = "",
    // 총 시간(분 단위로 저장)
    val time: Int = 0,
    // 총 거리(km 단위)
    val distance: Double = 0.0,
    // 줍은 쓰레기 개수
    val trashCount: Int = 0,
    // 일지 제목·본문
    val title: String = "",
    val body: String = "",
    // 일지 작성 시간
    val writeDateTime: String = "",
    // 사진 URL 리스트
    val imageUrls: List<String> = emptyList(),
    val docId: String? = null
) : Serializable
