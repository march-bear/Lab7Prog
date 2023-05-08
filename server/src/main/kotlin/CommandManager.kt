import collection.CollectionWrapper
import command.Command
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import organization.Organization

class CommandManager(
    private val collection: CollectionWrapper<Organization>,
    private val controller: CollectionController,
    private val module: Module,
) {
    private val koinApp = koinApplication {
        modules(module, module { single { this@CommandManager } })
    }

    fun getCommand(name: String): Command? = try {
            koinApp.koin.get(named(name)) { parametersOf(collection, controller) }
        } catch (ex: NoBeanDefFoundException) {
            null
        }
}