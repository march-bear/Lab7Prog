class Task<T>(
    private val task: T.() -> Unit,
) {
    private var executed = false
    fun execute(receiver: T) {
        if (executed)
            throw Exception()
        receiver.task()
    }
}