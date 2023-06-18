import iostreamers.*
import network.WorkerInterface
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

fun main(args: Array<String>) {
    val app = startKoin {
        modules(
            serverWorkerModule,
            collectionControllerModule,
        )
    }

    val r = Reader()

    Messenger.inputPrompt("Введите номер типа сервера (1 - автономный, 2 - подключаемый GatewayLBService)")
    val sType = r.readOneOfValues(listOf("1", "2"))

    var port: Int = -1
    if (sType == "1") {
        Messenger.print("Уже недоступно", TextColor.RED)
        return
        // Messenger.inputPrompt("Введите порт для работы сервера")
        // port = r.readPort(true) ?: return
    }

    Messenger.print("\nВведите данные для подключения к базе данных")
    Messenger.inputPrompt("Хост")
    val dbHost = r.readHost() ?: return
    Messenger.inputPrompt("Порт")
    val dbPort = r.readPort() ?: return
    Messenger.inputPrompt("Название БД")
    val dbName = r.readNotEmptyString() ?: return
    Messenger.inputPrompt("Имя пользователя")
    val dbUserName = r.readNotEmptyString() ?: return
    Messenger.inputPrompt("Пароль")
    val dbUserPassword = r.readNotEmptyString() ?: return

    val worker: WorkerInterface
    Messenger.print("\nВведите данные для подключения к GatewayLBService")
    Messenger.inputPrompt("Хост")
    val host = r.readHost() ?: return
    Messenger.inputPrompt("Порт")
    port = r.readPort() ?: return

    worker = app.koin.get(named("connectable")) {
            parametersOf(
                host,
                port,
                dbHost,
                dbPort,
                dbName,
                dbUserName,
                dbUserPassword,
            )
        }

    worker.start()
}
