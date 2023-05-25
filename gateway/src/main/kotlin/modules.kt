import command.Command
import message.Request
import message.Response
import org.koin.core.qualifier.named
import org.koin.dsl.module

val registeringNewServerModule = module {
    single<Command>(named("registerLikeServer")) {
        object : Command {
            override val info: String = "зарегистрировать подключенный сокет как сервер"

            override fun execute(req: Request): Response {
                return Response("", true, "")
            }
        }
    }
}