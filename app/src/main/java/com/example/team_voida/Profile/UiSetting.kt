package com.example.team_voida.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.Tools.MainViewModel
import com.example.team_voida.themeInStart
import com.example.team_voida.ui.theme.BackGroundWhite
import com.example.team_voida.ui.theme.BasketPaymentColor
import com.example.team_voida.ui.theme.ButtonBlackColor
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.CancelColor
import com.example.team_voida.ui.theme.DeepHighContrastGreen
import com.example.team_voida.ui.theme.DeepHighContrastPink
import com.example.team_voida.ui.theme.DeepHighContrastRed
import com.example.team_voida.ui.theme.DisabledText
import com.example.team_voida.ui.theme.HighContrastBlue
import com.example.team_voida.ui.theme.HighContrastGreen
import com.example.team_voida.ui.theme.HighContrastLightBlue
import com.example.team_voida.ui.theme.HighContrastPink
import com.example.team_voida.ui.theme.HighContrastRed
import com.example.team_voida.ui.theme.HighContrastYellow
import com.example.team_voida.ui.theme.IconBlue
import com.example.team_voida.ui.theme.LightPink
import com.example.team_voida.ui.theme.LoginTextFiled
import com.example.team_voida.ui.theme.NotifyBlock
import com.example.team_voida.ui.theme.SearchBarColor
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.SkyBlue
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import com.example.team_voida.ui.theme.Unselected
import com.example.team_voida.ui.theme.WishButton
import com.example.team_voida.ui.theme.PaymentCard

@Composable
fun UiSetting(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    Log.e("Address", "Recycle")

    val scrollState = rememberScrollState()

    // 유저 정보 페이지에 해당하는 하단 네비 Flag Bit 활성화
    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
        } else if (event == Lifecycle.Event.ON_STOP) {
        } else if (event == Lifecycle.Event.ON_DESTROY) {
        } else if (event == Lifecycle.Event.ON_CREATE) {
        } else if (event == Lifecycle.Event.ON_START) {
            basketFlag.value = false
            homeNavFlag.value = true
            productFlag.value = false

        } else if (event == Lifecycle.Event.ON_RESUME) {
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackGroundWhite)
            .verticalScroll(scrollState)

    ) {
        Notification("화면 디자인 설정 페이지입니다. 아래에 설정을 통해 화면 안내메시지 비활성화 하거나, 고대비 색상을 적용할 수 있습니다.")

        Spacer(Modifier.height(10.dp))

        ContrastRow(0,viewModel)
        ContrastRow(1,viewModel)
        ContrastRow(2,viewModel)
        ContrastRow(3,viewModel)

    }
}


fun UiSetName(codeNum: Int):String{
    lateinit var result:String
    when(codeNum){
        0 -> result = "하얀 · 파랑(기본값)"
        1 -> result = "    남색 · 연분홍"
        2 -> result = "     청록 · 노랑"
        3 -> result = "    차색 · 연하늘"
    }

    return result
}

fun UiSetIcon(codeNum: Int):Int{
    var result: Int = R.drawable.high_default
    when(codeNum){
        0 -> result = R.drawable.high_default
        1 -> result = R.drawable.high_bluepink
        2 -> result = R.drawable.high_greenyellow
        3 -> result = R.drawable.high_redskyblue
    }

    return result
}

fun UiSetHelper(
    codeNum: Int,
    viewModel: MainViewModel
){
    when(codeNum){
        0 -> {
            defaultColor()
            themeInStart.themeId.value = 0
            viewModel.setTheme(0)
        }
        1 -> {
            themeNavyPink()
            themeInStart.themeId.value = 1
            viewModel.setTheme(1)
        }
        2 -> {
            themeGreenYellow()
            themeInStart.themeId.value = 2
            viewModel.setTheme(2)
        }
        3 -> {
            themeRedSkyBlue()
            themeInStart.themeId.value = 3
            viewModel.setTheme(3)
        }
    }
}

@Composable
fun ContrastRow(
    whichColor: Int,
    viewModel: MainViewModel
){
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(
                horizontal = 15.dp,
                vertical = 10.dp
            )
            .shadow(elevation = 7.dp, shape = RoundedCornerShape(15.dp))
        ,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonColors(
            containerColor = BackGroundWhite,
            contentColor = TextLittleDark,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.Gray
        )
        ,
        onClick = {
            UiSetHelper(whichColor, viewModel)
        }
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.width(30.dp))
            Image(
                modifier = Modifier.size(30.dp),
                painter = painterResource(UiSetIcon(whichColor)),
                contentDescription = ""
            )
            Spacer(Modifier.width(50.dp))

            Text(
                textAlign = TextAlign.Center,
                text = UiSetName(whichColor).toString(),
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_semibold)),
                )
            )
        }
    }
}


fun defaultColor(){
    ButtonBlue = Color(0,76,255)
    NotifyBlock = Color(249,249,249)
    Selected = Color(229,235,252)
    TextColor = Color.Black
    IconBlue = Color(0,76,255)
    WishButton = Color(249,249,249)
    TextWhite = Color(243,243,243)
    TextLittleDark = Color(32,32,32)
    LoginTextFiled = Color(248,248,248)
    SearchBarColor = Color(248,248,248)
    BasketPaymentColor = Color(245,245,245)
    ButtonBlackColor = Color(32,32,32)
    SkyBlue = Color(229,235,252)
    LightPink = Color(255,235,235)
    Unselected = Color(249,249,249)
    DisabledText = Color(188,188,188)
    PaymentCard = Color(241,244,254)
    CancelColor = Color(248,17,64)
    BackGroundWhite = Color.White
}

// 파랑 분홍
fun themeNavyPink(){
    ButtonBlue = HighContrastBlue
    NotifyBlock = DeepHighContrastPink
    Selected = HighContrastPink
    TextColor = HighContrastBlue
    IconBlue = HighContrastBlue
    WishButton = HighContrastPink
    TextWhite = HighContrastPink
    TextLittleDark = HighContrastBlue
    LoginTextFiled = HighContrastPink
    SearchBarColor = HighContrastPink
    BasketPaymentColor = HighContrastPink
    ButtonBlackColor = HighContrastBlue
    SkyBlue = HighContrastPink
    LightPink = HighContrastPink
    Unselected = HighContrastPink
    DisabledText = HighContrastPink
    PaymentCard = HighContrastPink
    CancelColor = Color(248,17,64)
    BackGroundWhite = HighContrastPink
}

// 청록(배경) 노랑(글자)
fun themeGreenYellow(){
    ButtonBlue = HighContrastYellow
    NotifyBlock = DeepHighContrastGreen
    Selected = HighContrastGreen
    TextColor = HighContrastYellow
    IconBlue = HighContrastYellow
    WishButton = HighContrastGreen
    TextWhite = HighContrastGreen
    TextLittleDark = HighContrastYellow
    LoginTextFiled = HighContrastGreen
    SearchBarColor = HighContrastGreen
    BasketPaymentColor = HighContrastGreen
    ButtonBlackColor = HighContrastYellow
    SkyBlue = HighContrastGreen
    LightPink = HighContrastGreen
    Unselected = HighContrastGreen
    DisabledText = HighContrastGreen
    PaymentCard = HighContrastGreen
    CancelColor = Color(248,17,64)
    BackGroundWhite = HighContrastGreen
}

// 차색(배경) 연하늘(글자)
fun themeRedSkyBlue(){
    ButtonBlue = HighContrastLightBlue
    NotifyBlock = DeepHighContrastRed
    Selected = HighContrastRed
    TextColor = HighContrastLightBlue
    IconBlue = HighContrastLightBlue
    WishButton = HighContrastRed
    TextWhite = HighContrastRed
    TextLittleDark = HighContrastLightBlue
    LoginTextFiled = HighContrastRed
    SearchBarColor = HighContrastRed
    BasketPaymentColor = HighContrastRed
    ButtonBlackColor = HighContrastLightBlue
    SkyBlue = HighContrastRed
    LightPink = HighContrastRed
    Unselected = HighContrastRed
    DisabledText = HighContrastRed
    PaymentCard = HighContrastRed
    CancelColor = Color(248,17,64)
    BackGroundWhite = HighContrastRed
}