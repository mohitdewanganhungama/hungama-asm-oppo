package com.hungama.music.auto

fun <T> lazyAndroid(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)
