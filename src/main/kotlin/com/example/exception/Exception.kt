package com.example.exception

class NotFoundException(message: String) : Exception(message)

class ValidationException(message: String) : Exception(message)

class ConflictException(message: String) : Exception(message)