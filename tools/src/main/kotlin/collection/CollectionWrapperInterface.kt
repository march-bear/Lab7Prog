package collection

import kotlinx.serialization.Serializable
import java.util.stream.Stream

@Serializable
sealed interface CollectionWrapperInterface<E>: Iterable<E> {
    val size: Int

    fun add(element: E): Boolean

    fun replaceBy(new: E, predicate: (E) -> Boolean)

    fun replace(curr: E, new: E)

    fun isEmpty(): Boolean

    fun clear()

    fun remove(): E

    fun remove(element: E): Boolean

    fun addAll(iterable: Iterable<E>) {
        for (elem in iterable)
            this.add(elem)
    }

    fun getCollectionName(): String

    fun getCollectionType(): CollectionType

    fun clone(): CollectionWrapperInterface<E>

    fun stream(): Stream<E>
}