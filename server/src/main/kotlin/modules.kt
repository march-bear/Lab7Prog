import collection.CollectionWrapper
import command.ArgumentType
import command.ArgumentValidator
import command.Command
import commands.*
import db.manager.DataBaseManager
import db.manager.checkToken
import iostreamers.Messenger
import network.WorkerInterface
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import message.CommandInfo
import message.Request
import message.Response
import organization.Organization
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
                get() = "ꃅꍏꉓꀘ ꌩꂦꀎ (Это какой-то баг, не обращай внимание)"

            override fun execute(req: Request): Response {
                return Response(req.key, true, Messenger.whatThe())
            }
        }
    }
    factory<Command>(named("clear")) { (dbManager: DataBaseManager) -> ClearCommand(dbManager) }

    single<Command>(named("group_counting_by_employees_count")) {
            (collection: CollectionWrapper<Organization>) -> GroupCountingByEmployeesCountCommand(collection)
    }

    single<Command>(named("info")) {
            (collection: CollectionWrapper<Organization>) -> InfoCommand(collection)
    }

    single<Command>(named("print_unique_postal_address")) {
            (collection: CollectionWrapper<Organization>) -> PrintUniquePostalAddressCommand(collection)
    }

    single<Command>(named("show")) {
            (collection: CollectionWrapper<Organization>, dbManager: DataBaseManager) -> ShowCommand(collection, dbManager)
    }

    single<Command>(named("sum_of_employees_count")) {
            (collection: CollectionWrapper<Organization>) -> SumOfEmployeesCountCommand(collection)
    }

    factory<Command>(named("disconnect")) { (dbManager: DataBaseManager) -> DisconnectCommand(dbManager) }
    single<Command>(named("check_token")) { (dbManager: DataBaseManager) -> CheckTokenCommand(dbManager) }
    factory<Command>(named("log_in")) { (dbManager: DataBaseManager) -> LogInCommand(dbManager) }
    factory<Command>(named("register")) { (dbManager: DataBaseManager) -> RegisterCommand(dbManager) }

    single<Command>(named("check_connect")) {(dbManager: DataBaseManager) ->
        object : Command {
            override val info: String
                get() = "Проверить соединение"

            override val argumentValidator = ArgumentValidator(listOf(ArgumentType.TOKEN))

            override fun execute(req: Request): Response {
                argumentValidator.check(req.args)
                val validToken = dbManager.checkToken(req.args.token!!)
                return Response(
                    req.key,
                    true,
                    "Good connection! Запрос обработан: ${Date(System.currentTimeMillis())}",
                    if (validToken) null else "identify",
                )
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

    single {
            (
                dbManager: DataBaseManager,
                cController: CollectionController,
                collection: CollectionWrapper<Organization>
            ) ->
        CommandManager(commandModule, dbManager, cController, collection)
    }
}

