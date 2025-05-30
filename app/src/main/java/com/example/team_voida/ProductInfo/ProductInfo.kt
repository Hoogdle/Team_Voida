package com.example.team_voida.ProductInfo

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.team_voida.Basket.BasketInsert
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.CreateAccount.CheckEmail
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.session
import com.example.team_voida.ui.theme.ButtonBlackColor
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.IconBlue
import com.example.team_voida.ui.theme.NotifyBlock
import com.example.team_voida.ui.theme.SearchBarColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

@Composable
fun ProductInfo(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
){

    var result: MutableState<ProductInfoInfo?> = remember { mutableStateOf<ProductInfoInfo?>(null) }
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
            productFlag.value = true
            basketFlag.value = false
            homeNavFlag.value = false

            selectedIndex.value = 0
        } else if(event == Lifecycle.Event.ON_RESUME){

        }
    }


    if(result.value == null){
        runBlocking {
            Log.e("qqq",isItemWhichPart.value.toString())
            val job = GlobalScope.launch {
                when(isItemWhichPart.value){
                    0 -> result.value = ProductInfoServer(
                        url = "https://fluent-marmoset-immensely.ngrok-free.app/ProductInfo",
                        product_id = productID.value
                    )
                    1 -> result.value = ProductInfoServer(
                        url = "https://fluent-marmoset-immensely.ngrok-free.app/PopularProductInfo",
                        product_id = productID.value
                    )
                    2 -> result.value = ProductInfoServer(
                        url = "https://fluent-marmoset-immensely.ngrok-free.app/BigSaleProductInfo",
                        product_id = productID.value
                    )
                    3 -> result.value = ProductInfoServer(
                        url = "https://fluent-marmoset-immensely.ngrok-free.app/TodaySaleProductInfo",
                        product_id = productID.value
                    )
                    4 -> result.value = ProductInfoServer(
                        url = "https://fluent-marmoset-immensely.ngrok-free.app/NewProductInfo",
                        product_id = productID.value
                    )
                }
            }
        }
    }


    val scrollState = rememberScrollState()

    if(result.value != null){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)

        ){
            Notification(result.value!!.name + " 상품 정보입니다. 아래에 AI가 요약한 상품 정보와 요약된 리뷰 정보를 확인할 수 있습니다.  왼쪽 하단에 상품가격, 중앙 하단에 장바구니, 오른쪽 하단에 구매하기 버튼이 있습니다.")

            // 상품 이미지
            AsyncImage(
                modifier = Modifier.size(450.dp),
                model = if(result.value!!.image_url[0]=='\"'){
                    result.value!!.image_url.substring(1, result.value!!.image_url.length-1)} else{
                    result.value!!.image_url},
                contentDescription = "상품 이미지 입니다. 아래에서 상품 정보를 확인해주세요."
            )

            Spacer(Modifier.height(35.dp))

            Text(
                modifier = Modifier
                    .padding(
                        start = 18.dp
                    ),
                text = "상품 정보",
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_bold))
                )
            )
            Spacer(Modifier.height(5.dp))
            HorizontalDivider(
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        end = 20.dp
                    ),
                color = Color.Black
            )
            Spacer(Modifier.height(5.dp))
            Text(
                modifier = Modifier
                    .padding(
                        start = 18.dp,
                        end = 18.dp
                    ),
                text = result.value!!.ai_info,
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular))
                ),
                lineHeight = 30.sp
            )
            Spacer(Modifier.height(35.dp))
            Text(
                modifier = Modifier
                    .padding(
                        start = 18.dp
                    ),
                text = "리뷰 정보",
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_bold))
                )
            )
            Spacer(Modifier.height(5.dp))
            HorizontalDivider(
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        end = 20.dp
                    ),
                color = Color.Black
            )
            Spacer(Modifier.height(5.dp))
            Text(
                modifier = Modifier
                    .padding(
                        start = 18.dp,
                        end = 18.dp
                    ),
                text = result.value!!.ai_review,
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular))
                ),
                lineHeight = 30.sp
            )
            Spacer(Modifier.height(20.dp))


        }
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().background(Color.White)
                .semantics(mergeDescendants = true){
                    text = AnnotatedString("AI가 상품 정보를 요약하는 중입니다. 잠시만 기다려주세요.")
                }
        ){
            Loader()
            Spacer(Modifier.height(15.dp))
            Text("상품 로딩중")
        }
    }
}

@Composable
fun ProductInfoBottomBar(
    price: String,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>
){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(SearchBarColor)
            .padding(
                top = 20.dp,
                bottom = 40.dp,
                start = 10.dp,
                end =  10.dp
            )
    ){
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    top = 12.dp,
                    start = 15.dp
                ),
            text = price + " 원",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold))
            )
        )
        Button(
            shape = RoundedCornerShape(10.dp),
            colors = ButtonColors(
                contentColor = ButtonBlackColor,
                containerColor = ButtonBlackColor,
                disabledContentColor = ButtonBlackColor,
                disabledContainerColor = ButtonBlackColor
            ),
            onClick = {
                runBlocking {
                    val job = GlobalScope.launch {
                        BasketInsert(
                            action = when(isItemWhichPart.value){
                                0 -> "BasketInsert"
                                1 -> "/BasketInsert/Popular"
                                2 -> "/BasketInsert/Popular"
                                3 -> "/BasketInsert/TodaySale"
                                4 -> "/BasketInsert/New"
                                else -> ""
                            },
                            session_id = session.sessionId.value,
                            product_id = productID.value
                        )
                    }
                    Log.e("value", "product id : " +productID.value.toString())
                    Log.e("value", "iswhich id : " +isItemWhichPart.value.toString())

                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
                text = "장바구니",
                color = TextWhite,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular))
                )
            )
        }
        Spacer(Modifier.width(7.dp))
        Button(
            shape = RoundedCornerShape(10.dp),
            colors = ButtonColors(
                contentColor = ButtonBlue,
                containerColor = ButtonBlue,
                disabledContentColor = ButtonBlue,
                disabledContainerColor = ButtonBlue
            ),
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
                text = "결제하기",
                color = TextWhite,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular))
                )
            )
        }
    }
}



// 로딩화면은 오픈소스를 활용
// https://stackoverflow.com/questions/73966501/circular-loading-spinner-in-jetpack-compose
@Composable
fun Loader(
    size: Dp = 32.dp, // indicator size
    sweepAngle: Float = 90f, // angle (lenght) of indicator arc
    color: Color = IconBlue, // color of indicator arc line
    strokeWidth: Dp = 3.dp //width of cicle and ar lines
) {
    ////// animation //////

    // docs recomend use transition animation for infinite loops
    // https://developer.android.com/jetpack/compose/animation
    val transition = rememberInfiniteTransition()

    // define the changing value from 0 to 360.
    // This is the angle of the beginning of indicator arc
    // this value will change over time from 0 to 360 and repeat indefinitely.
    // it changes starting position of the indicator arc and the animation is obtained
    val currentArcStartAngle by transition.animateValue(
        0,
        360,
        Int.VectorConverter,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = LinearEasing
            )
        )
    )

    ////// draw /////

    // define stroke with given width and arc ends type considering device DPI
    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Square)
    }

    // draw on canvas
    Canvas(
        Modifier
            .progressSemantics() // (optional) for Accessibility services
            .size(size) // canvas size
            .padding(strokeWidth / 2) //padding. otherwise, not the whole circle will fit in the canvas
    ) {
        // draw "background" (gray) circle with defined stroke.
        // without explicit center and radius it fit canvas bounds
        drawCircle(Color.LightGray, style = stroke)

        // draw arc with the same stroke
        drawArc(
            color,
            // arc start angle
            // -90 shifts the start position towards the y-axis
            startAngle = currentArcStartAngle.toFloat() - 90,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = stroke
        )
    }
}