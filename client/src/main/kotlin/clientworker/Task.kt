package clientworker

class Task(
    private val task: ChannelClientWorker.() -> Unit,
) {
    private var executed = false
    fun execute(worker: ChannelClientWorker) {
        if (executed)
            throw Exception()
        worker.task()
    }

    fun clone(): Task = Task(task)
}