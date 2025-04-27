package com.example.myapplication.network

import android.util.Log
import java.io.DataInputStream
import java.net.Socket
import java.nio.ByteBuffer

private const val TAG = "SocketManager"

class SocketManager(
    private val address: String,
    private val port: Int,
) {
    private var socket: Socket? = null

    fun connect() {
        socket = Socket(address, port)

        Log.d(TAG, "connected: ${socket?.isConnected}")
    }

    fun send(request: TestRequest) {
        val message = "{ $request }" // <- request

        Log.i(TAG, "sending: $message")

        val messageBytes = message.toByteArray()
        val lengthBytes = ByteBuffer.allocate(4).putInt(messageBytes.size).array()

        val outputStream = socket?.getOutputStream()
        outputStream?.write(lengthBytes)
        outputStream?.write(messageBytes)
        outputStream?.flush()
    }

    fun receive(): TestResponse {
        val inputStream = DataInputStream(socket?.getInputStream())

        val lengthBytes = ByteArray(4)
        inputStream.readFully(lengthBytes)
        val length = ByteBuffer.wrap(lengthBytes).int

        val buffer = ByteArray(length)
        inputStream.readFully(buffer)
        val message = String(buffer, 0, length)

        Log.d(TAG, "received: $message")

        return TestResponse(false) // <- message
    }

    fun close() {
        socket?.close()
        socket = null
    }
}
