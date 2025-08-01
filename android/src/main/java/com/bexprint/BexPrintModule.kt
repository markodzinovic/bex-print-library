package com.bexprint

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class BexPrintModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private var connectionManager: BluetoothConnectionManager? = null


    override fun getName(): String = "BexPrintModule"

    @ReactMethod
    fun getHelloMessage(promise: Promise) {
        promise.resolve("Hello from BexPrintModule!")
    }

     @SuppressLint("MissingPermission")
    @ReactMethod
    fun connect(deviceName: String, promise: Promise) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            promise.reject("BT_DISABLED", "Bluetooth is not enabled")
            return
        }

        val device = bluetoothAdapter.bondedDevices.find { it.name == deviceName }
        if (device == null) {
            promise.reject("DEVICE_NOT_FOUND", "Device not found: $deviceName")
            return
        }

        connectionManager?.disconnect()  // Ako postoji ranija konekcija, prekini je

        connectionManager = BluetoothConnectionManager(device)

        if (connectionManager?.connect() == true) {
            promise.resolve("Connected to $deviceName")
        } else {
            connectionManager = null
            promise.reject("CONNECTION_FAILED", "Failed to connect to $deviceName")
        }
    }

    @ReactMethod
    fun disconnect(promise: Promise) {
        connectionManager?.disconnect()
        connectionManager = null
        promise.resolve("Disconnected")
    }

    @ReactMethod
    fun print(data: String,lineWidth: Int = 69, promise: Promise) {
        if (connectionManager == null || connectionManager?.isConnected() != true) {
            promise.reject("NOT_CONNECTED", "No active Bluetooth connection")
            return
        }

        try {
            connectionManager?.print(data, lineWidth)
            promise.resolve("Printed successfully")
        } catch (e: Exception) {
            promise.reject("PRINT_ERROR", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    @ReactMethod
    fun listPairedDevices(promise: Promise) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            promise.reject("BT_DISABLED", "Bluetooth is not enabled")
            return
        }

        val deviceNames = bluetoothAdapter.bondedDevices.map { it.name }
        promise.resolve(Arguments.fromList(deviceNames))
    }
}
