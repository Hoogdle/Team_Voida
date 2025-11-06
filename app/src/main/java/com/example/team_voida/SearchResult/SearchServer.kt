package com.example.team_voida.SearchResult

import android.util.Log
import com.example.team_voida.Basket.BasketInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


@Serializable
data class SearchResultItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Float,
    val image_url: String,
    val category: String,
    val sector: Int
)
suspend fun SearchResultServer(
    productName: String
): List<SearchResultItem>?{

    val jsonObject = JSONObject()
    jsonObject.put("search", productName)

    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/SearchItems") // edit1
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
            val json = Json.decodeFromString<List<SearchResultItem>?>(inputStream) // edit3
            Log.e("Search","O")
            return json
        } else {
            Log.e("Search","!")

            return  null
        }
    } catch (e: Exception) {
        Log.e("Search","?")
        e.printStackTrace()
        return  null
    }
}