package com.example.team_voida.Profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import com.example.team_voida.ui.theme.CancelColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
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
    val paymentInfo: MutableState<PaymentInfo?> = remember { mutableStateOf(PaymentInfo(
        address = "서울특별시 서대문구 독립문로 129-1 가나다 아파트세상 203동 1104호",
        phone = "010-1234-5678",
        email = "1234@gmail.com",
        item = listOf(
            BasketInfo(
                product_id = 1,
                img = "https://product-image.kurly.com/hdims/resize/%5E%3E360%20%20%20%20%20x%3E468/cropcenter/360x468/quality/85/src/product/image/25ac1ec1-005e-44a3-a00f-4224d25bcc96.jpg",
                name = "[KF365] 햇 감자 1kg",
                price = 3990F,
                number = 1
            ),
            BasketInfo(
                product_id = 1,
                img = "https://product-image.kurly.com/hdims/resize/%5E%3E360%20%20%20%20%20x%3E468/cropcenter/360x468/quality/85/src/product/image/25ac1ec1-005e-44a3-a00f-4224d25bcc96.jpg",
                name = "[KF365] 햇 감자 1kg",
                price = 3990F,
                number = 1
            ),
            BasketInfo(
                product_id = 1,
                img = "https://product-image.kurly.com/hdims/resize/%5E%3E360%20%20%20%20%20x%3E468/cropcenter/360x468/quality/85/src/product/image/25ac1ec1-005e-44a3-a00f-4224d25bcc96.jpg",
                name = "[KF365] 햇 감자 1kg",
                price = 3990F,
                number = 1
            ),BasketInfo(
                product_id = 1,
                img = "https://product-image.kurly.com/hdims/resize/%5E%3E360%20%20%20%20%20x%3E468/cropcenter/360x468/quality/85/src/product/image/25ac1ec1-005e-44a3-a00f-4224d25bcc96.jpg",
                name = "[KF365] 햇 감자 1kg",
                price = 3990F,
                number = 1
            ),
            ),
        cards = listOf(
            CardInfo(
                card_id = -1,
                company = "",
                card_code = "",
                date = "",
                card_num = 0
            )
        )
    )) }

    val payDetailHistory: MutableState<PaymentInfo?> = remember { mutableStateOf<PaymentInfo?>(null) }


    val customAlertDialogState: MutableState<CustomAlertDialogState> = remember {mutableStateOf<CustomAlertDialogState>(
        CustomAlertDialogState()
    )}
    // 출처: https://dev-inventory.com/27 [개발자가 들려주는 IT 이야기:티스토리]

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
                .background(Color.White)
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
                text = "주문번호" + orderNumber.value,
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 25.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                )
            )

            Spacer(Modifier.height(15.dp))

            PaymentAddress(
                address = "",
                editable = false
            )

            Spacer(Modifier.height(7.dp))

            PaymentContact(
                cell = "",
                email = "",
                editable = false
            )

            payDetailHistory.value?.item?.let { PaymentNum(it.size) }
            Spacer(Modifier.height(7.dp))
            PaymentRow(payDetailHistory)

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

