import worker.GatewayLBService

fun main(args: Array<String>) {
    Thread {
        val service = GatewayLBService(5555)
        service.start()
    }.start()
}