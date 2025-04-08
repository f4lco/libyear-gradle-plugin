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

    while (true) {
      val response = try {
        client.newCall(request).execute()
      } catch (e: IOException) {
        logger.warn("Request to '${request.url}' failed with exception: '${e.message}'. Retrying ($retryCount/$maxRetries)...")
        null
      }

      if (response != null && response.isSuccessful) {
        return response
      }

      if (response != null) {
        // Close the unsuccessful response
        val errorBody = response.body?.string() ?: "No response body"
        val code = response.code
        response.close()
        logger.warn("Request to ${request.url} failed with code $code and body '$errorBody'. Retrying ($retryCount/$maxRetries)...")
      }

      if (retryCount == maxRetries) {
        throw IOException("Request failed after $retryCount retries")
      }

      try {
        logger.debug("Waiting for ${currentDelay}ms before retry")
        TimeUnit.MILLISECONDS.sleep(currentDelay)
      } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        throw IOException("Retry interrupted", e)
      }

      retryCount++
      currentDelay *= retryBackoffMultiplier
    }
  }
} 
