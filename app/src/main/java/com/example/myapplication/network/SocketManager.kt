package com.example.myapplication.network

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.net.Socket
import java.nio.ByteBuffer

private const val TAG = "SocketManager"

class SocketManager(
    private val address: String,
    private val port: Int,
    private val gson: Gson
) {
    private var socket: Socket? = null

    fun connect() {
        socket = Socket(address, port)
        Log.d(TAG, "connected: ${socket?.isConnected}")
    }

    suspend fun send(request: TestRequest) {
        withContext(Dispatchers.IO) {
            val message = "${gson.toJson(request)}"

            Log.i(TAG, "sending: $message")

            val messageBytes = message.toByteArray()
            val lengthBytes = ByteBuffer.allocate(4).putInt(messageBytes.size).array()

            val outputStream = socket?.getOutputStream()
            outputStream?.write(lengthBytes)
            outputStream?.write(messageBytes)
            outputStream?.flush()
        }
    }

    suspend fun receive(): TestResponse {
        return withContext(Dispatchers.IO) {
            val inputStream = DataInputStream(socket?.getInputStream())

            val lengthBytes = ByteArray(4)
            inputStream.readFully(lengthBytes)
            val length = ByteBuffer.wrap(lengthBytes).int

            val buffer = ByteArray(length)
            inputStream.readFully(buffer)
            val message = String(buffer, 0, length)

            Log.d(TAG, "received: $message")

            gson.fromJson<TestResponse>(message, TestResponse::class.java)
        }
    }

    fun close() {
        socket?.close()
        socket = null
    }
}
