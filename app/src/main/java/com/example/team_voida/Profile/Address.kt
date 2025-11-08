package com.example.team_voida.Profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.Payment.PaymentAddress
import com.example.team_voida.ProfileServer.Address
import com.example.team_voida.ProfileServer.AddressList
import com.example.team_voida.ProfileServer.CardAdd
import com.example.team_voida.ProfileServer.CardInfo
import com.example.team_voida.ProfileServer.PayHistoryList
import com.example.team_voida.ProfileServer.PayHistoryListServer
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.IconBlue
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import com.example.team_voida.ui.theme.WishButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun Address(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
){

    val scrollState = rememberScrollState()

    val addressList: MutableState<List<Address>?> = remember { mutableStateOf<List<Address>?>(null) }
    val whichAddress: MutableState<Int> = remember { mutableStateOf(-1) }

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

        } else if (event == Lifecycle.Event.ON_RESUME) {
            Log.e("123", "on_resume")
        }
    }

    // 서버에 장바구니 정보 요청
    if(addressList.value == null){
        runBlocking {
            val job = GlobalScope.launch{
                addressList.value = AddressList(session.sessionId.value)
            }
        }
    }

    if(addressList.value != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)

        ) {
            Notification("배송지 설정 화면입니다. 아래에 배송지를 수정하거나 대표 배송지를 설정할 수 있습니다. 배송지 추가를 원하시는 경우 '배송지 추가' 버튼을 눌러주세요.")

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

            addressList.value!!.forEach {
                PaymentAddress(
                    addressId = it.address_id,
                    address = it.address_text,
                    editable = true,
                    whichAddress = whichAddress
                )
            }

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
                onClick = {

                }
            ) {
                Text(
                    modifier = Modifier
                        .padding(
                        ),
                    textAlign = TextAlign.Center,
                    text = "배송지 추가",
                    color = TextWhite,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
            }
        }
    } else {
        LoaderSet(info = "배송지 로딩중", semantics = "배송지 로딩중")
    }
}


// 배송지 주소 컴포저블
@Composable
fun SettingAddress(
    addressId: Int,
    address: String,
    editable: Boolean,
    whichAddress: MutableState<Int>
){
    Column(
        modifier = Modifier
            .semantics(mergeDescendants = true){
                text = AnnotatedString("배송지 주소는 서울특별시 서대문구 독립문로 129-1 가나다 아파트세상 203동 1104호 입니다. 배송지를 수정하시려면 다음에 나오는 배송지 수정 버튼을 눌러주세요.")
            }
            .fillMaxWidth()
            .height(120.dp)
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .clip(RoundedCornerShape(7.dp))
            .border(
                width = if(whichAddress.value == addressId){
                    2.dp
                } else {
                    0.dp
                },
                color = IconBlue,
                shape = RoundedCornerShape(7.dp)
            )
            .background(
                color = WishButton
            )
    ){
        Text(
            modifier = Modifier
                .padding(
                    start = 5.dp
                )
                .padding(
                    start = 13.dp,
                    top = 13.dp,
                    end = 13.dp
                )
            ,
            textAlign = TextAlign.Center,
            text = "배송 주소",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                modifier = Modifier
                    .padding(
                        start = 5.dp
                    )
                    .padding(13.dp)
                    .fillMaxWidth()
                    .weight(8f)
                ,
                text = address,
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                )
            )
            if(editable){
                Button(
                    onClick = {},
                    modifier = Modifier
                        .size(30.dp)
                        .width(1.dp)
                        .offset(
                            x = -10.dp,
                            y = 20.dp
                        )
                    ,
                    colors = ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.payment_edit),
                        contentDescription = "배송지 수정 버튼",
                        modifier = Modifier

                    )
                }
            }
        }
    }
}