import iostreamers.*
import network.WorkerInterface
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.parameter.parametersOf
import serverworker.StreamServerWorker

fun main(args: Array<String>) {
    val app = startKoin {
        modules(
            serverWorkerModule,
            collectionControllerModule,
        )
    }

    val r = Reader()

    Messenger.inputPrompt("Введите порт для работы сервера")
    val port = r.readPort(true) ?: return

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

    val worker = app.koin.get<WorkerInterface> { parametersOf(
        port,
        dbHost,
        dbPort,
        dbName,
        dbUserName,
        dbUserPassword,
    ) }

    worker.start()
}
