import collection.CollectionWrapper
import command.Command
import command.implementations.HelpCommand
import command.implementations.LogInCommand
import command.implementations.RegisterCommand
import db.DataBaseManager
import iostreamers.Messenger
import network.WorkerInterface
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.named
import org.koin.dsl.module
import organization.Organization
import request.CommandInfo
import request.Request
import request.Response
import serverworker.LoggerWrapper
import serverworker.StreamServerWorker
import java.io.File
import java.util.*

val qualifiers = listOf<String>(
    "hack",
)

val commandModule = module {
    single<Command>(named("help")) {
            (_: CollectionWrapper<Organization>, _: CollectionController) ->
        val commandInfos = mutableListOf<CommandInfo>()
        for (qualifier in qualifiers) {
            val command = get<Command>(named(qualifier))
            commandInfos.add(CommandInfo(qualifier, command.info, command.argumentValidator.argumentTypes))
        }

        HelpCommand(commandInfos)
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

    factory<Command>(named("log_in")) {(dbManager: DataBaseManager) ->
        LogInCommand(dbManager)
    }

    factory<Command>(named("register")) {(dbManager: DataBaseManager, cController: CollectionController) ->
        RegisterCommand(dbManager, cController)
    }

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

