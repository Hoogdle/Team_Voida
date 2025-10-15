import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.navigation.NavController
import com.example.team_voida.Login.LoginSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

@Serializable
data class AssistantSearchResult(
    val keyword: String
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


suspend fun AssistantSearch(
    voiceInput: String,
): String?{
    val jsonObject = JSONObject()
    jsonObject.put("voiceInput", voiceInput)

    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL("https://fluent-marmoset-immensely.ngrok-free.app/AssistantSearch") // edit1
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
            val json = Json.decodeFromString<AssistantSearchResult>(inputStream) // edit3
            return json.keyword
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
    navController: NavController,
    voiceInput: String,
    input: MutableState<String>,
    isAssistantWorking : MutableState<Boolean>
){
    var result = "None"
    when{
        llmCategory.contains("상품검색", ignoreCase = true) ->{

            Log.e("debug","search start")
            var keyword: String? = null

            runBlocking {
                val job = GlobalScope.launch{
                    keyword = AssistantSearch(
                        voiceInput = voiceInput
                    )
                }
                while(keyword == null){
                    Thread.sleep(1000L)
                }
            }


            Log.e("debug","sesarch result :" + keyword.toString())

            if (keyword != null){
                input.value = keyword as String
                navController.navigate("searchResult")
                isAssistantWorking.value = false
            }
        }
        llmCategory.contains("계정설정", ignoreCase = true) ->{
            navController.navigate("account")
            isAssistantWorking.value = false
        }
        llmCategory.contains("카테고리", ignoreCase = true) ->{
            navController.navigate("categories")
            isAssistantWorking.value = false
        }
        llmCategory.contains("장바구니", ignoreCase = true) ->{
            navController.navigate("basket")
            isAssistantWorking.value = false
        }
        llmCategory.contains("인기상품", ignoreCase = true) ->{
            isWhichPart.value = 1
            isItemWhichPart.value = 1
            navController.navigate("partList")
            isAssistantWorking.value = false
        }
        llmCategory.contains("할인상품", ignoreCase = true) ->{
            isWhichPart.value = 2
            isItemWhichPart.value = 2
            navController.navigate("partList")
            isAssistantWorking.value = false
        }
        llmCategory.contains("배송지 변경", ignoreCase = true) ->{
            isAssistantWorking.value = false

        }
        llmCategory.contains("결제수단 변경", ignoreCase = true) ->{
            isAssistantWorking.value = false

        }
        llmCategory.contains("도움말", ignoreCase = true) ->{
            isAssistantWorking.value = false

        }
        else -> {
            isAssistantWorking.value = false

        }
    }
}

