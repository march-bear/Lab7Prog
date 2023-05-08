import iostreamers.Messenger
import iostreamers.Reader
import iostreamers.TextColor
import network.WorkerInterface
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.parameter.parametersOf
import serverworker.StreamServerWorker

fun main(args: Array<String>) {
    //val db = DataBaseManager("localhost", 5433, "studs", "s368494", "VfBSXmTeGhBMtn4g")
    //db.initTables()

    val app = startKoin {
        modules(
            serverWorkerModule,
            collectionControllerModule,
        )
    }

    val reader = Reader()
    var worker: WorkerInterface
    when (args.size) {
        0 -> {
            Messenger.print("Введите порт и название файла")
            Messenger.inputPrompt("Название файла")
            val fileName = reader.readStringOrNull()
            Messenger.inputPrompt("Порт")
            var port: Int
            while (true) {
                try {
                    port = reader.readString().toInt()
                    if (port < 0 || port > 65535) throw NumberFormatException()
                    worker = app.koin.get<StreamServerWorker> { parametersOf(port, fileName) }
                    break
                } catch (ex: NumberFormatException) {
                    Messenger.print("Введите целое число от 0 до 65535: ", TextColor.RED, false)
                }
            }
        }
        1 -> {
            val port: Int
            try {
                port = args[0].toInt()
                if (port < 0 || port > 65535) throw NumberFormatException()
                worker = app.koin.get<StreamServerWorker> { parametersOf(port, null) }
            } catch (ex: NumberFormatException) {
                Messenger.print("Для определения порта нужно ввести целое число от 0 до 65535: ", TextColor.RED)
                return
            }
        }
        2 -> {
            val port: Int
            try {
                port = args[0].toInt()
                if (port < 0 || port > 65535) throw NumberFormatException()
                val fileName = args[1]
                worker = app.koin.get<StreamServerWorker> { parametersOf(port, fileName) }
            } catch (ex: NumberFormatException) {
                Messenger.print("Для определения порта нужно ввести целое число от 0 до 65535: ", TextColor.RED)
                return
            }
        }
        else -> {
            Messenger.print(
                "Создать сервер можно:\n" +
                        "1) указав в качестве аргументов порт и путь к файлу с коллекцией\n" +
                        "2) указав к качестве аргумента только порт (путь будет взят по умолчанию)\n" +
                        "3) введя название файла и порт после запуска приложения, не указывая аргументы", TextColor.YELLOW)
            return
        }
    }

    worker.start()
}
