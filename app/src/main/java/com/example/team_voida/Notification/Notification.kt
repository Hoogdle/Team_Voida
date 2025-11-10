package com.example.team_voida.Notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.team_voida.R
import com.example.team_voida.notifySwitch
import com.example.team_voida.ui.theme.NotifyBlock
import com.example.team_voida.ui.theme.TextColor

// 모든 페이지에 설정되는 상단 알림바 모듈화 컴포저블
@Composable
fun Notification(
    text: String,
    top: Dp = 30.dp,
    bottom: Dp = 10.dp
){
    if(notifySwitch.switch.value) {
        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = top,
                    end = 10.dp,
                    bottom = bottom
                )
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(color = NotifyBlock)
                .padding(15.dp),
            text = text,
            color = TextColor,
            style = TextStyle(
                color = TextColor,
                fontSize = 10.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_light))
            )
        )
    }
}