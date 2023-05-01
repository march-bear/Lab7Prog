package clientworker

class Task(
    private val task: ChannelClientWorker.() -> Unit,
    val startTime: Long = System.currentTimeMillis(),
    val waitingTime: Long = 0,
) {
    private var executed = false
    fun execute(worker: ChannelClientWorker) {
        if (executed || System.currentTimeMillis() - startTime < waitingTime)
            throw Exception()
        worker.task()
    }
}