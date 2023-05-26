import message.*
import worker.GatewayLBService
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.PriorityBlockingQueue

fun main(args: Array<String>) {
    Thread {
        val service = GatewayLBService(5555)
        service.start()
    }.start()
}