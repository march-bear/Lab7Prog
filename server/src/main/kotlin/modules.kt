import collection.CollectionWrapper
import collection.LinkedListWrapper
import command.Command
import command.implementations.HelpCommand
import org.koin.core.qualifier.named
import org.koin.dsl.module
import organization.Organization
import request.CommandInfo
import request.Request
import request.Response
import serverworker.LoggerWrapper
import serverworker.StreamServerWorker
import java.io.File

val qualifiers = listOf<String>(
    "hack",
    "check"
)

val commandModule = module {
    single<Command>(named("help")) {
            (collection: CollectionWrapper<Organization>, controller: CollectionController) ->
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
                return Response(true, "", req.key)
            }

            override fun cancel(): String {
                return ""
            }

        }
    }

    single<Command>(named("check")) {
        object : Command {
            override val info: String
                get() = "check connection"

            override fun execute(req: Request): Response {
                return Response(true, "Good connection!", req.key, "identify getCommandInfo")
            }

            override fun cancel(): String {
                return ""
            }

        }
    }
}

val serverWorkerModule = module {
    single(named("withFile")) {(port: Int, fileName: String) ->
        StreamServerWorker(port, fileName)
    }

    single {(port: Int) ->
        StreamServerWorker(port)
    }
}

val collectionControllerModule = module {
    single {
        CollectionWrapper(LinkedListWrapper<Organization>())
    }

    single {
        CollectionController()
    }

    single(named("logging")) {(log: LoggerWrapper) ->
        CollectionController(log)
    }

    single {
        println("вот так вот")
        CommandManager(get(), get(), commandModule)
    }
}

