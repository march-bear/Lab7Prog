import clientworker.ChannelClientWorker
import network.WorkerInterface
import org.koin.dsl.module

val channelClientWorkerManager = module {
    single<WorkerInterface> { (port: Int, host: String) ->
        ChannelClientWorker(port, host)
    }
}

