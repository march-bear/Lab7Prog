package worker

import AbstractCommandManager
import CommandManager
import MessageComparator
import message.Infarct
import network.WorkerInterface
import message.Message
import message.MessageCase
import registeringNewServerModule
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue

class GatewayLBService(
    port: Int,
) : WorkerInterface {

    @get:Synchronized
    @set:Synchronized
    private var currWorker: Int = 0

    internal val superSecretWord = "persik"
    private val serv: ServerSocket = ServerSocket(port)

    internal val workersList: CopyOnWriteArrayList<Pair<Socket, TCPStreamSenderWrapper>> = CopyOnWriteArrayList()
    internal val clientsMap: ConcurrentMap<String, Pair<Socket, TCPStreamSenderWrapper>> = ConcurrentHashMap()
    private val msgCQueue: BlockingQueue<Message> = LinkedBlockingQueue()
    private val msgSQueue: PriorityBlockingQueue<Message> = PriorityBlockingQueue(11, MessageComparator())

    private val commandManager: AbstractCommandManager = CommandManager(registeringNewServerModule, this)
    private val clientMessageHandler = GCMessageHandler(this, commandManager)
    private val serverMessageHandler = GSMessageHandler(this)


    internal var updatingInProgress = false
    private var isRunning = false
    private var lastUpdate: Long = 0

    override fun start() {
        isRunning = true
        val execReceive = Executors.newCachedThreadPool()

        Thread {
            while (isRunning) {
                val sockKey = generateKey(20)
                val sock = serv.accept()

                clientsMap[sockKey] = Pair(sock, TCPStreamSenderWrapper(sock))

                execReceive.execute {
                    val receiver = TCPStreamReceiverWrapper(sock)
                    while (true) {
                        val msg = receiver.receive() ?: continue
                        msgCQueue.add(MessageCase(sockKey, msg))
                    }
                }
            }
        }.start()

        val execHandle = Executors.newCachedThreadPool()

        Thread {
            while (isRunning) {
                if (!updatingInProgress)
                    execHandle.execute {
                        clientMessageHandler.process(msgCQueue.take())
                    }
            }
        }.start()

        Thread {
            while (isRunning) {
                if (msgSQueue.peek()::class.java == Infarct::class.java) {
                    updatingInProgress = true
                    if ((msgSQueue.peek() as Infarct).number - 1 != lastUpdate) {
                        continue
                    } else {
                        ++lastUpdate
                    }
                }
                execHandle.execute {
                    serverMessageHandler.process(msgSQueue.take())
                }
            }
        }.start()

        while (isRunning) {
            val cmd = readlnOrNull()?.trim()
            if (cmd == "exit" || cmd == null)
                isRunning = false
        }
    }

    private fun getNextWorker(): Pair<Socket, TCPStreamSenderWrapper>? {
        while (true) {
            if (workersList.isEmpty())
                return null
            try {
                return workersList[currWorker++ % workersList.size]
            } catch (_: IndexOutOfBoundsException) {

            }
        }
    }

    fun sendToServer(msg: Message): Boolean {
        getNextWorker()?.second?.send(msg) ?: return false
        return true
    }

    fun sendToAllServers(msg: Message) {
        workersList.forEach {
            it.second.send(msg)
        }
    }
}

fun generateKey(length: Int): String = CharArray(length) { validChars.random() }.concatToString()
val validChars: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')