package com.example.team_voida.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
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
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.SkyBlue
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import kotlin.math.log

@Composable
fun PaymentSetting(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>
){

    val scrollState = rememberScrollState()

    // 유저 정보 페이지에 해당하는 하단 네비 Flag Bit 활성화
    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            Log.e("123","on_pause")
        } else if(event == Lifecycle.Event.ON_STOP){
            Log.e("123","on_stop")
        } else if(event == Lifecycle.Event.ON_DESTROY){
            Log.e("123","on_destroy")
        } else if(event == Lifecycle.Event.ON_CREATE){
            Log.e("123","on_create")
        } else if(event == Lifecycle.Event.ON_START){
            Log.e("123","on_start")
            basketFlag.value = false
            homeNavFlag.value = true
            productFlag.value = false

            selectedIndex.value = 4
        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)

    ) {
        Notification("결제수단 설정 화면입니다. 현재 결제수단을 확인하시고, 다른 결제수단으로 변경하시거나 새로운 결제수단을 등록하세요.")

        Spacer(Modifier.height(10.dp))
        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = 23.dp
                ),
            textAlign = TextAlign.Center,
            text = "Settings",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 25.sp,
                fontFamily = FontFamily(Font(R.font.roboto_bold)),
            )
        )

        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = 10.dp
                ),
            textAlign = TextAlign.Center,
            text = "결제수단 변경",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
            )
        )

        Spacer(Modifier.height(15.dp))

        PaymentAdd()

        Spacer(Modifier.height(15.dp))

        PaymentCard(
            company = "ibk",
            paymentNumber = "1111222233334444",
            name = "Travis",
            expiredMonth = "12",
            expiredDate = "10"
        )

    }
}

@Composable
fun PaymentAdd(){
    Button(
        shape = RoundedCornerShape(10.dp),
        colors = ButtonColors(
            contentColor = ButtonBlue,
            containerColor = ButtonBlue,
            disabledContentColor = ButtonBlue,
            disabledContainerColor = ButtonBlue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(10.dp)
        ,
        onClick = {}
    ) {
        Text(
            modifier = Modifier
                .padding(

                ),
            textAlign = TextAlign.Center,
            text = "결제수단 추가",
            color = TextWhite,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular)),
            )
        )
    }
}

@Composable
fun PaymentCard(
    company: String,
    paymentNumber: String,
    name: String,
    expiredMonth: String,
    expiredDate: String
){
    val logo = PaymentLogoSelector(company)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(
                horizontal = 15.dp
            )
            .clip(
                shape = RoundedCornerShape(15.dp)
            )
            .background(color= com.example.team_voida.ui.theme.PaymentCard)

    ){
        Column {

            // Logo and Setting
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Image(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(
                            horizontal = 20.dp,
                            vertical = 20.dp
                        )
                    ,
                    painter = painterResource(logo),
                    contentDescription = ""
                )
                Button(
                    onClick = {},
                    modifier = Modifier
                        .padding(
                            all = 10.dp
                        )
                        .size(50.dp)
                        .padding(
                            all = 5.dp
                        )
                    ,
                    colors = ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ){
                    Image(
                        painter = painterResource(R.drawable.cogwheel),
                        contentDescription = "프로필 이미지 수정 버튼",
                        modifier = Modifier
                            .clip(shape = CircleShape)
                            .background(
                                color = Selected
                            )
                            .padding(all = 10.dp)
                    )
                }

            }

            // Card Number
            Row {

            }

            // Name and Expired
            Row {

            }
        }
    }
}

fun PaymentLogoSelector(
    company: String
):Int{
    var result = 0
    when(company){
        "bnk" -> result = R.drawable.bnk
        "city" -> result = R.drawable.city
        "dgb" -> result = R.drawable.dgb
        "gj" -> result = R.drawable.gj
        "hana" -> result = R.drawable.hana
        "ibk" -> result = R.drawable.ibk
        "kb" -> result = R.drawable.kb
        "mg" -> result = R.drawable.mg
        "nh" -> result = R.drawable.nh
        "sc" -> result = R.drawable.sc
        "sh" -> result = R.drawable.sh
        "shinhup" -> result = R.drawable.shinhup
        "sinhan" -> result = R.drawable.sinhan
        "woorie" -> result = R.drawable.woorie
    }
    return result
}