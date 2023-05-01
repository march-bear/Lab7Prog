package clientworker

import OrganizationFactory
import command.*
import exceptions.InvalidArgumentsForCommandException
import iostreamers.Messenger
import iostreamers.Reader
import iostreamers.TextColor
import network.WorkerInterface
import network.receiver.ReceiverInterface
import network.receiver.TCPChannelReceiver
import network.sender.SenderInterface
import network.sender.TCPChannelSender
import org.koin.core.component.KoinComponent
import request.CommandInfo
import request.Request
import request.Response
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.channels.SelectionKey
import java.nio.channels.SelectionKey.*
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.system.exitProcess

class ChannelClientWorker (
    serverPort: Int,
    serverHost: String,
) : WorkerInterface, KoinComponent {
    private val remote = InetSocketAddress(serverHost, serverPort)

    private var sock = SocketChannel.open()
    private val receiver: ReceiverInterface = TCPChannelReceiver(sock)
    private val sender: SenderInterface = TCPChannelSender(sock)

    private var selector = Selector.open()
    private val queue: BlockingQueue<Request> = ArrayBlockingQueue(3)
    private val tasks: ArrayList<Task> = ArrayList()

    val responses: Queue<Response> = LinkedList()
    val requestKeys: Queue<String> = LinkedList()
    val commandList: MutableList<CommandInfo> = mutableListOf()

    var user: String = ""
    var password: String = ""
    var token: String? = null

    override fun start() {
        if (!connect()) {
            for (_i in 1..20) {
                Messenger.print("Сервер недоступен. Попытка переподключения...", TextColor.RED)
                if (reconnect()) { Messenger.print("Переподключение прошло успешно!"); break }

                Thread.sleep(1000)
            }

            if (!sock.isConnected) {
                Messenger.print("Переподключение безуспешно. Завершение работы программы...")
                return
            }
        }

        Thread {
            while (true) {
                for (task in tasks) {
                    if (System.currentTimeMillis() - task.startTime >= task.waitingTime) {
                        task.execute(this)
                        tasks.remove(task)
                    }
                }
            }
        }

        Thread { select() }.start()
        Thread {
            while (true) {
                if (responses.isEmpty()) continue
                val key = requestKeys.poll()
                val response = responses.poll()
                if (response.requestKey != key) {
                    Messenger.print("Ответ сервера на запрос $key некорректен", TextColor.RED)
                } else {
                    Messenger.print("Ответ сервера на запрос $key:")
                    Messenger.print(response.message, if (response.success) TextColor.BLUE else TextColor.RED)
                }
            }
        }.start()

        interactiveMode()
        exitProcess(0)
    }

    private fun select() {
        while (true) {
            selector.select()

            val keys = selector.selectedKeys().iterator()

            for (key in keys) {
                if (key.isValid && key.isWritable && queue.isNotEmpty()) {
                    val request = queue.take()
                    requestKeys.add(request.key)
                    send(request)

                    key.interestOps(OP_READ)
                }

                if (key.isValid && key.isReadable && requestKeys.isNotEmpty()) {
                    val response = receive()
                    if (response != null) {
                        responses.add(response)
                        key.interestOps(OP_WRITE)
                    }
                }

                if (key.isValid) keys.remove() else break
            }
        }
    }

    private fun waitForResponse(): Response {
        val time = System.currentTimeMillis()
        while (System.currentTimeMillis() - time < MAX_RESPONSE_TIME)
            if (responses.isNotEmpty())
                break
            else
                Thread.sleep(200)

        return if (responses.isNotEmpty())
            responses.poll()
        else {
            Messenger.print("Сервер не отвечает. Отключение...")
            exitProcess(1)
        }
    }

    private fun send(req: Request) {
        while (true) {
            try {
                sender.send(req)
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

    private fun receive(): Response? {
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
                val commandInfo = commandList.stream().filter { it.name == name }.findFirst().get()
                if (ArgumentType.ORGANIZATION in commandInfo.args)
                    args.setOrganization(orgFactory.newOrganizationFromInput())
                if (ArgumentType.SCRIPT in commandInfo.args)
                    null

                argValidatorFactory.getByCommandName(name)!!.check(args)
                val key = generateKey()
                queue.add(Request(name, key, args, user, token ?: ""))
                requestKeys.add(key)
                selector.wakeup()
            } catch (ex: NullPointerException) {
                Messenger.print("$name: команда не найдена", TextColor.RED)
            } catch (ex: NoSuchElementException) {
                Messenger.print("$name: команда не найдена", TextColor.RED)
            } catch (ex: InvalidArgumentsForCommandException) {
                Messenger.print(ex.message, TextColor.RED)
            } catch (ex: java.lang.IllegalArgumentException) {
                Messenger.print(ex.message, TextColor.RED)
            }
        } while (name != "exit")
    }

    private fun connect(): Boolean {
        sock = SocketChannel.open()
        selector = Selector.open()

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

        return if (sock.isConnected) { true } else { finish(); false }
    }

    private fun reconnect(): Boolean {
        finish()
        return connect()
    }

    private fun finish() {
        if (selector.isOpen) selector.close()
        if (sock.isOpen) sock.close()
    }

    fun addTask()

    private fun userInput(): Pair<String, String> {
        val r = Reader()
        Messenger.print("Введите логин и пароль:")

        Messenger.inputPrompt("логin")
        var login: String?
        while (true) {
            login = r.readStringOrNull()
            if (login == null) {
                Messenger.print("Введите л0гин: ", TextColor.RED, false)
                continue
            }

            break
        }

        Messenger.inputPrompt("паролb")
        var passwd: String?
        while (true) {
            passwd = r.readStringOrNull()
            if (passwd == null) {
                Messenger.print("Введите пар0ль: ", TextColor.RED, false)
                continue
            }

            break
        }

        return Pair(login!!, passwd!!)
    }

    fun addTask(task: Task) {
        tasks.add(task)
    }

    companion object {
        private const val MAX_RESPONSE_TIME = 10000L
        private const val ALPHA_FOR_KEYS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][;'.,/&^%#"

        fun generateKey(): String {
            var key = ""
            for (_i in 1..10)
                key += ALPHA_FOR_KEYS[(Math.random() * ALPHA_FOR_KEYS.length).toInt()]

            return key
        }
    }
}