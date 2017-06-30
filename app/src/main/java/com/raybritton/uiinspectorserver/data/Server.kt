package com.raybritton.uiinspectorserver.data

import com.raybritton.uiinspectorserver.data.model.Device
import timber.log.Timber

class Server(val ctx: android.content.Context, var port: Int) {
    private val group = java.net.InetAddress.getByName("229.11.22.34")
    private val deviceSubject: io.reactivex.subjects.PublishSubject<List<Device>> = io.reactivex.subjects.PublishSubject.create()
    private val uiSubject: io.reactivex.subjects.PublishSubject<String> = io.reactivex.subjects.PublishSubject.create()

    private var running: Boolean = false
    private var device: com.raybritton.uiinspectorserver.data.model.Device? = null
    private var activeThread: Thread? = null
    private var multicastSocket: java.net.MulticastSocket? = null
    private var multicastWait = true
    private lateinit var hostAddress: String

    @Suppress("ConvertLambdaToReference") //type is lost so method reference can't be used
    fun observeDevices(): io.reactivex.Observable<List<Device>> {
        deviceSubject.subscribe({list -> list.size})
        running = true
        listenForPhones()
        return deviceSubject.doOnTerminate {
            stop()
        }.hide()
    }

    fun observeStatus(): io.reactivex.Observable<String> {
        return uiSubject.hide()
    }

    fun connect(device: com.raybritton.uiinspectorserver.data.model.Device) {
        multicastWait = false
        this.device = device
        uiSubject.onNext("status|Connecting with ${this.device}")
        val dataPort = java.util.Random().nextInt(2000) + 14567
        val message = ("2@SERVER_IP|" + hostAddress + "|$dataPort|${device.code}|").toByteArray()

        startDirectSocket(dataPort)

        try {
            multicastSocket?.send(java.net.DatagramPacket(message, message.size, group, port))
            Timber.d("Connected")
        } catch (e: java.net.SocketException) {
            uiSubject.onNext("ERROR (MC): " + e.message)
        }

        multicastSocket?.close()
    }

    private fun stop() {
        Timber.d("Stopping")
        multicastWait = false
        multicastSocket?.close()
        running = false
        activeThread?.join()
    }

    private fun listenForPhones() {
        val thread = Thread(Runnable {
            hostAddress = com.raybritton.uiinspectorserver.Utils.getIpAddress(ctx)!!.hostAddress
            multicastWait = true
            multicastSocket = java.net.MulticastSocket(port)
            multicastSocket!!.networkInterface = java.net.NetworkInterface.getByInetAddress(com.raybritton.uiinspectorserver.Utils.getIpAddress(ctx))
            multicastSocket!!.joinGroup(group)

            val packet = java.net.DatagramPacket(ByteArray(256), 256)
            val listenWindow = 5000
            multicastSocket!!.soTimeout = 1000
            val devices: MutableSet<com.raybritton.uiinspectorserver.data.model.Device> = mutableSetOf()
            var nextUpdate = System.currentTimeMillis() + listenWindow
            while (multicastWait && running) {
                if (nextUpdate < System.currentTimeMillis()) {
                    deviceSubject.onNext(devices.toList())
                    devices.clear()
                    nextUpdate = System.currentTimeMillis() + listenWindow
                }
                try {
                    multicastSocket?.receive(packet)
                    val data = String(packet.data)
                    when {
                        data.startsWith("1@INSPECTOR") -> {
                            devices.add(com.raybritton.uiinspectorserver.data.model.Device("Unknown device", 0, Device.Status.OUT_OF_DATE))
                        }
                        data.startsWith("2@INSPECTOR") -> {
                            val parts = data.split(Regex.fromLiteral("|"))
                            if (parts.size > 2) {
                                val name = parts[2]
                                val phone = com.raybritton.uiinspectorserver.data.model.Device(name, 0, Device.Status.OUT_OF_DATE)
                                devices.add(phone)
                            } else {
                                devices.add(com.raybritton.uiinspectorserver.data.model.Device("Unknown device", 0, Device.Status.BAD_DATA))
                            }
                        }
                        data.startsWith("3@INSPECTOR") -> {
                            val parts = data.split(Regex.fromLiteral("|"))
                            if (parts.size > 2) {
                                val code = parts[1].toInt()
                                val name = parts[2]
                                val phone = com.raybritton.uiinspectorserver.data.model.Device(name, code, Device.Status.VALID)
                                devices.add(phone)
                            } else {
                                devices.add(com.raybritton.uiinspectorserver.data.model.Device("Unknown device", 0, Device.Status.BAD_DATA))
                            }
                        }
                    }
                } catch (e: java.net.SocketTimeoutException) {
                }
            }
        }, "Multicast thread")
        thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler{ _: Thread, throwable: Throwable ->
            uiSubject.onNext("status|ERROR (MC): " + throwable.message)
        }
        thread.start()
    }

    private fun startDirectSocket(port: Int) {
        val thread = Thread(Runnable {
            var msg: String?
            val socket = java.net.ServerSocket(port)
            var clientSocket: java.net.Socket
            socket.soTimeout = 30000
            while(running) {
                try {
                    clientSocket = socket.accept()
                    uiSubject.onNext("status|Receiving from $device")
                    val dis = java.io.DataInputStream(clientSocket.inputStream)
                    var temp: String = dis.readUTF()
                    msg = temp
                    while(temp.isNotEmpty()) {
                        try {
                            temp = dis.readUTF()
                            msg += temp
                        }catch(e: java.io.EOFException) {
//                            e.printStackTrace()
                            temp = ""
                            //end of stream
                        }
                    }
                    uiSubject.onNext("status|Showing $device")
                    uiSubject.onNext("json|" + msg)
                    clientSocket.close()
                }catch(e: java.net.SocketTimeoutException) {
//                    e.printStackTrace()
                }
            }

            socket.close()
        }, "Listener Thread")
        thread.start()
    }
}