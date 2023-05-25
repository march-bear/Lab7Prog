import message.*
import java.util.concurrent.PriorityBlockingQueue

fun main(args: Array<String>) {
    val queue = PriorityBlockingQueue(11, MessageComparator())
    queue.put(Request("3", ""))
    queue.put(Infarct("1", 1L, listOf()))
    queue.put(Infarct("2", 2L, listOf()))

    println(queue.take().key)
    println(queue.take().key)
    println(queue.take().key)
}