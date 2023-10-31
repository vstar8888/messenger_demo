package ru.demo.domain.base

class DataPage<out T>(val data: List<T>, val hasMore: Boolean)