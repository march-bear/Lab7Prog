import clientworker.executeCommandTasks
import clientworker.startTasks
import iostreamers.*
import network.WorkerInterface
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf

fun main(args: Array<String>) {
    val app = startKoin {
        modules(
            channelClientWorkerManager,
            executeCommandTasks,
            startTasks,
        )
    }

    val r = Reader()

    Messenger.print("Введите данные сервера")
    Messenger.inputPrompt("Порт")
    val port = r.readPort() ?: return
    Messenger.inputPrompt("Хост")
    val host = r.readHost() ?: return

    val worker: WorkerInterface = app.koin.get { parametersOf(port, host) }

    worker.start()
}