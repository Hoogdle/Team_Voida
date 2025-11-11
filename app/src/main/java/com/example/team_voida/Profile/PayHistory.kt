package com.example.team_voida.Profile

import android.util.Log
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.Basket.BasketListServer
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.ProfileServer.CardInfo
import com.example.team_voida.ProfileServer.PayHistory
import com.example.team_voida.ProfileServer.PayHistoryList
import com.example.team_voida.ProfileServer.PayHistoryListServer
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import com.example.team_voida.ui.theme.BackGroundWhite
import com.example.team_voida.ui.theme.IconBlue
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.Unselected
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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


    val rememberPage = remember { mutableStateOf(false) }

    val view = LocalView.current

    if(rememberPage.value == false){
        view.announceForAccessibility("결제 내역 화면입니다. 화면 최상단에서 안내메세지를 제공받으세요.")
        rememberPage.value = true
    }
    
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

    val selectedCardId: MutableState<Int> = remember { mutableStateOf(-1)}

    val payHistory: MutableState<List<PayHistoryList>?> = remember { mutableStateOf<List<PayHistoryList>?>(null) }
    val cardInfo: MutableState<List<CardInfo>?> = remember { mutableStateOf<List<CardInfo>?>(null) }


    // 서버에 장바구니 정보 요청
    if(payHistory.value == null){
        runBlocking {
            val job = GlobalScope.launch{
                payHistory.value = PayHistoryListServer(session.sessionId.value)
            }
        }
    }

    if(payHistory.value != null) {
        if(selectedCardId.value == -1) {
            selectedCardId.value = payHistory.value!![0].card_id
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackGroundWhite)
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

            if(payHistory.value!![0].card_id != -1) {

                HistoryCardList(
                    selectedCardId = selectedCardId,
                    cardList = payHistory.value!!
                )

                Spacer(Modifier.height(20.dp))

                payHistory.value!!.forEach {
                    if (it.card_id == selectedCardId.value) {
                        it.pay_list.forEach {
                            PaymentHistoryItem(
                                date = it.date,
                                orderNum = it.order_num,
                                price = it.price.toString(),
                                isRefund = it.is_refund,
                                orderNumberSetter = orderNumber,
                                navController = navController
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    } else {
        LoaderSet(info = "결제내역을 불러오는 중입니다.", semantics = "결제내역을 불러오는 중입니다.")
    }
}

fun monthToNumber(month: String): Int {
    return when (month) {
        "JANUARY", "JAN" -> 1
        "FEBRUARY", "FEB" -> 2
        "MARCH", "MAR" -> 3
        "APRIL", "APR" -> 4
        "MAY" -> 5
        "JUNE", "JUN" -> 6
        "JULY", "JUL" -> 7
        "AUGUST", "AUG" -> 8
        "SEPTEMBER", "SEP" -> 9
        "OCTOBER", "OCT" -> 10
        "NOVEMBER", "NOV" -> 11
        "DECEMBER", "DEC" -> 12
        else -> throw IllegalArgumentException("Invalid month: $month")
    }
}

@Composable
fun PaymentHistoryItem(
    date: String,
    orderNum: String,
    price: String,
    isRefund: Boolean,// 0 -> Buy, 1 -> Refund
    orderNumberSetter: MutableState<String>,
    navController: NavController
){
    val floatPrice = price.toFloat()
    val textPrice = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(floatPrice)

    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val localDateTime = LocalDateTime.parse(date, formatter)

    val year = localDateTime.year
    val month = monthToNumber(localDateTime.month.toString())
    val day = localDateTime.dayOfMonth

    val time = localDateTime.hour.toString() + ":" + localDateTime.minute.toString() + ":" + localDateTime.second.toString()

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
                if (!isRefund){
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
                    if(!isRefund) R.drawable.buy
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
                    text = year.toString() + "년 " + month.toString() + "월 " + day.toString() + "일 " + time,
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
                    text = "#" + orderNum.padStart(7,'0'),
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



@Composable
fun HistoryCardList(
    selectedCardId: MutableState<Int>,
    cardList: List<PayHistoryList>
){
    val scrollState = rememberScrollState()


    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(
                start = 20.dp
            )
    ){
        cardList.forEachIndexed { index, item ->

            HistoryPaymentCard(
                cardID = item.card_id,
                company = item.card_company,
                paymentNumber = item.card_code,
                name = "",
                expiredMonth = item.card_date.substring(0,2),
                expiredDate = item.card_date.substring(2,4),
                selectedCardId = selectedCardId
            )
            Spacer(Modifier.width(2.dp))
        }
    }
}
@Composable
fun HistoryPaymentCard(
    cardID: Int,
    company: String,
    paymentNumber: String,
    name: String,
    expiredMonth: String,
    expiredDate: String,
    selectedCardId: MutableState<Int>
){
    val logo = PaymentLogoSelector(company)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(
                horizontal = 3.dp
            )
            .border(
                width = if(selectedCardId.value == cardID){
                    2.dp
                } else {
                    0.dp
                },
                color = IconBlue,
                shape = RoundedCornerShape(15.dp)
            )
            .clip(
                shape = RoundedCornerShape(15.dp)
            )
            .background(color= com.example.team_voida.ui.theme.PaymentCard)
            .clickable {
                selectedCardId.value = cardID
            }

    ){
        Column (
            modifier = Modifier.fillMaxWidth()
        ){

            // Logo and Setting
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Image(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(
                            horizontal = 20.dp,
                            vertical = 20.dp
                        )
                    ,
                    painter = painterResource(logo),
                    contentDescription = ""
                )
                Button(
                    onClick = {
                    },
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

                }
            }

            Spacer(Modifier.height(30.dp))

            // Card Number
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Spacer(Modifier.width(35.dp))
                val lastNumber = paymentNumber.substring(12,16)

                for(i in 1..3){
                    Text(
                        text = "****  ",
                        style = TextStyle(
                            color = TextColor,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
                Text(
                    text = lastNumber,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(Modifier.width(10.dp))
            }

            Spacer(Modifier.height(15.dp))

            // Name and Expired
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ){
                Spacer(Modifier.width(130.dp))

                Spacer(Modifier.weight(1f))

                Text(
                    text = expiredMonth + "/" + expiredDate,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}