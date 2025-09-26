import android.util.Log
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

fun AssistantSelector(llmCategory:String): String{

    var result = "None"
    when{
        llmCategory.contains("상품검색", ignoreCase = true) ->{

        }
        else -> {

        }
    }

    return result
}
fun AssistantMediator(){

}