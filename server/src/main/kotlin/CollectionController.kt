import collection.CollectionWrapper
import collection.LinkedListWrapper
import command.CommandResult
import commands.UpdateLocalCollectionCommand
import db.manager.DataBaseManager
import exceptions.InvalidArgumentsForCommandException
import iostreamers.Messenger
import message.*
import message.handler.HandlerException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import organization.Organization

class CollectionController(
    private val dbManager: DataBaseManager,
    log: LoggerWrapper? = null,
) : KoinComponent {
    private val collection = CollectionWrapper<Organization>(LinkedListWrapper())
    private val commandManager: CommandManager = get { parametersOf(dbManager, this, collection) }

    init {
        Messenger.print("Инициализация таблиц...")
        dbManager.initTables()
        Messenger.print("Загрузка коллекции...")
        UpdateLocalCollectionCommand(collection, dbManager).execute(Request("", ""))
    }

    fun process(req: Request) : CommandResult {
        val command = commandManager.getCommand(req.name)
            ?: return CommandResult(Response(req.key, false, "${req.name}: неизвестная команда"))

        return try {
            command.execute(req)
        } catch (ex: InvalidArgumentsForCommandException) {
            CommandResult(Response(req.key, false, ex.message ?: ""))
        }
    }

    @Synchronized
    fun updateLocalCollection(inf: DataBaseChanges) {
        for ((type, args) in inf.changes) {
            when (type) {
                ChangeType.INSERT -> {
                    val value = args.organization ?: throw HandlerException("Среди серверов есть самозванец")
                    collection.add(value)
                }

                ChangeType.UPDATE -> {
                    val id = args.primArgs[0].toLong()
                    val newValue = args.organization ?: throw HandlerException("Среди серверов есть самозванец")
                    collection.replaceBy(newValue) { it.id == id }
                }

                ChangeType.DELETE -> {
                    val id = args.primArgs[0].toLong()
                    collection.removeIf { it.id == id }
                }
            }
        }
    }
}