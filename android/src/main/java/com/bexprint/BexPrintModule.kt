package com.bexprint

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.facebook.react.bridge.*

class BexPrintModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1234
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var connectionManager: BluetoothConnectionManager? = null
    private var permissionPromise: Promise? = null

    private val activityEventListener = object : ActivityEventListener {
        override fun onActivityResult(activityRequestCode: Int, resultCode: Int, data: android.content.Intent?) {
            // Nije potrebno za permisije, ostavi prazno
        }

        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionPromise?.resolve(true)
                } else {
                    permissionPromise?.reject("PERMISSION_DENIED", "Bluetooth permission denied")
                }
                permissionPromise = null
            }
        }
    }

    init {
        reactContext.addActivityEventListener(activityEventListener)
    }

    override fun getName(): String = "BexPrintModule"

    private fun hasBluetoothPermission(): Boolean {
        val context = reactApplicationContext
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestBluetoothPermission(promise: Promise) {
        if (hasBluetoothPermission()) {
            promise.resolve(true)
            return
        }

        val activity = currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "No current activity found")
            return
        }

        permissionPromise = promise
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
            PERMISSION_REQUEST_CODE
        )
    }

    @ReactMethod
    fun getHelloMessage(promise: Promise) {
        promise.resolve("Hello from BexPrintModule!")
    }

    @SuppressLint("MissingPermission")
    @ReactMethod
    fun connect(deviceName: String, promise: Promise) {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermission(promise)
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            promise.reject("BT_DISABLED", "Bluetooth is not enabled")
            return
        }

        val device = bluetoothAdapter.bondedDevices.find { it.name == deviceName }
        if (device == null) {
            promise.reject("DEVICE_NOT_FOUND", "Device not found: $deviceName")
            return
        }

        connectionManager?.disconnect()
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
    fun print(data: String, lineWidth: Int = 69, promise: Promise) {
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
        promise.resolve(deviceNames)  // direktno šalješ listu, RN je može prihvatiti
    }

    @SuppressLint("MissingPermission")
    @ReactMethod
    fun listPairedDevicesWithAddress(promise: Promise) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            promise.reject("BT_DISABLED", "Bluetooth is not enabled")
            return
        }

        val deviceArray: WritableArray = Arguments.createArray()

        for (device in bluetoothAdapter.bondedDevices) {
            val map: WritableMap = Arguments.createMap()
            map.putString("name", device.name)
            map.putString("address", device.address)
            deviceArray.pushMap(map)
        }

        promise.resolve(deviceArray)
    }
}
