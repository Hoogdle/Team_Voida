package com.example.team_voida.Payment

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import coil3.compose.AsyncImage
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.Basket.BasketListServer
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Login.LogIntButton
import com.example.team_voida.Login.LoginForgotPW
import com.example.team_voida.Login.LoginPassWordField
import com.example.team_voida.Login.LoginTextField
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.TextLittleDark
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun PayRegister(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
    isPayOne: MutableState<Boolean>,
    isPayPage: MutableState<Boolean>,
    paymentUserInfo: MutableState<PaymentUserInfo>
){
    val scrollState = rememberScrollState()
    val orderResponse: MutableState<OrderResponse?> = remember { mutableStateOf(null) }
    val basketInfo: MutableState<List<BasketInfo>?> = remember { mutableStateOf<List<BasketInfo>?>(null) }


    val paymentInfo:MutableState<PaymentInfo?> = remember { mutableStateOf<PaymentInfo?>(null) }
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
            basketFlag.value = true
            homeNavFlag.value = true
            productFlag.value = false

            selectedIndex.value = 3
        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }


    // 서버로 부터 해당 계정의 결제정보를 요청
    runBlocking {
        val job = GlobalScope.launch {
            if(isPayOne.value){
                orderResponse.value = AddOneOrder(
                    session_id = session.sessionId.value,
                    address = paymentUserInfo.value.address,
                    email = paymentUserInfo.value.email,
                    cell = paymentUserInfo.value.cell,
                    productId = productID.value
                )
            } else {
                runBlocking {
                    val job = GlobalScope.launch {
                        basketInfo.value = BasketListServer(session_id = session.sessionId.value)
                    }
                }
                Thread.sleep(2000L)

                if(basketInfo.value != null) {
                    runBlocking {
                        val job = GlobalScope.launch {
                            orderResponse.value = AddBaksetOrder(
                                session_id = session.sessionId.value,
                                address = paymentUserInfo.value.address,
                                email = paymentUserInfo.value.email,
                                cell = paymentUserInfo.value.cell,
                                itemList = basketInfo.value!!
                            )
                        }
                    }
                }
            }
        }
    }

    if(orderResponse.value != null){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ){
            Notification("결제가 완료되었습니다. 아래에 주문 정보를 확인해주세요.")
            Spacer(Modifier.height(25.dp))

            Image(
                painter = painterResource(R.drawable.pay_success),
                contentDescription = ""
            )
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = "주문번호 #" + orderResponse.value!!.order_num.toString(),
                style = TextStyle(
                    fontSize = 35.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_bold)),
                )
            )
            Spacer(Modifier.height(25.dp))

            PayFinishRow()


        }
    } else {
        LoaderSet(info = "결제 중",semantics = "결제 중")
    }

}

// 결제 정보 목록 컴포저블
// 시간이 없어 모듈화는 생략함
@Composable
fun PayFinishRow(
    paymentInfo: MutableState<PaymentInfo?>
){



    //
    paymentInfo.value?.item?.forEachIndexed { index, item ->
        Column {
            Row(
                modifier = Modifier
                    .semantics(mergeDescendants = true){
                        text = AnnotatedString("${item.name} 상품이 총 ${item.number} 개 담겨 있습니다. 상품 가격은 ${item.price.toInt()}원 입니다.")
                    }
                    .padding(
                        start = 10.dp,
                        end = 10.dp
                    )
                ,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Box(){
                    AsyncImage(
                        model = if(item.img[0]=='\"'){item.img.substring(1,item.img.length-1)} else{item.img},
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(86.dp)
                            .shadow(
                                elevation = 5.dp,
                                shape = CircleShape
                            )
                            .clip(CircleShape)

                            .border(
                                width = 5.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                    )

                    Box(
                        modifier = Modifier
                            .offset(
                                x = 60.dp,
                                y = 11.dp
                            )
                    ){
                        Text(
                            modifier = Modifier
                                .clip(shape = CircleShape)
                                .background(
                                    color = Selected
                                )
                                .width(30.dp)
                                .height(30.dp)
                                .border(
                                    width = 3.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .offset(
                                    y = 5.dp
                                )
                            ,
                            textAlign = TextAlign.Center,
                            text = item.number.toString(),
                            color = TextLittleDark,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                            )
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .padding(
                            start = 5.dp
                        )
                        .padding(13.dp)
                        .weight(7f)
                        .padding(top = 10.dp)
                    ,
                    text = item.name,
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )

                val textPrice = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(item.price)

                Text(
                    modifier = Modifier
                        .padding(
                            start = 5.dp
                        )
                        .padding(13.dp)
                        .weight(4f)
                        .padding(top = 17.dp)

                    ,
                    text = textPrice + "원",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
            }
            Spacer(Modifier.height(5.dp))
        }
    }
}