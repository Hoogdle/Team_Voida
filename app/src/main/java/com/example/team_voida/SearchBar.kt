package com.example.team_voida

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.MutableState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.team_voida.ui.theme.LoginTextFiled
import com.example.team_voida.ui.theme.SearchBarColor
import com.example.team_voida.ui.theme.TextColor
import java.util.Locale


// 검색바 메인 컴포저블
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    navController: NavController,
    resultInput: MutableState<String>
){

    val placeholder: String = "검색"
    val context = LocalContext.current

    // input : store the search result
    var input = remember { mutableStateOf("") }

    val interactionSource = remember { MutableInteractionSource() }

    // 아래의 구글 음성 검색 API 활용
    // 하단 코드는 오픈소스 코드를 참조하여 활용.
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val spokenText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                input.value = spokenText  // Update prompt with recognized text
                resultInput.value = input.value
                navController.navigate("searchResult")
            } else {
                Toast.makeText(context, "음성인식에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    )



    Box(
        modifier = Modifier
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    ){

        // Todo
        // 1. check why BTF Not working
        // => then almost done
        BasicTextField(
            // 필터 입력 후 Action에 대해 정의
            keyboardActions = KeyboardActions(
                // 예시)
                // 백엔드로 필터링 된 데이터 요청 함수
                // callFilter2Backend(input)

                // 선택된 페이지에 관한 데이터는 이 함수의 input 변수를 활용
                onDone = {
                    navController.navigate("searchResult")
                }
            ),
            value = resultInput.value,
            onValueChange = {
                resultInput.value = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 10.dp,
                    end = 10.dp
                )
                .clip(RoundedCornerShape(35.dp))
                .background(
                    color = SearchBarColor
                )
                .height(50.dp)

            ,
            singleLine = true,
            textStyle = TextStyle(
                color = TextColor,
                fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                fontSize = 14.sp
            ),

            decorationBox = @Composable{ innerTextField ->
                TextFieldDefaults.DecorationBox(
                    placeholder = {
                        Column (
                            modifier = Modifier.fillMaxWidth()
                        ){
                            Text(
                                text = placeholder,
                                color = Color.LightGray,
                                style = TextStyle(
                                    color = TextColor,
                                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                                    fontSize = 16.sp
                                ),
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    enabled = true,
                    innerTextField = innerTextField,
                    value = resultInput.value.toString(),
                    interactionSource = interactionSource,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextColor,
                        unfocusedTextColor = TextColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = TextColor,
                        unfocusedContainerColor = SearchBarColor,
                        focusedContainerColor = SearchBarColor,
                        errorContainerColor = SearchBarColor,
                        disabledContainerColor = SearchBarColor
                    ),
                )
            }
        )

        Image(
            painter = painterResource(R.drawable.mic),
            contentDescription = "음성검색",
            modifier = Modifier
                .size(20.dp)
                .offset(
                    x = 218.dp,
                    y = 14.dp
                )
                .clickable {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        intent.putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voida Assistance가 음성을 인식합니다.")
                        speechRecognizerLauncher.launch(intent)
                    } else {
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            100
                        )
                    }
                }
        )
    }
}