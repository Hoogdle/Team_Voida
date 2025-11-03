package com.example.team_voida.Profile

import android.util.Log
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


data class PaymentItemHistory(
    val year: String,
    val month: String,
    val date: String,
    val time: String,
    val orderNum: String,
    val price: String,
    val flag: Int // buy or refund
)
val tmpPaymentItemHistory = listOf(
    PaymentItemHistory(
        year = "2025",
        month = "04",
        date = "25",
        time = "12:25",
        orderNum = "#92287157",
        price = "27000",
        flag = 1
    ),
    PaymentItemHistory(
        year = "2025",
        month = "04",
        date = "28",
        time = "15:15",
        orderNum = "#92287192",
        price = "8750",
        flag = 1
    ),PaymentItemHistory(
        year = "2025",
        month = "06",
        date = "13",
        time = "10:25",
        orderNum = "#92287245",
        price = "12500",
        flag = 1
    ),PaymentItemHistory(
        year = "2025",
        month = "06",
        date = "25",
        time = "15:25",
        orderNum = "#92287300",
        price = "12500",
        flag = 2
    ),PaymentItemHistory(
        year = "2025",
        month = "08",
        date = "03",
        time = "16:25",
        orderNum = "#92287400",
        price = "18000",
        flag = 1
    ),PaymentItemHistory(
        year = "2025",
        month = "09",
        date = "01",
        time = "01:25",
        orderNum = "#92287423",
        price = "7250",
        flag = 1
    ),

)

@Composable
fun PaymentHistory(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    orderNumber: MutableState<String>
) {

    val scrollState = rememberScrollState()
    val isAdding = remember { mutableStateOf(false) }


    // 유저 정보 페이지에 해당하는 하단 네비 Flag Bit 활성화
    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            Log.e("123", "on_pause")
        } else if (event == Lifecycle.Event.ON_STOP) {
            Log.e("123", "on_stop")
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            Log.e("123", "on_destroy")
        } else if (event == Lifecycle.Event.ON_CREATE) {
            Log.e("123", "on_create")
        } else if (event == Lifecycle.Event.ON_START) {
            Log.e("123", "on_start")
            basketFlag.value = false
            homeNavFlag.value = true
            productFlag.value = false

            selectedIndex.value = 4
        } else if (event == Lifecycle.Event.ON_RESUME) {
            Log.e("123", "on_resume")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)

    ) {
        Notification("현재 등록된 카드에서 결제한 기록을 확인할 수 있습니다. 아래에 현재 등록된 카드와 결제 내역을 확인하세요.")

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
            text = "결제내역",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
            )
        )

        Spacer(Modifier.height(15.dp))

        PaymentCard(
            cardID = 1,
            company = "ibk",
            paymentNumber = "1111222233334444",
            name = "Travis",
            expiredMonth = "12",
            expiredDate = "10"
        )

        Spacer(Modifier.height(20.dp))

        tmpPaymentItemHistory.forEach {
            PaymentHistoryItem(
                year = it.year,
                month = it.month,
                date = it.date,
                time = it.time,
                orderNum = it.orderNum,
                price = it.price,
                flag = it.flag,
                orderNumberSetter = orderNumber,
                navController = navController
            )
            Spacer(Modifier.height(10.dp))
        }

    }
}

@Composable
fun PaymentHistoryItem(
    year: String,
    month: String,
    date: String,
    time: String,
    orderNum: String,
    price: String,
    flag: Int,// 0 -> Buy, 1 -> Refund
    orderNumberSetter: MutableState<String>,
    navController: NavController
){
    val floatPrice = price.toFloat()
    val textPrice = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(floatPrice)

    Box(
        modifier = Modifier
            .padding(
                horizontal = 15.dp
            )
            .clip(
                shape = RoundedCornerShape(15.dp)
            )
            .background(
                color = Selected
            )
            .height(
                80.dp
            )
            .clickable {
                if (flag ==1){
                orderNumberSetter.value = orderNum
                navController.navigate("paymentHistoryList")
                    }
            }

    ){
        Row(
            modifier = Modifier.fillMaxWidth()
        ){
            Image(
                painter = painterResource(
                    if(flag==1) R.drawable.buy
                    else R.drawable.refund
                ),
                contentDescription = "",
                modifier = Modifier
                    .padding(25.dp)
                    .size(25.dp)
            )

            Column(
            ){
                Text(
                    modifier = Modifier
                        .padding(
                            top = 21.dp
                        ),
                    text = year + "년 " + month + "월 " + date + "일 " + time,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontSize = 11.sp
                    )
                )
                Text(
                    modifier = Modifier
                        .padding(
                            bottom = 15.dp
                        ),
                    text = orderNum,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_semibold)),
                        fontSize = 20.sp
                    )
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                modifier = Modifier
                    .padding(
                        25.dp,
                    ),
                text = textPrice + "원",
                style = TextStyle(
                    color = TextColor,
                    fontFamily = FontFamily(Font(R.font.roboto_bold)),
                    fontSize = 20.sp
                )
            )
        }
    }
}