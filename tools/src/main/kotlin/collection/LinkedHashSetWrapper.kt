package collection

import kotlinx.serialization.Serializable
import java.util.stream.Stream

@Serializable
class LinkedHashSetWrapper<E>(private val set: LinkedHashSet<E> = LinkedHashSet()): CollectionWrapperInterface<E> {
    override val size: Int
        get() = set.size

    override fun replace(curr: E, new: E) {
        set.remove(curr); set.add(new)
    }

    override fun replaceBy(new: E, predicate: (E) -> Boolean) {
        set.remove(set.find(predicate))
    }

    override fun isEmpty(): Boolean = set.isEmpty()

    override fun clear() = set.clear()

    override fun remove(): E {
        val first = set.first()
        set.remove(first)
        return first
    }

    override fun getCollectionName(): String = "LinkedHashSet"
    override fun getCollectionType(): CollectionType = CollectionType.SET
    override fun clone(): CollectionWrapperInterface<E> {
        val setCopy = LinkedHashSetWrapper<E>()
        setCopy.addAll(set)
        return setCopy
    }

    override fun remove(element: E): Boolean = set.remove(element)

    override fun add(element: E): Boolean = set.add(element)

    override fun iterator(): Iterator<E> = set.iterator()

    override fun stream(): Stream<E> {
        return set.stream()
    }
}