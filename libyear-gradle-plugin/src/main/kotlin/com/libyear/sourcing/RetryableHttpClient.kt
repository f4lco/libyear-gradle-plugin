package com.libyear.sourcing

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * A wrapper around OkHttpClient that adds retry functionality with exponential backoff.
 */
class RetryableHttpClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val maxRetries: Int = 3,
    private val initialRetryDelayMillis: Long = 2000,
    private val retryBackoffMultiplier: Int = 2
) {
    private val logger = LoggerFactory.getLogger(RetryableHttpClient::class.java)

    /**
     * Executes the given request with retry capability.
     * If the request fails with a non-2xx response code, it will be retried up to [maxRetries] times
     * with exponential backoff.
     *
     * @param request The HTTP request to execute
     * @return The successful response, or throws an exception if all retries fail
     */
    fun executeWithRetry(request: Request): Response {
        var retryCount = 0
        var currentDelay = initialRetryDelayMillis
        var lastException: IOException? = null

        while (retryCount <= maxRetries) {
            try {
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    return response
                } else {
                    // Close the unsuccessful response
                    val errorBody = response.body?.string() ?: "No response body"
                    val code = response.code
                    response.close()
                    
                    if (retryCount == maxRetries) {
                        throw IOException("Request failed after $retryCount retries. Last response code: $code, body: $errorBody")
                    }
                    
                    logger.warn("Request to ${request.url} failed with code ${code}. Retrying (${retryCount + 1}/${maxRetries})...")
                }
            } catch (e: IOException) {
                lastException = e
                
                if (retryCount == maxRetries) {
                    throw IOException("Request failed after $retryCount retries", e)
                }
                
                logger.warn("Request to ${request.url} failed with exception: ${e.message}. Retrying (${retryCount + 1}/${maxRetries})...")
            }
            
            // Exponential backoff
            if (retryCount < maxRetries) {
                try {
                    logger.debug("Waiting for ${currentDelay}ms before retry")
                    TimeUnit.MILLISECONDS.sleep(currentDelay)
                    currentDelay *= retryBackoffMultiplier
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Retry interrupted", e)
                }
            }
            
            retryCount++
        }
        
        // This should never happen due to the checks above, but just in case
        throw lastException ?: IOException("Request failed after $maxRetries retries")
    }
} 