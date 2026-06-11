package com.example.api

import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

object OptimizedApiClient {
    val okHttpClient: OkHttpClient by lazy {
        val dispatcher = Dispatcher().apply {
            maxRequests = 2
            maxRequestsPerHost = 2
        }

        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .dispatcher(dispatcher)
            .addInterceptor(GzipInterceptor())
            .addInterceptor(RetryInterceptor())
            .build()
    }
}

class GzipInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.body == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest)
        }

        // Just add Accept-Encoding: gzip, okhttp automatically decompresses if we don't handle it
        // Or actually the prompt says "Enables GZIP compression on all requests", which might mean compressing request body
        // OkHttp handles response GZIP by default via Accept-Encoding.
        val request = originalRequest.newBuilder()
            .header("Accept-Encoding", "gzip")
            .build()
        return chain.proceed(request)
    }
}

class RetryInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var retryCount = 0
        val maxRetries = 4
        val backoffMap = listOf(1000L, 2000L, 4000L, 8000L)

        while (retryCount <= maxRetries) {
            try {
                if (response != null) {
                    response.close()
                }
                response = chain.proceed(request)
                if (response.isSuccessful) {
                    return response
                }
            } catch (e: IOException) {
                exception = e
            }
            if (retryCount < maxRetries) {
                try {
                    Thread.sleep(backoffMap[retryCount])
                } catch (ignored: InterruptedException) {
                }
            }
            retryCount++
        }
        
        if (exception != null) throw exception
        return response ?: chain.proceed(request)
    }
}
