package com.example.tcptest01

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket


val TAG = "myTag"
const val HOSTIP = "192.168.0.100"
const val PORT = 4000

//const val DATA = "taonce"
const val DATA = "0fff"

class MainActivity : AppCompatActivity() {

    var host = HOSTIP // 主机是本机
    var port = PORT// 使用 2333 端口
    var datatext = DATA
    var hexconvert = false   // 就是ascii code

    lateinit var mSocket: Socket
    var outputStream: OutputStream? = null
//    var inputStream:InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // checkbox 按鍵處理
        checkBoxDefaultTargetIP.setOnClickListener {
            if (checkBoxDefaultTargetIP.isChecked)
                host = HOSTIP
            else host = ""

            editTextTargetIP.setText(host)
        }
        //
        checkBoxDefaultTargetPort.setOnClickListener {
            if (checkBoxDefaultTargetPort.isChecked)
                port = PORT
            else port = 0
            editTextTextTargetPort.setText(port.toString())
        }
//
        checkBoxHex.setOnClickListener {
            if (checkBoxHex.isChecked)
                hexconvert = true
            else hexconvert = false
        }

        //一般按鍵處理
        checkBoxDefaultData.setOnClickListener {
            if (checkBoxDefaultData.isChecked)
                datatext = DATA
            else datatext = ""
            editTextTextData.setText(datatext)
        }

//=======================================================

        // connect 連接
        btnConnect.setOnClickListener {
            GlobalScope.launch {
                mSocket = Socket()
                try {
                    host = editTextTargetIP.text.toString()
                    port = editTextTextTargetPort.text.toString().toInt()
                    mSocket!!.connect(InetSocketAddress(host, port), 2000)
                    if (mSocket!!.isConnected) {
// sendData("taonce")
// receiverData()
                        btnConnect.setTextColor(android.graphics.Color.RED)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "连接出错：${e.message}")
                }

            } //globalscope

        }

//傳送資料
        btnSenData.setOnClickListener {
            GlobalScope.launch {
                if (mSocket!!.isConnected) {
                    datatext = editTextTextData.text.toString()
                    println("datatext = $datatext")
                    if (datatext != "") {
                        println("我有資料 ")
                        sendData(datatext)
                    }  //送資料一定要在協程做, 不然是錯誤的

                    else {
                        println("資料不能為空 ")
                               runOnUiThread {
                                   Toast.makeText(this@MainActivity,"資料不能為空", Toast.LENGTH_SHORT).show()
                               }
                    }
                }
            }
        }

//讀的按鍵
        btnReadData.setOnClickListener {
            GlobalScope.launch {
                if (mSocket!!.isConnected) {
                    receiverData() //送資料一定要在協程做, 不然是錯誤的
                }
            }
        }

    } // oncreate

    /**
     * 定时发送数据
     */
    fun sendData(message: String) {

        Log.d(TAG, "Hello 我進來了 ")
        Log.d(TAG, "mSocket,$mSocket")
        try {
            outputStream = mSocket.getOutputStream()

            if (hexconvert == true) {
                val size = message.length / 2  // 忽略奇數值, 若輸入3個字串只處理1個byte
                // val r = message.length % 2 取餘

                val msg = ByteArray(size)           // 取得size
                //取值
                for (i in 0..size - 1) {
                    msg[i] = message.subSequence(i * 2, i * 2 + 2)
                        .toString()
                        .toInt(16)
                        .toByte()
                }

                //         msg[1] = message.subSequence(2,4).toString().toInt(16).toByte()
                val a = message.substring(0, 2)
                val b = message.substring(2, 4)
                val c = a.toString().toInt(16)
                val d = b.toString().toInt(16)
                println("a= $a , b =$b,c= $c , d =$d ")
                //  msg[0]=c.toByte()
                //  msg[1]=d.toByte()
                //   msg[0] = 0xaa.toByte()
                //  msg[1] = 0xdd.toByte()
                outputStream!!.write(msg)           //寫入資料 （Hex16進制）
            } else {
                outputStream!!.write(message.toByteArray())   //寫入資料 （ASCII）
            }
            outputStream!!.flush()      //即時送出

            Log.d(TAG, "发送给服务端内容为：$message")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "sendData: 發送錯誤")
        }

    }

//================接收數據 =================================
    /**
     * 定时接收数据
     */
    fun receiverData() {
        Log.d(TAG, "receiverData: 接收進來了")
        try {
            //        val data = BufferedReader(InputStreamReader(mSocket.getInputStream(), "ISO-8859-1"))
            val inputStream = mSocket.getInputStream()
            val data = BufferedReader(InputStreamReader(inputStream, "ISO-8859-1"))

            val cbuf = CharArray(1024)            // 1次讀1024字元
            var num = data.read(cbuf)              // 這是阻塞式,就是一直等到有值為止  (讀1個字）
            Log.d(TAG, "获取服务端數目为：$num")
            var s = ""
            if (checkBoxHex.isChecked) {            //就是true = 16進制
                for (i in 0..num - 1) {
                    s = s + cbuf[i].toInt().toString()
                    Log.d(TAG, "获取服务端内容为：${cbuf[i].toInt()}")
                }
            } else {
                for (i in 0..num - 1) {
                    s = s + cbuf[i].toString()
                    Log.d(TAG, "获取服务端内容为：${cbuf[i]}")

                }
            }                                 //ascii
//印出結果
            runOnUiThread { textView.text = s }

// 若你是ascii 就用cbuf[i] , 若你是16進制則後面加 .toInt() 就可以知道答案了

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "receiveData: 接收錯誤")
        }


    }


}