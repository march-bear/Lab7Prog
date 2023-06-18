import command.Command
import org.koin.core.qualifier.named
import org.koin.dsl.module
import worker.GatewayLBService
import worker.RegisterAsServerCommand

val registeringNewServerModule = module {
    factory<Command>(named("register_as_server")) {(service: GatewayLBService, id: String) ->
        RegisterAsServerCommand(service, id)
    }
}