package com.example.team_voida.Categories

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.LightPink
import com.example.team_voida.ui.theme.SkyBlue
import com.example.team_voida.ui.theme.TextLittleDark
import kotlin.math.ceil


//1. 식품
//2. 술
//3. 생활
//4. 스포츠,레저,캠핑
//5. 반려동물
//6. 뷰티
//7. 패션

// 카테고리 메인 컴포저블
@Composable
fun Categories(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    categoryCode: MutableState<String>,
    isItemWhichPart: MutableState<Int>,
){

    isItemWhichPart.value = 0
    // 카테고리 해당 하단 네비 활성화
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
            basketFlag.value = false
            homeNavFlag.value = true
            productFlag.value = false

            selectedIndex.value = 1
            Log.e("123","on_start")
        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }

    
    val scrollState = rememberScrollState()
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)

    ){
        // 안내바
        Notification("카테고리 페이지입니다. 아래에 목록에서 원하는 상품 목록을 선택해주세요.")
        CategoryTitle()
        Spacer(Modifier.height(20.dp))
        CategoryColumn(cateList,categoryCode,navController)
    }
}

@Composable
fun CategoryTitle(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
        ,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = 23.dp
                ),
            textAlign = TextAlign.Center,
            text = "All Categories",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 25.sp,
                fontFamily = FontFamily(Font(R.font.roboto_bold)),
            )
        )
        Image(
            painter = painterResource(R.drawable.basket_close),
            contentDescription = "",
            modifier = Modifier
                .padding(
                    end = 25.dp,
                    top = 30.dp
                    )
                .size(15.dp)

        )
    }
}

// 카테고리 리스트 컴포저블
@Composable
fun CategoryColumn(
    categoryList: List<CategoryItem>,
    categoryCode: MutableState<String>,
    navController: NavController
){
    Column(
        modifier = Modifier
            .padding(
                start = 10.dp,
                end = 10.dp
            )
    ){
        categoryList.forEachIndexed { index, item ->
            CategoryRow(
                item,
                index,
                categoryCode = categoryCode,
                navController = navController
            )
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(20.dp))
    }
}
// 각 카테고리 카드 컴포저블
@Composable
fun CategoryRow(
    categoryItem: CategoryItem,
    index: Int,
    categoryCode: MutableState<String>,
    navController: NavController
){
    val isSelected = remember { mutableStateOf(false) }
    val buttonColor = remember { mutableStateOf(Color.White) }
    val buttonArrow = remember { mutableStateOf(R.drawable.basket_down) }
    val buttonShadow = remember { mutableStateOf(DefaultShadowColor) }

    Column {
        Button (
            contentPadding = PaddingValues(0.dp),
            colors = ButtonColors(
                contentColor = Color.Transparent,
                containerColor = buttonColor.value,
                disabledContentColor = Color.Transparent,
                disabledContainerColor = buttonColor.value
            ),
            onClick = {
                if(index in 0..4 || index == 8){
                    isSelected.value = !isSelected.value

                    if(isSelected.value){
                        buttonColor.value = SkyBlue
                        buttonArrow.value = R.drawable.basket_up
                        buttonShadow.value = ButtonBlue
                    } else{
                        buttonColor.value = Color.White
                        buttonArrow.value = R.drawable.basket_down
                        buttonShadow.value = DefaultShadowColor
                    }
                }
            },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .shadow(
                    ambientColor = buttonShadow.value,
                    spotColor = buttonShadow.value,
                    elevation = 5.dp
                )

        ){
            Row (
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(9f)
                ){
                    AsyncImage(
                        modifier = Modifier
                            .size(
                                width = 70.dp,
                                height = 70.dp
                            )
                            .padding(
                                top = 5.dp,
                                bottom = 5.dp
                            )
                        ,
                        model = categoryItem.img,
                        contentDescription = ""
                    )

                    Text(
                        modifier = Modifier
                            .padding(
                                start = 10.dp,
                                top = 23.dp
                            ),
                        textAlign = TextAlign.Center,
                        text = categoryItem.name,
                        color = TextLittleDark,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        )
                    )

                }

                if(index in 0..4 || index == 8){
                    Image(
                        modifier = Modifier
                            .padding(
                                top = 30.dp,
                                end = 15.dp
                            )
                            .fillMaxSize()
                            .weight(1f)
                            .size(8.dp)
                        ,
                        painter = painterResource(buttonArrow.value),
                        contentDescription = "",
                    )
                }
            }
        }
        if(isSelected.value){
            if(categoryItem.subCategories != null){
                val iterateNum = ceil(categoryItem.subCategories.size / 2.0) .toInt()
                for(i in 0..iterateNum-1){
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Button(
                            colors = ButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.White,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.White
                            ),
                            onClick = {
                                categoryCode.value = "Vegetable"
                                navController.navigate("categoryList")
                            },
                            shape = RoundedCornerShape(5.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = LightPink
                            ),
                            modifier = Modifier
                                .fillMaxWidth(
                                    if (i*2+1==categoryItem.subCategories.size) 0.5f
                                    else 1f
                                )
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                text = categoryItem.subCategories[i*2],
                                color = TextLittleDark,
                                style = TextStyle(
                                    fontSize = 14.5.sp,
                                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                                )
                            )
                        }
                        if(i*2+1 < categoryItem.subCategories.size){
                            Button(
                                colors = ButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White,
                                    disabledContainerColor = Color.White
                                ),
                                onClick = {},
                                shape = RoundedCornerShape(5.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = LightPink
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(
                                        if (i*2+1==categoryItem.subCategories.size) 0.5f
                                        else 1f
                                    )
                                    .weight(1f)
                                    .padding(5.dp)
                            ) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = categoryItem.subCategories[i*2+1],
                                    color = TextLittleDark,
                                    style = TextStyle(
                                        fontSize = 14.5.sp,
                                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                                    )
                                )
                            }
                        } else {

                        }
                    }
                }
            }
        }
    }
}