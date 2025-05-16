package com.example.slo_plo

import androidx.annotation.DrawableRes

enum class OnboardingIntro (val text: String, @DrawableRes val imageResId: Int) {
    PAGE_1(
        "우리는 매년 800만 톤의 플라스틱을\n바다에 버리고 있습니다.\n\n그 중 상당수는 길거리에\n무심코 버려진 쓰레기에서 시작돼요.",
        R.drawable.img_intro_character
    ),
    PAGE_2(
        "많은 사람들이 이 문제를\n이미 인식하고 있었습니다.\n\n설문조사 결과, 72.1%가\n\"길거리 쓰레기가 해양오염으로 이어진다\"\n고 답했어요.",
        R.drawable.img_intro_graph_1
    ),
    PAGE_3(
        "하지만 실제로 플로깅에 참여해본 사람은\n단 19.2%\n\n알고는 있지만, 실천하는 사람은\n많지 않았습니다.",
        R.drawable.img_intro_graph_2
    ),
    PAGE_4(
        "그래서 우리는\nSlo-plo를 만들었습니다.\n\n가볍지만 진지하게,\n플로깅을 일상처럼 실천할 수 있도록.",
        R.drawable.img_app_character
    )
}