import db.DataBaseManager
import exceptions.InvalidArgumentsForCommandException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import request.Request
import request.Response
import serverworker.LoggerWrapper

class CollectionController(
    private val dbManager: DataBaseManager,
    log: LoggerWrapper? = null,
) : KoinComponent {
    private val commandManager: CommandManager = get { parametersOf(dbManager, this) }

    init {
        dbManager.initTables()
    }

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
}