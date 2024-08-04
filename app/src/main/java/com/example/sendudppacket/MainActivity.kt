package com.example.sendudppacket

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity() {
    private val buttonSendUdpPacket         by lazy { findViewById<Button>(R.id.buttonSendUdpPacket) }
    private val linearLayoutLogs            by lazy { findViewById<LinearLayout>(R.id.linearLayoutLogs) }
    private val textInputEditTextIpv4       by lazy { findViewById<TextInputEditText>(R.id.textInputEditTextIpv4) }
    private val textInputEditTextPort       by lazy { findViewById<TextInputEditText>(R.id.textInputEditTextPort) }
    private val textInputEditTextMessage    by lazy { findViewById<TextInputEditText>(R.id.textInputEditTextMessage) }
    private val textInputEditTextHeavy      by lazy { findViewById<TextInputEditText>(R.id.textInputEditTextHeavy) }
    private val textInputEditTextIteration  by lazy { findViewById<TextInputEditText>(R.id.textInputEditTextIteration) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonSendUdpPacket.setOnClickListener {
            val targetIp = textInputEditTextIpv4.text.toString().ifEmpty { "127.0.0.1" }
            val targetPort = try { textInputEditTextPort.text.toString().toInt() } catch (e: Exception) { 12345 }
            val message = textInputEditTextMessage.text.toString().ifEmpty { "hi!" }
            val heavy = try { textInputEditTextHeavy.text.toString().toInt() } catch (e: Exception) { 1 }
            val iteration = try { textInputEditTextIteration.text.toString().toInt() } catch (e: Exception) { 1 }

            Thread {
                val result = sendUdpPacket(targetIp, targetPort, message, heavy, iteration)
                runOnUiThread {addOnLogs(result)}
            }.start()
        }
    }

    private fun sendUdpPacket(targetIp: String, targetPort: Int, message: String, heavy: Int, iteration: Int): String {
        var socket: DatagramSocket? = null
        return try {
            socket = DatagramSocket()
            val messageBytes = message.repeat(heavy).toByteArray()
            val address = InetAddress.getByName(targetIp)
            val packet = DatagramPacket(messageBytes, messageBytes.size, address, targetPort)
            repeat(iteration) {
                socket.send(packet)
                Thread.sleep(1)
            }
            socket.close()
            "$message x $heavy | $targetIp:$targetPort | $iteration x"
        } catch (e: Exception) {
            e.message.toString()
        } finally {
            socket?.close()
        }
    }

    private fun addOnLogs(message: String) {
        val log = TextView(this)
        log.text = message
        log.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        linearLayoutLogs.addView(log)
    }
}