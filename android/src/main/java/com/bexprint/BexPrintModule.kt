package com.bexprint

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class BexPrintModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "BexPrintModule"

    @ReactMethod
    fun getHelloMessage(promise: Promise) {
        promise.resolve("Hello from BexPrintModule!")
    }
}
