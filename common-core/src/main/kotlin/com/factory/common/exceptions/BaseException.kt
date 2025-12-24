package com.factory.common.exceptions

/**
 * Base exception class for all service exceptions
 */
open class BaseException(
    message: String,
    val errorCode: String,
    val statusCode: Int
) : Exception(message)
