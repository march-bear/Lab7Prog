import command.Command
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import worker.GatewayLBService

class CommandManager(
    private val module: Module,
    private val service: GatewayLBService,
) : AbstractCommandManager() {

    private val koinApp = koinApplication {
        modules(module)
    }

    override fun getCommand(name: String): Command? = try {
        koinApp.koin.get(named(name)) { parametersOf(service) }
    } catch (ex: NoBeanDefFoundException) {
        null
    }

    override fun getCommandForUser(name: String, username: String): Command? = try {
        koinApp.koin.get(named(name)) { parametersOf(service, username) }
    } catch (ex: NoBeanDefFoundException) {
        null
    }
}