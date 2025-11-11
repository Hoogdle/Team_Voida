package com.example.team_voida.Profile

import android.util.Log
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
import androidx.compose.ui.graphics.RectangleShape
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
import com.example.team_voida.Basket.Basket
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.Payment.PayDetailHistoryRow
import com.example.team_voida.Payment.PaymentAddress
import com.example.team_voida.Payment.PaymentContact
import com.example.team_voida.Payment.PaymentInfo
import com.example.team_voida.Payment.PaymentNum
import com.example.team_voida.Payment.PaymentRow
import com.example.team_voida.ProfileServer.CancelOrder
import com.example.team_voida.ProfileServer.CardInfo
import com.example.team_voida.ProfileServer.PayDetailHistory
import com.example.team_voida.ProfileServer.PayDetailHistoryListServer
import com.example.team_voida.ProfileServer.PayDetailItem
import com.example.team_voida.ProfileServer.PayHistory
import com.example.team_voida.ProfileServer.PayHistoryList
import com.example.team_voida.ProfileServer.PayHistoryListServer
import com.example.team_voida.ProfileServer.PaymentDetailInfo
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import com.example.team_voida.ui.theme.BackGroundWhite
import com.example.team_voida.ui.theme.CancelColor
import com.example.team_voida.ui.theme.IconBlue
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import com.example.team_voida.ui.theme.WishButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@Composable
fun PaymentHistoryList(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    orderNumber: MutableState<String>
){
    val scrollState = rememberScrollState()
    
    val rememberPage = remember { mutableStateOf(false) }

    val view = LocalView.current

    if(rememberPage.value == false){
        view.announceForAccessibility("결제 세부정보 화면입니다. 화면 최상단에서 안내메세지를 제공받으세요.")
        rememberPage.value = true
    }

    val payDetailHistory: MutableState<PaymentDetailInfo?> = remember { mutableStateOf<PaymentDetailInfo?>(null) }


    val customAlertDialogState: MutableState<CustomAlertDialogState> = remember {mutableStateOf<CustomAlertDialogState>(
        CustomAlertDialogState()
    )}

    fun resetDialogState() {
        customAlertDialogState.value = CustomAlertDialogState()
    }

    fun showCustomAlertDialog() {
        customAlertDialogState.value = CustomAlertDialogState(
            title = "정말로 주문을 취소 하시겠습니까?",
            description = "",
            onClickConfirm = {
                resetDialogState()
            },
            onClickCancel = {
                resetDialogState()
            }
        )
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

    // 서버에 장바구니 정보 요청
    if(payDetailHistory.value == null){
        runBlocking {
            val job = GlobalScope.launch{
                payDetailHistory.value = PayDetailHistoryListServer(session.sessionId.value, orderNumber.value)
            }
        }
    }

    if(payDetailHistory.value != null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackGroundWhite)
                .verticalScroll(scrollState)

        ) {
            Notification("주문번호" + orderNumber.value + " 상품 정보입니다. 아래에 주문하신 상품을 확인하시고, 주문 취소를 원하시는 경우 화면 하단에 '주문취소' 버튼을 눌러주세요.")

            Spacer(Modifier.height(10.dp))

            Text(
                modifier = Modifier
                    .padding(
                        start = 22.dp
                    )
                    .semantics(mergeDescendants = true) {
                        text = AnnotatedString("아래에 주문하신 상품 목록을 확인해주세요.")
                    },
                textAlign = TextAlign.Center,
                text = "주문번호 " + "#" + orderNumber.value.padStart(7,'0'),
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 25.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                )
            )

            Spacer(Modifier.height(15.dp))

            PaymentDetailPageAddress(
                address = payDetailHistory.value!!.address,
                editable = false
            )

            Spacer(Modifier.height(7.dp))

            PaymentContact(
                cell = payDetailHistory.value!!.cell,
                email = payDetailHistory.value!!.email,
                editable = false
            )

            payDetailHistory.value?.items?.let { PaymentNum(it.size) }
            Spacer(Modifier.height(7.dp))
            PayDetailHistoryRow(payDetailHistory)

            Button(
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 10.dp
                    )
                    .height(50.dp)
                    .clip(shape = RoundedCornerShape(15.dp)),
                onClick = {
                    Log.e("Button", "1")
                    showCustomAlertDialog()
                    Log.e("Button", "2")
                },
                colors = ButtonColors(
                    containerColor = CancelColor,
                    contentColor = TextWhite,
                    disabledContentColor = TextWhite,
                    disabledContainerColor = CancelColor
                )
            ) {
                Text(
                    text = "주문취소",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular))
                    )
                )
            }

        }
    } else {
        LoaderSet(info = "결제 상품 정보를 불러오는 중입니다.", semantics = "결제 상품 정보를 불러오는 중입니다.")

    }
    if (customAlertDialogState.value.title.isNotBlank()) {
        CustomAlertDialog(
            title = customAlertDialogState.value.title,
            description = customAlertDialogState.value.description,
            onClickCancel = { customAlertDialogState.value.onClickCancel() },
            onClickConfirm = {
                runBlocking {
                    var result = false
                    val job = GlobalScope.launch{
                        result = CancelOrder(session_id = session.sessionId.value, order_num = orderNumber.value)
                    }
                }
                customAlertDialogState.value.onClickConfirm()
             },
            leftText = "뒤로가기",
            rightText = "주문취소"
        )
    }
}


// 배송지 주소 컴포저블
@Composable
fun PaymentDetailPageAddress(
    address: String,
    editable: Boolean,
){
    Column(
        modifier = Modifier
            .semantics(mergeDescendants = true){
                text = AnnotatedString("배송지 주소는 서울특별시 서대문구 독립문로 129-1 가나다 아파트세상 203동 1104호 입니다. 배송지를 수정하시려면 다음에 나오는 배송지 수정 버튼을 눌러주세요.")
            }
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .clip(RoundedCornerShape(7.dp))
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


