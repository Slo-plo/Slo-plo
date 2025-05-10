package com.example.slo_plo.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    /** Firestore 문서ID 포맷 (yyyy.MM.dd) */
    private val firestoreFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy.MM.dd")

    /** 캘린더 셀 클릭 네비게이션용 ID 포맷 */
    fun toDocId(date: LocalDate): String =
        date.format(firestoreFormatter)

    /** 사용자 화면용 날짜 포맷 (예: 2025.05.10) */
    val displayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy.MM.dd")
}
