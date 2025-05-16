package com.example.slo_plo

enum class OnboardingGuide (val title: String, val content: String, val imageResId: Int) {
    PAGE_1(
        "플로깅을 측정해요",
        "나의 위치를 기반으로\n플로깅을 측정해요",
        R.drawable.img_guide_1
    ),
    PAGE_2(
        "플로깅을 기록해요",
        "측정한 플로깅을 일지로 저장하고,\nSNS를 통해 공유도 가능해요",
        R.drawable.img_guide_2
    ),
    PAGE_3(
        "봉사활동에 참여해요!",
        "환경 봉사활동도 앱을 통해\n쉽게 확인해요",
        R.drawable.img_guide_3
    )
}