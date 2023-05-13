package serverworker

import CollectionController
import CommandManager
import db.DataBaseManager
import network.WorkerInterface
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import java.io.File
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.*

class StreamServerWorker(
    port: Int,
    dbHost: String,
    dbPort: Int,
    dbName: String,
    dbUser: String,
    dbPasswd: String,
) : WorkerInterface, KoinComponent {
    private val log = LoggerWrapper(LoggerFactory.getLogger(StreamServerWorker::class.java))
    private val serv: ServerSocket = ServerSocket(port)
    private val cController: CollectionController = get(named("logging")) {
        parametersOf(DataBaseManager(dbHost, dbPort, dbName, dbUser, dbPasswd), log)
    }
    private var isRunning: Boolean = false

    override fun start() {
        isRunning = true
        log.info("Сервер запущен на порте ${serv.localPort}")

        Thread {
            while (true) {
                val cmd = readlnOrNull()?.trim()?.lowercase(Locale.getDefault())
                if (cmd == "exit" || cmd == null) {
                    isRunning = false
                    break
                }
            }
        }.start()

        val executor = Executors.newCachedThreadPool()
        while (isRunning) {
            val sock = serv.accept()

            executor.execute {
                val sender = TCPStreamSenderWrapper(sock)
                val receiver = TCPStreamReceiverWrapper(sock)

                while (true) {
                    val request = receiver.receive() ?: continue
                    val response = cController.process(request)
                    sender.send(response)
                }
            }
        }
    }

    private fun finish() {}
}