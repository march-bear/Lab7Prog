import clientworker.ChannelClientWorker
import network.WorkerInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

/*
import clientworker.ChannelClientWorker
import command.*
import command.implementations.*
import network.WorkerInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.collections.HashMap

val commandQualifiers = listOf(
    "info", "show", "add", "update",
    "remove_by_id", "clear",
    "exit", "remove_head",
    "add_if_max", "remove_lower",
    "sum_of_employees_count", "oops",
    "group_counting_by_employees_count",
    "print_unique_postal_address",
    "show_field_requirements",
)

val clientCommandModule = module {
    single<Command>(named("help")) {
        val commandsMap = HashMap<String, String>()
        commandsMap["help"] = HelpCommand(mapOf()).info
        for (qualifier in commandQualifiers) {
            commandsMap[qualifier] = this.getKoin().get<Command>(named(qualifier)).info
        }
        HelpCommand(commandsMap)
    }

    single<Command>(named("info")) { InfoCommand() }
    single<Command>(named("show")) { ShowCommand() }
    single<Command>(named("add")) { AddCommand() }
    single<Command>(named("update")) { UpdateCommand() }
    single<Command>(named("remove_by_id")) { RemoveByIdCommand() }
    single<Command>(named("clear")) { ClearCommand() }
    single<Command>(named("exit")) { ExitCommand() }
    single<Command>(named("remove_head")) { RemoveHeadCommand() }
    single<Command>(named("add_if_max")) { AddIfMaxCommand() }
    single<Command>(named("remove_lower")) { RemoveLowerCommand() }
    single<Command>(named("sum_of_employees_count")) { SumOfEmployeesCountCommand() }
    single<Command>(named("group_counting_by_employees_count")) { GroupCountingByEmployeesCountCommand() }
    single<Command>(named("print_unique_postal_address")) { PrintUniquePostalAddressCommand() }
    single<Command>(named("oops")) { HackSystemCommand() }
    single<Command>(named("show_field_requirements")) { ShowFieldRequirementsCommand() }
}

val clientCommandManagerModule = module {
    single {
        clientCommandModule
    }

    single {
        CommandManager(get())
    }
}
*/
val channelClientWorkerManager = module {
    single<WorkerInterface> { (port: Int, host: String) ->
        ChannelClientWorker(port, host)
    }

    single<WorkerInterface>(named("localhost")) { (port: Int) ->
        ChannelClientWorker(port, "localhost")
    }
}

