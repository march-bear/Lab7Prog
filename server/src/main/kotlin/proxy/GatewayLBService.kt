package proxy

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import network.WorkerInterface
import network.receiver.TCPStreamReceiver
import network.sender.SenderInterface
import network.sender.TCPStreamSender
import org.slf4j.LoggerFactory
import serverworker.LoggerWrapper
import serverworker.TCPStreamReceiverWrapper
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class GatewayLBService(
    port1: Int,
    port2: Int,
) : WorkerInterface {

    @get:Synchronized
    private var currWorker: Int = 0
        get() {
            val old = field
            field = (field + 1) % workersList.size
            return old
        }

    private val log = LoggerWrapper(LoggerFactory.getLogger(GatewayLBService::class.java))

    private val sockForClients: ServerSocket = ServerSocket(port1)
    private val sockForServers: ServerSocket = ServerSocket(port2)

    private val workersList: CopyOnWriteArrayList<SenderInterface> = CopyOnWriteArrayList()
    private val clientsMap: ConcurrentMap<String, Socket> = ConcurrentHashMap()

    override fun start() {
        val execReceive = Executors.newFixedThreadPool(10)

        Thread {
            while (true) {
                val sockKey = generateKey(20)
                clientsMap[sockKey] = sockForClients.accept()

                execReceive.execute {
                    val receiver = TCPStreamReceiver(clientsMap[sockKey]!!)
                    while (true) {
                        val msg = receiver.receive()
                        if (workersList.isNotEmpty()) {
                            workersList[currWorker].send(Json.encodeToString(Pair(sockKey, msg)))
                        }
                    }
                }
            }
        }.start()

        Thread {
            while (true) {
                val sock = sockForServers.accept()
                workersList.add(TCPStreamSender(sock))

                execReceive.execute {

                }
            }
        }
    }
}

fun generateKey(length: Int): String = CharArray(length) { validChars.random() }.concatToString()
val validChars: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')