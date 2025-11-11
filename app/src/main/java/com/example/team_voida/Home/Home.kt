package com.example.team_voida.Home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.util.DebugLogger

import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.SearchBar
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.themeInStart
import com.example.team_voida.ui.theme.BackGroundWhite
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// 홈 메인 컴포저블
@Composable
fun Home(
    navController: NavController,
    input: MutableState<String>,
    result: List<Popular>?,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    isWhichPart: MutableState<Int>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
    barPrice: MutableState<Float>
){
    val view = LocalView.current
    view.announceForAccessibility("홈 화면입니다. 화면 최상단에서 안내메세지를 제공받으세요.")

    val scrollState = rememberScrollState()
    Log.e("zxz",result.toString())
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
            selectedIndex.value = 0
            homeNavFlag.value = true
            basketFlag.value = false
            productFlag.value = false
            Log.e("123","on_start")
        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }

    // TODO
    // 임시로 result를 공통으로 사용

    if(result != null){
        Log.e("home","get item")
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(BackGroundWhite)
                .verticalScroll(scrollState)

        ){
            Notification("홈 화면입니다. 실시간 인기 상품과 특가 상품을 만나볼 수 있습니다. 화면을 아래로 스크롤하여 다양한 이벤트 상품을 만나보세요!")
            HomeSearchBar(
                navController,
                input
            )
            HomePopularRanking(navController,isWhichPart,1,isItemWhichPart)
            if (result != null) {
                HomeProducts(
                    result = result.filter {
                        it.sector == 1
                    },
                    productID = productID,
                    navController = navController,
                    isItemWhichPart = isItemWhichPart,
                    indexRow = 1,
                    barPrice = barPrice
                )
            }

            Notification("아래에 요즘 많이 담기는 특가 상품을 만나보세요!")
            HomeBar(
                navController = navController,
                title = "많이 담는 특가",
                isWhichPart,
                isItemWhichPart,
                2
            )
            if (result != null) {
                HomeProducts(
                    result = result.filter {
                        it.sector == 2
                    },
                    productID = productID,
                    navController = navController,
                    isItemWhichPart = isItemWhichPart,
                    indexRow = 2,
                    barPrice = barPrice
                )
            }

            Notification("아래에 오늘 하루만 진행하는 특가 이벤트 상품을 만나보세요!")
            HomeBar(
                navController = navController,
                title = "하루 특가",
                isWhichPart,
                isItemWhichPart,
                3
            )
            if (result != null) {
                HomeProducts(
                    result = result.filter {
                        it.sector == 3
                    },
                    productID = productID,
                    navController = navController,
                    isItemWhichPart = isItemWhichPart,
                    indexRow = 3,
                    barPrice = barPrice
                )
            }

            Notification("아래에 요즘 뜨고 있는 인기 신상품을 만나보세요!")
            HomeBar(
                navController = navController,
                title = "인기 신상품",
                isWhichPart,
                isItemWhichPart,
                4
            )
            if (result != null) {
                HomeProducts(
                    result = result.filter {
                        it.sector == 4
                    },
                    productID = productID,
                    navController = navController,
                    isItemWhichPart = isItemWhichPart,
                    indexRow = 4,
                    barPrice = barPrice
                )
            }
            Spacer(Modifier.height(45.dp))
        }
    } else{
        LoaderSet(semantics = "홈 화면의 상품 정보를 불러오는 중입니다. 잠시만 기다려주세요.")
    }

}

// 검색바 컴포저블
@Composable
fun HomeSearchBar(
    navController: NavController,
    input: MutableState<String>
){
    Row(
        modifier = Modifier
            .clearAndSetSemantics {
                contentDescription = "우측에 음성 검색 버튼이 있습니다."
            }

    ){
        Text(
            modifier = Modifier
                .offset(
                    x = 15.dp,
                    y = 5.dp
                ),
            textAlign = TextAlign.Center,
            text = "Shop",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 35.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
        Spacer(Modifier.width(7.dp))
        SearchBar(
            navController = navController,
            resultInput = input
        )
    }
}

// 실시간 인기 상품 리스트 컴포저블
@Composable
fun HomePopularRanking(
    navController: NavController,
    isWhichPart: MutableState<Int>,
    index: Int,
    isItemWhichPart: MutableState<Int>,

){
    Column {
        HomeBar(
            navController = navController,
            title = "실시간 인기",
            isWhichPart = isWhichPart,
            isItemWhichPart = isItemWhichPart,
            index = index
        )
    }
}


// Home Bar which represented repeatedly
@Composable
fun HomeBar(
    navController: NavController,
    title: String,
    isWhichPart: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
    index: Int
){
    Row (
        modifier = Modifier
            .clearAndSetSemantics {
                contentDescription = "우측에 모두 보기를 통해 ${title} 상품들을 만나보세요."
            }

            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            modifier = Modifier,
//                    .offset(
//                        x = 15.dp,
//                        y = 5.dp
//                    ),
            textAlign = TextAlign.Center,
            text = title,
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 25.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
        Row (
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .clickable {
                    navController.navigate("home")
                }
        ){
            Text(
                modifier = Modifier
                    .offset(
                        x = -10.dp,
                        y = 7.dp
                    ),

                textAlign = TextAlign.Center,
                text = "모두 보기",
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                )
            )
            Icon(
                painter = painterResource(R.drawable.arrow_button),
                contentDescription = "모두 보기 버튼",
                tint = Color.Unspecified,
                modifier = Modifier
                    .clickable {
                        isWhichPart.value = index
                        isItemWhichPart.value = index
                        navController.navigate("partList")
                    }
            )
        }
    }
}


// 상품을 각 2개씩 뽑아 출력하는 정렬 컴포저블
@Composable
fun HomeProducts(
    result: List<Popular>? = null,
    productID: MutableState<Int>,
    navController: NavController,
    isItemWhichPart: MutableState<Int>,
    indexRow: Int,
    barPrice: MutableState<Float>
){

    var count: Int? = null
    val index = remember { mutableStateOf(2) }


    //Todo, control the time optimally...
    // 분명 sleep with time 이 아닌
    // wait로 처리하는 로직이 있을 거 같음.


    if(result != null){
        count = result!!.size
    }

    Column {
        for(i in 1..index.value){
            val realIndex = i-1
            Row (
                modifier = Modifier
                    .padding(10.dp)
            ){
                if(count != null && index.value <= count){

                    if(realIndex*2 <= count-1 && realIndex*2+1 <= count-1){
                        val tmpResult1 = result!![realIndex*2]
                        val tmpResult2 = result!![realIndex*2+1]

                        HomeCard(
                            id = tmpResult1.id,
                            img = tmpResult1.image_url,
                            name = tmpResult1.name,
                            price = tmpResult1.price,
                            description = tmpResult1.description,
                            category = tmpResult1.category,
                            productID = productID,
                            navController = navController,
                            isItemWhichPart = isItemWhichPart,
                            indexRow = indexRow,
                            barPrice = barPrice
                        )
                        HomeCard(
                            id = tmpResult2.id,
                            img = tmpResult2.image_url,
                            name = tmpResult2.name,
                            price = tmpResult2.price,
                            description = tmpResult2.description,
                            category = tmpResult2.category,
                            productID = productID,
                            navController = navController,
                            isItemWhichPart = isItemWhichPart,
                            indexRow = indexRow,
                            barPrice = barPrice
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row (
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    // index 값을 증가시켜, 해당 값에 따라 추가 아이템들이 나타나도록 구현
                    if(count != null && index.value*2 < count){
                        index.value += 1
                    }
                }
        ){
            Image(
                modifier = Modifier
                    .size(10.dp)
                    .offset(
                        y = 3.dp
                    ),
                painter = painterResource(R.drawable.arrow_down),
                contentDescription = ""
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = "상품 더보기",
                color = TextColor,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                )
            )
        }
    }
}

// 각 상품 아이템 카드 컴포저블
@Composable
fun HomeCard(
    id: Int,
    img: String,
    description: String,
    name: String,
    price: Float,
    category: String,
    productID: MutableState<Int>,
    navController: NavController,
    indexRow: Int,
    isItemWhichPart: MutableState<Int>,
    barPrice: MutableState<Float>
){
    val imageLoader = LocalContext.current.imageLoader.newBuilder()
        .logger(DebugLogger())
        .build()


    Box(
        modifier = Modifier
            .clearAndSetSemantics {
                contentDescription = name + "상품 입니다." + "상품의 가격은" + price.toInt() + "원 입니다."
            }

            .width(180.dp)
            .padding(
                start = 10.dp,
                end = 10.dp,
            )
            .clickable {
                barPrice.value = price
                isItemWhichPart.value = indexRow
                productID.value = id
                Log.e("qqq","in click ${isItemWhichPart.value}")
                navController.navigate("productInfo")
            }
    ){
        Image(
            painter = painterResource(
                if(themeInStart.themeId.value == 0){
                    R.drawable.home_rec

                } else if(themeInStart.themeId.value == 1) {
                    R.drawable.home_rec_pink
                } else if(themeInStart.themeId.value == 2) {
                    R.drawable.home_rec_green
                } else if(themeInStart.themeId.value == 3){
                    R.drawable.home_rec_red
                }
                else {
                    R.drawable.home_rec
                }
            ),
            contentDescription = "",
            modifier = Modifier.shadow(elevation = 15.dp, shape = RoundedCornerShape(15.dp))
        )
        Column (
            modifier = Modifier.fillMaxSize()
        ){

            Log.e("zzz",img)


            AsyncImage(
                imageLoader = imageLoader,
                modifier = Modifier
                    .offset(
                        y = 10.dp
                    )
                    .size(170.dp)
                    .clip(RoundedCornerShape(15.dp))
                ,
                model = ImageRequest
                    .Builder(LocalContext.current)
                    .data(
                        if(img[0]=='\"'){img.substring(1,img.length-1)} else{img}
                    )
                    .build(),
                contentDescription = name + "상품 이미지"
            )
            Log.e("after    ","${name} : ${img}")

            Column (
                modifier = Modifier.offset(
                    x = 12.dp,
                    y = 14.dp
                )
            ){
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(140.dp),
                    text = name,
                    color = TextColor,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
    
                val textPrice = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(price)
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = textPrice + "원",
                    color = TextColor,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
            }
        }
    }
}

