package proxy

import network.WorkerInterface
import org.slf4j.LoggerFactory
import serverworker.LoggerWrapper
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class StreamProxyServerWorker(
    port: Int,
) : WorkerInterface {
    private val log = LoggerWrapper(LoggerFactory.getLogger(StreamProxyServerWorker::class.java))
    private val serv: ServerSocket = ServerSocket(port)
    private val workerList: MutableList<Socket> = mutableListOf()

    override fun start() {
        val execSend = Executors.newFixedThreadPool(10)
        val execReceive = Executors.newCachedThreadPool()
        while (true) {
            val sock = serv.accept()
            execSend.execute {
                sock.getInputStream()
            }

            execReceive.execute {
                sock.getOutputStream()
            }
        }
    }
}