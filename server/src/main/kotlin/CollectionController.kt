import collection.CollectionWrapper
import exceptions.InvalidArgumentsForCommandException
import iostreamers.Messenger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import organization.Organization
import request.Request
import request.Response
import serverworker.LoggerWrapper

class CollectionController(
    log: LoggerWrapper? = null
) : KoinComponent {
    companion object {
        fun checkUniquenessFullName(fullName: String?, collection: CollectionWrapper<Organization>): Boolean {
            if (fullName == null)
                return true

            for (elem in collection)
                if (elem.fullName != null && elem.fullName == fullName)
                    return false
            return true
        }

        fun checkUniquenessId(id: Long, collection: CollectionWrapper<Organization>): Boolean {
            if (!Organization.idIsValid(id))
                return false

            for (elem in collection)
                if (elem.id == id)
                    return false
            return true
        }
    }

    private val collection: CollectionWrapper<Organization> by inject()
    private val commandManager: CommandManager by inject()

    @Synchronized
    fun process(req: Request) : Response {
        val command = commandManager.getCommand(req.name)
            ?: return Response(false, "${req.name}: неизвестная команда", req.key)

        return try {
            command.execute(req)
        } catch (ex: InvalidArgumentsForCommandException) {
            Response(false, ex.message ?: "", req.key)
        }
    }

    init {
        Messenger.print("Начало загрузки коллекции. Это может занять некоторое время...")


        Messenger.print("Загрузка коллекции завершена. Отчет о выполнении загрузки:")
        Messenger.print("---------------------------------------------------------------------")
        Messenger.print()
        Messenger.print("---------------------------------------------------------------------\n")
    }
}