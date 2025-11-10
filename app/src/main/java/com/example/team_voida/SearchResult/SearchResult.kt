package com.example.team_voida.SearchResult

import android.app.appsearch.SearchResult
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.util.DebugLogger
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Home.HomeSearchBar
import com.example.team_voida.Home.Popular
import com.example.team_voida.Nav.navItemList
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.theme
import com.example.team_voida.ui.theme.BackGroundWhite
import com.example.team_voida.ui.theme.TextColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


// 검색 결과 메인 컴포저블
@Composable
fun SearchResult(
    navController: NavController,
    input: MutableState<String>,
    productName: String,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
    barPrice: MutableState<Float>
){

    isItemWhichPart.value = 0

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
            homeNavFlag.value =true
            basketFlag.value = false
            productFlag.value = false
            selectedIndex.value = 0

        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }

    val searchResultItem: MutableState<List<SearchResultItem>?> = remember { mutableStateOf<List<SearchResultItem>?>(null) }

    val scrollState = rememberScrollState()

    runBlocking {
        val job = GlobalScope.launch {
            searchResultItem.value = SearchResultServer(
                input.value
            )
        }
    }
    if(searchResultItem.value != null){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(BackGroundWhite)
                .verticalScroll(scrollState)

        ) {
            Notification(productName + " 검색결과 입니다. 아래에 검색된 상품들을 만나보세요.")
            HomeSearchBar(
                navController,
                input
            )

            RealSearchProducts(
                searchResultItem.value,
                navController,
                barPrice = barPrice,
                productID,
                isItemWhichPart
            )
            Spacer(Modifier.height(30.dp))
        }
    } else {
        LoaderSet(semantics = "${input.value} 상품을 검색하는 중입니다. 잠시만 기다려주세요.")
    }
}

// 검색 결과 목록 컴포저블
// ProductInfo 컴포저블과 거의 동일한 패턴
@Composable
fun RealSearchProducts(
    result: List<SearchResultItem>? = null,
    navController: NavController,
    barPrice: MutableState<Float>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
){

    var count: Int? = null

    isItemWhichPart.value = 0
    // represent {index * 2} items
    val index = remember { mutableStateOf(5) } // 보여 주고자 하는 상품의 개수


    //Todo, control the time optimally...
    // 분명 sleep with time 이 아닌
    // wait로 처리하는 로직이 있을 거 같음.


    if(result != null){
        count = result!!.size // 실제 검색된 상품의 개수
    }

    Column {
        // 아래의 for 문은 보여주고자 하는 상품으 개수를 결정
        for(i in 1..index.value){
            val realIndex = i-1
            Row (
                modifier = Modifier
                    .padding(10.dp)
            ){
                if(count != null){
                    var tmpResult1: SearchResultItem? = null
                    var tmpResult2: SearchResultItem? = null
                    if(realIndex*2+1 <= count){
                        tmpResult1 = result!![realIndex*2]
                    }
                    if(realIndex*2+2 <= count){
                        tmpResult2 = result!![realIndex*2+1]
                    }
//                    val tmpResult1 = result!![realIndex*2]
//                    val tmpResult2 = result[realIndex*2+1]
                    if (tmpResult1 != null) {
                        SearchCard(
                            id = tmpResult1.id,
                            img = tmpResult1.image_url,
                            name = tmpResult1.name,
                            price = tmpResult1.price,
                            description = tmpResult1.description,
                            category = tmpResult1.category,
                            navController = navController,
                            barPrice = barPrice,
                            productID = productID,
                            isItemWhichPart = isItemWhichPart
                        )
                    }
                    if (tmpResult2 != null) {
                        SearchCard(
                            id = tmpResult2.id,
                            img = tmpResult2.image_url,
                            name = tmpResult2.name,
                            price = tmpResult2.price,
                            description = tmpResult2.description,
                            category = tmpResult2.category,
                            navController = navController,
                            barPrice = barPrice,
                            productID = productID,
                            isItemWhichPart = isItemWhichPart
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
                        index.value += 5
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

@Composable
fun SearchProducts(
    result: List<Popular>? = null,
    navController: NavController,
    barPrice: MutableState<Float>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
){

    var count: Int? = null


    // represent {index * 2} items
    val index = remember { mutableStateOf(5) }


    //Todo, control the time optimally...
    // 분명 sleep with time 이 아닌
    // wait로 처리하는 로직이 있을 거 같음.


    if(result != null){
        count = result!!.size
    }

    Column {
        // 아래의 for 문은 보여주고자 하는 상품으 개수를 결정
        for(i in 1..index.value){
            val realIndex = i-1
            Row (
                modifier = Modifier
                    .padding(10.dp)
            ){
                if(count != null){
                    var tmpResult1: Popular? = null
                    var tmpResult2: Popular? = null
                    if(realIndex*2+1 <= count){
                        tmpResult1 = result!![realIndex*2]
                    }
                    if(realIndex*2+2 <= count){
                        tmpResult2 = result!![realIndex*2+1]
                    }
//                    val tmpResult1 = result!![realIndex*2]
//                    val tmpResult2 = result[realIndex*2+1]
                    if (tmpResult1 != null) {
                        SearchCard(
                            id = tmpResult1.id,
                            img = tmpResult1.image_url,
                            name = tmpResult1.name,
                            price = tmpResult1.price,
                            description = tmpResult1.description,
                            category = tmpResult1.category,
                            navController = navController,
                            barPrice = barPrice,
                            productID = productID,
                            isItemWhichPart = isItemWhichPart
                        )
                    }
                    if (tmpResult2 != null) {
                        SearchCard(
                            id = tmpResult2.id,
                            img = tmpResult2.image_url,
                            name = tmpResult2.name,
                            price = tmpResult2.price,
                            description = tmpResult2.description,
                            category = tmpResult2.category,
                            navController = navController,
                            barPrice = barPrice,
                            productID = productID,
                            isItemWhichPart = isItemWhichPart
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
                        index.value += 5
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

// 검색된 각 상품의 카드 컴포저블
// Product Info와 거의 동일한 패턴
@Composable
fun SearchCard(
    id: Int,
    img: String,
    description: String,
    name: String,
    price: Float,
    category: String,
    navController: NavController,
    barPrice: MutableState<Float>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
){

    val imageLoader = LocalContext.current.imageLoader.newBuilder()
        .logger(DebugLogger())
        .build()

    Box(
        modifier = Modifier
            // screen reader를 위해 텍스트를 한 묶음으로 처리
            .semantics(mergeDescendants = true){
                text = AnnotatedString(name + "상품 입니다." +"상품의 가격은" + price + "입니다.")
            }
            /////////////////////////
            .width(180.dp)
            .padding(
                start = 10.dp,
                end = 10.dp,
            )
            .clickable {
                barPrice.value = price
                productID.value = id
                navController.navigate("productInfo")
            }
    ){
        Image(
            painter = painterResource(
                if(theme.themeId.value == 0){
                R.drawable.home_rec
                } else if(theme.themeId.value == 1){
                    R.drawable.home_rec_pink
                } else{
                    R.drawable.home_rec
                }
            ),
            contentDescription = "",
            modifier = Modifier.shadow(elevation = 15.dp, shape = RoundedCornerShape(15.dp))
        )
        Column (
            modifier = Modifier.fillMaxSize()
        ){

            AsyncImage(
                imageLoader = imageLoader,
                modifier = Modifier
                    .offset(
                        y = 10.dp
                    )
                    .size(170.dp)
                    .clip(RoundedCornerShape(15.dp))
                ,
                model = if(img[0]=='\"'){img.substring(1,img.length-1)} else{img},
                contentDescription = name + "상품 이미지"
            )
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
                    text = name.substring(1,name.length-1),
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
                    text = textPrice+"원",
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


