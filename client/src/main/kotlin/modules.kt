import clientworker.ChannelClientWorker
import network.WorkerInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

val channelClientWorkerManager = module {
    single<WorkerInterface> { (port: Int, host: String) ->
        ChannelClientWorker(port, host)
    }

    single<WorkerInterface>(named("localhost")) { (port: Int) ->
        ChannelClientWorker(port, "localhost")
    }
}

