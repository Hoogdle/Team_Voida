package com.example.team_voida.Tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.team_voida.ProductInfo.Loader
import com.example.team_voida.ui.theme.BackGroundWhite

@Composable
fun LoaderSet(
    info:String = "상품 로딩중",
    semantics: String
){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().background(BackGroundWhite)
            .semantics(mergeDescendants = true){
                text = AnnotatedString(semantics)
            }
    ){
        Loader()
        Spacer(Modifier.height(15.dp))
        Text(info)
    }
}