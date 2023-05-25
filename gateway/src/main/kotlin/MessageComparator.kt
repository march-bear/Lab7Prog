import message.Infarct
import message.Message

class MessageComparator : Comparator<Message> {
    override fun compare(o1: Message?, o2: Message?): Int {
        if (o1 == null) return 1
        if (o2 == null) return -1
        if (o1::class.java == Infarct::class.java) {
            o1 as Infarct
            if (o2::class.java == Infarct::class.java) {
                o2 as Infarct
                return o1.number.compareTo(o2.number)
            }
            return -1
        }
        return 0
    }
}