package serverworker

import CollectionController
import LoggerWrapper
import command.CommandArgument
import commands.UpdateLocalCollectionCommand
import db.manager.DataBaseManager
import db.requests.queries.GetLastChangesNumberQuery
import iostreamers.Messenger
import iostreamers.TextColor
import message.DataBaseChanges
import message.Message
import message.MessageComparator
import message.Request
import message.handler.AbstractMessageHandler
import network.WorkerInterface
import network.receiver.AbstractReceiverWrapper
import network.sender.AbstractSenderWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue
import kotlin.system.exitProcess

class GStreamServerWorker(
    gHost: String,
    gPort: Int,
    dbHost: String,
    dbPort: Int,
    dbName: String,
    dbUser: String,
    dbPasswd: String,
) : WorkerInterface, KoinComponent {
    private val remote: SocketAddress = InetSocketAddress(gHost, gPort)
    private lateinit var service: Socket
    private val log = LoggerWrapper(LoggerFactory.getLogger(GStreamServerWorker::class.java))

    private lateinit var sender: AbstractSenderWrapper<Message>
    private lateinit var receiver: AbstractReceiverWrapper<Message>

    private val msgQueue = PriorityBlockingQueue(11, MessageComparator())
    private val dbm = DataBaseManager(dbHost, dbPort, dbName, dbUser, dbPasswd)

    private val msgHandler: AbstractMessageHandler = GServerMessageHandler(
        this,
        get(named("logging")) {
            parametersOf(dbm, log)
        }
    )

    private var isRunning = false
    private var updatingInProgress = false

    override fun start() {
        if (!connect()) {
            for (_i in 1..20) {
                Messenger.print("Сервис недоступен. Попытка переподключения...", TextColor.RED)
                if (reconnect()) { Messenger.print("Переподключение прошло успешно!"); break }

                Thread.sleep(1000)
            }

            if (!service.isConnected) {
                Messenger.print("Переподключение безуспешно. Завершение работы сервера...")
                return
            }
        }

        Messenger.print("Отправка запроса на регистрацию сервера...")
        while (true) {
            val numb = dbm.execute(GetLastChangesNumberQuery()) ?: continue
            try {
                val n = numb[0]["number"] as Long? ?: continue
                send(Request("Hello", "register_as_server", CommandArgument("$n persik")))
                break
            } catch (ex: Exception) {
                ex.printStackTrace()
                continue
            }
        }

        val execHandle = Executors.newCachedThreadPool()

        isRunning = true

        Thread {
            while (isRunning || msgQueue.isNotEmpty()) {
                val msg = msgQueue.peek()
                if (msg != null && !updatingInProgress) {
                    println(msg)
                    if (msg::class.java == DataBaseChanges::class.java) {
                        updatingInProgress = true
                    }
                    msgQueue.poll()
                    execHandle.execute {
                        msgHandler.process(msg)
                        updatingInProgress = false
                    }
                }
            }
        }.start()

        Thread {
            while (isRunning) {
                val msg = receiver.receive() ?: continue
                msgQueue.add(msg)
            }
        }.start()

        while (isRunning) {
            val cmd = readlnOrNull()?.trim()
            if (cmd == "exit" || cmd == null)
                isRunning = false
        }
    }

    private fun connect(): Boolean {
        service = Socket()
        sender = TCPStreamSenderWrapper(service)
        receiver = TCPStreamReceiverWrapper(service)

        try { service.connect(remote) } finally { }
        return if (service.isConnected) { true } else { finish(); false }
    }

    private fun reconnect(): Boolean {
        finish()
        return connect()
    }

    @Synchronized
    fun send(msg: Message) {
        while (true) {
            try {
                sender.send(msg)
                return
            } catch (ex: IOException) {
                Messenger.print("\nВо время отправки запроса связь с сервером была потеряна!", TextColor.RED)
                Messenger.print("Попытка переподключения...")
                if (!reconnect()) {
                    Messenger.print("Не удалось переподключиться к серверу. Выход из программы...", TextColor.RED)
                    exitProcess(6)
                }
                Messenger.print("Переподключение произошло успешно!")
            }
        }
    }

    fun receive(): Message? {
        while (true) {
            try {
                return receiver.receive()
            } catch (ex: IOException) {
                Messenger.print("\nВо время получения сообщения связь с сервисом была потеряна!", TextColor.RED)
                Messenger.print("Попытка переподключения...")
                if (!reconnect()) {
                    Messenger.print("Не удалось переподключиться к сервису. Выход из программы...", TextColor.RED)
                    exitProcess(6)
                }
                Messenger.print("Переподключение произошло успешно!")
            }
        }
    }

    private fun finish() {
        if (!service.isClosed) service.close()
    }
}