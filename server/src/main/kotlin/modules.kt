import command.Command
import command.implementations.*
import db.DataBaseManager
import iostreamers.Messenger
import network.WorkerInterface
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import request.CommandInfo
import request.Request
import request.Response
import serverworker.LoggerWrapper
import serverworker.StreamServerWorker
import java.util.*

val qualifiers = listOf(
    "add",
    "hack",
    "disconnect",
    "group_counting_by_employees_count",
    "clear",
)

val commandModule = module {
    single<Command>(named("help")) {
            (dbManager: DataBaseManager, cController: CollectionController) ->
        val commandInfos = mutableListOf<CommandInfo>()
        for (qualifier in qualifiers) {
            val command = get<Command>(named(qualifier)) { parametersOf(dbManager, cController) }
            commandInfos.add(CommandInfo(qualifier, command.info, command.argumentValidator.argumentTypes))
        }

        HelpCommand(commandInfos)
    }

    factory<Command>(named("add")) {
        (dbManager: DataBaseManager) -> AddCommand(dbManager)
    }

    single<Command>(named("hack")) {
        object : Command {
            override val info: String
                get() = "hack you"

            override fun execute(req: Request): Response {
                return Response(true, Messenger.whatThe(), req.key)
            }
        }
    }
    factory<Command>(named("clear")) { (dbManager: DataBaseManager) -> ClearCommand(dbManager) }

    factory<Command>(named("group_counting_by_employees_count")) {
            (dbManager: DataBaseManager) -> GroupCountingByEmployeesCountCommand(dbManager)
    }

    factory<Command>(named("disconnect")) { (dbManager: DataBaseManager) -> DisconnectCommand(dbManager) }
    single<Command>(named("check_token")) { (dbManager: DataBaseManager) -> CheckTokenCommand(dbManager) }
    factory<Command>(named("log_in")) { (dbManager: DataBaseManager) -> LogInCommand(dbManager) }
    factory<Command>(named("register")) { (dbManager: DataBaseManager) -> RegisterCommand(dbManager) }

    single<Command>(named("check_connect")) {
        object : Command {
            override val info: String
                get() = "Проверить соединение"

            override fun execute(req: Request): Response {
                return Response(true, "Good connection! Запрос обработан: ${Date(System.currentTimeMillis())}", req.key, "identify")
            }
        }
    }
}

operator fun <T> ParametersHolder.component6(): T = get(5)

val serverWorkerModule = module {
    single<WorkerInterface> {
            (
                port: Int,
                dbHost: String,
                dbPort: Int,
                dbName: String,
                dbUserName: String,
                dbUserPassword: String,
            ) ->
        StreamServerWorker(port, dbHost, dbPort, dbName, dbUserName, dbUserPassword)
    }
}

val collectionControllerModule = module {
    single {(dbManager: DataBaseManager) ->
        CollectionController(dbManager)
    }

    factory {
            (
                host: String,
                port: Int,
                name: String,
                user: String,
                passwd: String,
            ) ->
        DataBaseManager(host, port, name, user, passwd)
    }

    single(named("logging")) {(dbManager: DataBaseManager, log: LoggerWrapper) ->
        CollectionController(dbManager, log)
    }

    single {(dbManager: DataBaseManager, cController: CollectionController) ->
        CommandManager(commandModule, dbManager, cController)
    }
}

