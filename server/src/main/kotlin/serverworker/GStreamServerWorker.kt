package serverworker

import CollectionController
import LoggerWrapper
import db.manager.DataBaseManager
import iostreamers.Messenger
import iostreamers.TextColor
import network.WorkerInterface
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

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

    private val cController: CollectionController = get(named("logging")) {
        parametersOf(DataBaseManager(dbHost, dbPort, dbName, dbUser, dbPasswd), log)
    }

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

    }

    private fun connect(): Boolean {
        service = Socket()
        try { service.connect(remote) } finally { }
        return if (service.isConnected) { true } else { finish(); false }
    }

    private fun reconnect(): Boolean {
        finish()
        return connect()
    }

    private fun finish() {
        if (!service.isClosed) service.close()
    }
}