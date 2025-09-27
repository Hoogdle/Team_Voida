import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController
import com.example.team_voida.Login.LoginSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

fun Assistant(
    voiceInput: String
){

}

@Serializable
data class AssistantResult(
    val category : String
)

suspend fun AssistantToServer(
    voiceInput: String
): String?{
    val jsonObject = JSONObject()
    jsonObject.put("voiceInput", voiceInput)

    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL("https://fluent-marmoset-immensely.ngrok-free.app/AssistantCategory") // edit1
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.doOutput = true // 서버로 보내기 위해 doOutPut 옵션 활성화
        connection.doInput = true
        connection.requestMethod = "POST" // edit2 // or POST

        // 서버와 통신을 위하 코드는 아래으 url 참조
        // https://johncodeos.com/post-get-put-delete-requests-with-httpurlconnection/
        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        ) // The format of the content we're sending to the server
        connection.setRequestProperty(
            "Accept",
            "application/json"
        ) // The format of response we want to get from the server

        val outputStreamWriter = OutputStreamWriter(connection.outputStream)
        outputStreamWriter.write(jsonObjectString)
        outputStreamWriter.flush()


        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
            val json = Json.decodeFromString<AssistantResult>(inputStream) // edit3
            return json.category
        } else {
            Log.e("xxx","else")
            return null
        }
    } catch (e: Exception) {
        Log.e("xxx","catch")

        e.printStackTrace()
        return null
    }
}

fun AssistantSelector(
    isWhichPart: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
    llmCategory:String,
    navController: NavController
){
    var result = "None"
    when{
        llmCategory.contains("상품검색", ignoreCase = true) ->{
        }
        llmCategory.contains("계정설정", ignoreCase = true) ->{
            navController.navigate("account")
        }
        llmCategory.contains("카테고리", ignoreCase = true) ->{
            navController.navigate("categories")
        }
        llmCategory.contains("장바구니", ignoreCase = true) ->{
            navController.navigate("basket")
        }
        llmCategory.contains("인기상품", ignoreCase = true) ->{
            isWhichPart.value = 1
            isItemWhichPart.value = 1
            navController.navigate("partList")
        }
        llmCategory.contains("할인상품", ignoreCase = true) ->{
            isWhichPart.value = 2
            isItemWhichPart.value = 2
            navController.navigate("partList")
        }
        llmCategory.contains("배송지 변경", ignoreCase = true) ->{
        }
        llmCategory.contains("결제수단 변경", ignoreCase = true) ->{
        }
        llmCategory.contains("도움말", ignoreCase = true) ->{
        }
        else -> {}
    }
}

