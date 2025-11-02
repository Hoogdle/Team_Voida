package com.example.team_voida.Home

import android.util.Log
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL



suspend fun HomePopularCall():List<Popular>?{
    try{
        val url = URL("https://fluent-marmoset-immensely.ngrok-free.app/Home") // edit1
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET" // edit2 // or POST


        if(connection.responseCode == HttpURLConnection.HTTP_OK){

            val inputStream = connection.inputStream.bufferedReader().use{it.readText()}

            val json = Json.decodeFromString<List<Popular>>(inputStream) // edit3

            return json
        } else {
            return null
        }
    } catch (e:Exception){
        e.printStackTrace()
        return null
    }
}

suspend fun HomePopularList():List<Popular>?{
    try{
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/PopularItems") // edit1
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET" // edit2 // or POST

        if(connection.responseCode == HttpURLConnection.HTTP_OK){
            val inputStream = connection.inputStream.bufferedReader().use{it.readText()}
            val json = Json.decodeFromString<List<Popular>>(inputStream) // edit3
            return json
        } else {
            return null
        }
    } catch (e:Exception){
        e.printStackTrace()
        return null
    }
}

suspend fun HomeBigSaleList():List<Popular>?{
    try{
        val url = URL("https://fluent-marmoset-immensely.ngrok-free.app/BigSaleItems") // edit1
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET" // edit2 // or POST

        if(connection.responseCode == HttpURLConnection.HTTP_OK){
            val inputStream = connection.inputStream.bufferedReader().use{it.readText()}
            val json = Json.decodeFromString<List<Popular>>(inputStream) // edit3
            return json
        } else {
            return null
        }
    } catch (e:Exception){
        e.printStackTrace()
        return null
    }
}

suspend fun HomeTodaySaleList():List<Popular>?{
    try{
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/TodaySaleItems") // edit1
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET" // edit2 // or POST

        if(connection.responseCode == HttpURLConnection.HTTP_OK){
            val inputStream = connection.inputStream.bufferedReader().use{it.readText()}
            val json = Json.decodeFromString<List<Popular>>(inputStream) // edit3
            return json
        } else {
            return null
        }
    } catch (e:Exception){
        e.printStackTrace()
        return null
    }
}

suspend fun HomeNewList():List<Popular>?{
    try{
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/NewItems") // edit1
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET" // edit2 // or POST

        if(connection.responseCode == HttpURLConnection.HTTP_OK){
            val inputStream = connection.inputStream.bufferedReader().use{it.readText()}
            val json = Json.decodeFromString<List<Popular>>(inputStream) // edit3
            return json
        } else {
            return null
        }
    } catch (e:Exception){
        e.printStackTrace()
        return null
    }
}