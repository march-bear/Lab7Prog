import command.Command
import db.DataBaseManager
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class CommandManager(
    private val module: Module,
    private val dbManager: DataBaseManager,
    private val controller: CollectionController,
) {
    private val koinApp = koinApplication {
        modules(module, module { single { this@CommandManager } })
    }

    fun getCommand(name: String): Command? = try {
            koinApp.koin.get(named(name)) { parametersOf(dbManager, controller) }
        } catch (ex: NoBeanDefFoundException) {
            null
        }
}