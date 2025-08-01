package com.bexprint

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.bexprint.print.EscPosCommands
import com.bexprint.print.EscPosFormatter
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class BluetoothConnectionManager(private val device: BluetoothDevice) {
    private val TAG = "BluetoothConnectionManager"
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    @SuppressLint("MissingPermission")
    fun connect(): Boolean {
        return try {
            adapter?.cancelDiscovery()
            val uuid = getDeviceUUID(device)
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            outputStream = socket?.outputStream
            Log.i(TAG, "Connected to ${device.name}")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed", e)
            disconnect()
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
            Log.i(TAG, "Disconnected from ${device.name}")
        } catch (e: IOException) {
            Log.e(TAG, "Disconnection error", e)
        }
        socket = null
        outputStream = null
    }

    fun isConnected(): Boolean = socket?.isConnected == true

    @Throws(IOException::class)
    fun print(data: String, lineWidth: Int = 69) {
        if (!isConnected()) throw IOException("Not connected")
        val bytes = EscPosFormatter.formatMultiLine(data, lineWidth)
        outputStream?.apply {
            write(EscPosCommands.selectFontA)
            write(EscPosCommands.setFontSize("normal"))
            write(bytes)
            flush()
        }
        Thread.sleep(300)
    }

    // Helper funkcija za dobijanje UUID (kopirano iz tvog primera)
    @SuppressLint("MissingPermission")
    private fun getDeviceUUID(device: BluetoothDevice): UUID {
        val sppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val uuids = device.uuids
        if (uuids != null && uuids.isNotEmpty()) {
            val hasSpp = uuids.any { it.uuid == sppUUID }
            return if (hasSpp) sppUUID else uuids[0].uuid
        }
        return sppUUID
    }
}
