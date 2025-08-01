package com.bexprint

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class BexPrintPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(BexPrintModule(reactContext))
    }

    override fun createJSModules(): MutableList<Class<out JavaScriptModule>> {
        return mutableListOf()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
