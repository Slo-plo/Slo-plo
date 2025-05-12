package com.example.slo_plo

enum class OnboardingGuide (val title: String, val content: String, val imageResId: Int) {
    PAGE_1(
        "플로깅을 기록해요!",
        "슬로플로를 통해 플로깅을 측정하고\n나의 기록을 확인해요",
        R.drawable.img_guide
    ),
    PAGE_2(
        "일지를 남기고 공유해요!",
        "기록한 플로깅을 일지로 저장하고,\nSNS를 통해 공유할 수 있어요",
        R.drawable.img_guide
    ),
    PAGE_3(
        "봉사활동에 참여해요!",
        "환경 봉사활동도 앱을 통해\n편하게 신청하고 확인해요",
        R.drawable.img_guide
    )
}