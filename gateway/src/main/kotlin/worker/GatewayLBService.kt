package worker

import AbstractCommandManager
import CommandManager
import generateKey
import message.MessageComparator
import iostreamers.Messenger
import iostreamers.TextColor
import message.DataBaseChanges
import network.WorkerInterface
import message.Message
import message.MessageCase
import registeringNewServerModule
import java.io.IOException
import java.lang.NullPointerException
import java.net.ServerSocket
import java.net.SocketTimeoutException
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

    internal val workersList: CopyOnWriteArrayList<TCPStreamSenderWrapper> = CopyOnWriteArrayList()
    internal val clientsMap: ConcurrentMap<String, Pair<SocketType, TCPStreamSenderWrapper>> = ConcurrentHashMap()
    private val msgCQueue: BlockingQueue<Message> = LinkedBlockingQueue()
    private val msgSQueue: PriorityBlockingQueue<Message> = PriorityBlockingQueue(11, MessageComparator())

    private val commandManager: AbstractCommandManager = CommandManager(registeringNewServerModule, this)
    private val clientMessageHandler = GCMessageHandler(this, commandManager)
    private val serverMessageHandler = GSMessageHandler(this)

    @get:Synchronized
    @set:Synchronized
    internal var updatingInProgress = false

    private var isRunning = false

    @get:Synchronized
    @set:Synchronized
    internal var lastUpdate: Long = -1

    override fun start() {
        Messenger.print("СТАРТУЕМ")
        isRunning = true
        val execReceive = Executors.newCachedThreadPool()

        Thread {
            while (isRunning) {
                val sockKey = generateKey(20)

                val sock = try {
                    serv.accept()
                } catch (ex: IOException) {
                    break
                }

                println("Новая зараза $sockKey, Приветствуем!")
                clientsMap[sockKey] = Pair(SocketType.CLIENT, TCPStreamSenderWrapper(sock))

                execReceive.execute {
                    sock.soTimeout = TIMEOUT_FOR_SOCKETS
                    val receiver = TCPStreamReceiverWrapper(sock)
                    while (isRunning) {
                        val msg = try {
                            receiver.receive() ?: continue
                        } catch (ex: IOException) {
                            break
                        } catch (ex: SocketTimeoutException) {
                            when (clientsMap[sockKey]?.first) {
                                SocketType.CLIENT, null -> break
                                SocketType.SERVER -> continue
                            }
                        }

                        when (clientsMap[sockKey]?.first) {
                            SocketType.CLIENT -> msgCQueue.add(MessageCase(sockKey, msg))
                            SocketType.SERVER -> msgSQueue.add(msg)
                            null -> break
                        }
                    }

                    try { sock.close() } catch (_: IOException) {}
                    println("Нас покинул $sockKey")
                }
            }
        }.start()

        val execHandle = Executors.newCachedThreadPool()

        Thread {
            while (isRunning) {
                if (!updatingInProgress) {
                    val msg = msgCQueue.poll()
                    if (msg != null) {
                        execHandle.execute {
                            clientMessageHandler.process(msg)
                        }
                    }
                }
            }
        }.start()

        Thread {
            while (isRunning) {
                if (updatingInProgress) continue

                if ((msgSQueue.peek() ?: continue)::class.java == DataBaseChanges::class.java) {
                    updatingInProgress = true

                    while ((msgSQueue.peek() as DataBaseChanges).number - 1 != lastUpdate) {
                        println("$lastUpdate ${(msgSQueue.peek() as DataBaseChanges).number - 1}")
                        Thread.sleep(300)
                        continue
                    }
                    ++lastUpdate
                }

                val msg = msgSQueue.poll()
                execHandle.execute {
                    try {
                        serverMessageHandler.process(msg)
                    } catch (_: NullPointerException) {

                    }
                }
            }
        }.start()

        while (isRunning) {
            val cmd = readlnOrNull()?.trim()
            if (cmd == "exit" || cmd == null)
                isRunning = false
        }

        serv.close()
        execHandle.shutdown()
        execReceive.shutdown()
        Messenger.print("Завершение работы сервиса...", TextColor.YELLOW)
    }

    private fun getNextWorker(): TCPStreamSenderWrapper? {
        println(workersList)
        while (true) {
            if (workersList.isEmpty())
                return null
            try {
                return workersList[currWorker++ % workersList.size]
            } catch (_: IndexOutOfBoundsException) {

            }
        }
    }

    private fun deleteWorkerBySender(sender: TCPStreamSenderWrapper) {
        workersList.remove(sender)
        clientsMap.values.removeIf { it.second == sender }
    }

    fun sendToServer(msg: Message): Boolean {
        while (true) {
            val sender = getNextWorker() ?: return false
            try {
                sender.send(msg)
                return true
            } catch (_: IOException) {
                deleteWorkerBySender(sender)
            }
        }
    }

    fun sendToAllServers(msg: Message) {
        val workersToDelete = mutableListOf<TCPStreamSenderWrapper>()
        workersList.forEach {
            try {
                it.send(msg)
            } catch (_: IOException) {
                workersToDelete.add(it)
            }
        }

        workersToDelete.forEach { deleteWorkerBySender(it) }
    }

    fun sendToClient(msg: Message, cid: String) {
        val client = clientsMap[cid]
        try {
            client?.second?.send(msg)
        } catch (_: IOException) {
            clientsMap.remove(cid)
        }
    }

    private companion object {
        const val TIMEOUT_FOR_SOCKETS = 3000000
    }
}

enum class SocketType {
    CLIENT,
    SERVER,
}