package com.example.fitme

import com.example.fitme.AuthResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class FoodsResponse(
    val status: String,
    val data: List<AuthResponse>
)
interface AuthApi {
    @GET("api/foods")
    fun getAllFoods(): Call<FoodsResponse>

    @POST("/api/addFood")
    fun addFood(@Body food: AuthResponse): Call<AuthResponse>
}
/*
Reference list:
Retrofit Android Tutorial - Make API Calls. 2023. YouTube video, added by Ahmed Guedmioui. [Online]. Available at: https://www.youtube.com/watch?v=8IhNq0ng-wk [Accessed 29 September 2025].
 */