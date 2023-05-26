package clientworker

import OrganizationFactory
import Task
import command.*
import exceptions.InvalidArgumentsForCommandException
import iostreamers.Messenger
import iostreamers.Reader
import iostreamers.TextColor
import network.WorkerInterface
import network.receiver.AbstractReceiverWrapper
import network.sender.AbstractSenderWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import message.CommandInfo
import message.Message
import message.Request
import message.Response
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.BufferUnderflowException
import java.nio.channels.SelectionKey
import java.nio.channels.SelectionKey.*
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*
import kotlin.system.exitProcess

class ChannelClientWorker (
    serverPort: Int,
    serverHost: String,
) : WorkerInterface, KoinComponent {
    private val remote = InetSocketAddress(serverHost, serverPort)

    private lateinit var sock: SocketChannel
    private lateinit var receiver: AbstractReceiverWrapper<Message>
    private lateinit var sender: AbstractSenderWrapper<Message>

    private val commandList: MutableList<CommandInfo> = mutableListOf()
    private val localCommandList: MutableList<CommandInfo> = mutableListOf()
    internal var token: String? = null

    override fun start() {
        if (!connect()) {
            for (_i in 1..20) {
                Messenger.print("Сервер недоступен. Попытка переподключения...", TextColor.RED)
                if (reconnect()) { Messenger.print("Переподключение прошло успешно!"); break }

                Thread.sleep(1000)
            }

            if (!sock.isConnected) {
                Messenger.print("Переподключение безуспешно. Завершение работы клиента...")
                return
            }
        }

        Messenger.print("Проверка соединения...")
        get<Task<ChannelClientWorker>>(named("checkConnect")).execute(this)

        interactiveMode()
        exitProcess(0)
    }

    private fun send(msg: Message) {
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

    private fun receive(): Message? {
        while (true) {
            try {
                return receiver.receive()
            } catch (_: IOException) {
                Messenger.print("\nВо время получения запроса связь с сервером была потеряна!", TextColor.RED)
                Messenger.print("Попытка переподключения...")
                if (!reconnect()) {
                    Messenger.print("Не удалось переподключиться к серверу. Выход из программы...", TextColor.RED)
                    exitProcess(6)
                }
                Messenger.print("Переподключение произошло успешно!")
            }
        }
    }

    internal fun sendAndReceive(req: Request): Message? {
        send(req)
        var response: Message?
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime <= MAX_RESPONSE_TIME) {
            try {
                response = receive()
                if (response != null)
                    return response
            } catch (_: BufferUnderflowException) { }
        }

        return null
    }


    private fun interactiveMode() {
        val reader = Reader(Scanner(System.`in`))
        val orgFactory = OrganizationFactory()
        val argValidatorFactory = ArgumentValidatorFactory(commandList)

        Messenger.interactiveModeMessage()
        Messenger.print(
            "\nДобро пожаловать в интерактивный режим! Для просмотра доступных команд введите `help`"
        )

        do {
            Messenger.inputPrompt(">>>", " ")
            val (name, args) = try { reader.readCommand()!! } catch (ex: NullPointerException) { break }
            try {
                if {}

                else {
                    val commandInfo = commandList.stream().filter { it.name == name }.findFirst().get()
                    if (ArgumentType.ORGANIZATION in commandInfo.args)
                        args.setOrganization(orgFactory.newOrganizationFromInput())

                    argValidatorFactory.getByCommandName(name)!!.check(args)
                    val key = generateKey()
                    val response = sendAndReceive(Request(name, key, args))
                    if (response != null)
                        processMessage(response, key)
                }
            } catch (ex: NullPointerException) {
                Messenger.print("$name: команда не найдена", TextColor.RED)
            } catch (ex: NoSuchElementException) {
                Messenger.print("$name: команда не найдена", TextColor.RED)
            } catch (ex: InvalidArgumentsForCommandException) {
                Messenger.print(ex.message, TextColor.RED)
            } catch (ex: java.lang.IllegalArgumentException) {
                Messenger.print(ex.message, TextColor.RED)
            }
        } while (name != "exit" || args.primArgs.isNotEmpty())
    }

    private fun connect(): Boolean {
        sock = SocketChannel.open()
        receiver = TCPChannelReceiverWrapper(sock)
        sender = TCPChannelSenderWrapper(sock)

        val selector = Selector.open()
        sock.configureBlocking(false)
        sock.register(selector, OP_CONNECT)
        sock.connect(remote)

        selector.select()

        val keys: MutableIterator<SelectionKey> = selector.selectedKeys().iterator()
        for (key in keys) {
            keys.remove()

            if (key.isConnectable) {
                try {
                    sock.finishConnect()
                    key.interestOps(OP_WRITE)
                    break
                } catch (_: SocketException) { }
            }
        }

        selector.close()

        return if (sock.isConnected) { true } else { finish(); false }
    }

    private fun reconnect(): Boolean {
        finish()
        return connect()
    }

    private fun finish() {
        if (sock.isOpen) sock.close()
    }

    fun updateCommandList(newList: List<CommandInfo>) {
        commandList.clear()
        commandList.addAll(newList)
    }

    fun processMessage(msg: Message, requestKey: String) {
        if (msg::class.java != Response::class.java) {
            Messenger.print("Сообщение, полученное от сервера, не может быть обработано", TextColor.RED)
            return
        }

        msg as Response
        if (msg.key == requestKey) {
            Messenger.print(msg.message, if (msg.success) TextColor.BLUE else TextColor.RED)
            for (task in msg.necessaryTask?.split(' ') ?: listOf())
                get<Task<ChannelClientWorker>>(named(task)).execute(this)
        } else {
            Messenger.print("Ответ сервера некорректен", TextColor.RED)
        }
    }

    fun getCommandList(): List<CommandInfo> = ArrayList(commandList)

    companion object {
        const val MAX_RESPONSE_TIME = 10000L
        private const val ALPHA_FOR_KEYS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][;'.,/&^%#"

        fun generateKey(): String {
            var key = ""
            for (_i in 1..10)
                key += ALPHA_FOR_KEYS[(Math.random() * ALPHA_FOR_KEYS.length).toInt()]

            return key
        }
    }
}