package collection

import kotlinx.serialization.Serializable
import serializers.LinkedListSerializer
import java.util.LinkedList
import java.util.stream.Stream

@Serializable
class LinkedListWrapper<E>(
    @Serializable(with = LinkedListSerializer::class) private val linkedList: LinkedList<E> = LinkedList(),
) : CollectionWrapperInterface<E> {
    override val size: Int
        get() = linkedList.size

    override fun add(element: E): Boolean = linkedList.add(element)

    override fun replace(curr: E, new: E) {
        linkedList[linkedList.indexOf(curr)] = new
    }

    override fun replaceBy(new: E, predicate: (E) -> Boolean) {
        linkedList[
                linkedList.indexOf(
                    linkedList.find(predicate)
                )
        ] = new
    }

    override fun isEmpty(): Boolean = linkedList.isEmpty()

    override fun clear() = linkedList.clear()

    override fun remove(): E = linkedList.remove()

    override fun iterator(): Iterator<E> = linkedList.iterator()

    override fun remove(element: E): Boolean = linkedList.remove(element)

    override fun getCollectionName(): String = "LinkedList"

    override fun getCollectionType(): CollectionType = CollectionType.LIST

    override fun clone(): CollectionWrapperInterface<E> {
        val linkedListCopy = LinkedListWrapper<E>()
        linkedListCopy.addAll(linkedList)
        return linkedListCopy
    }

    override fun stream(): Stream<E> {
        return linkedList.stream()
    }
}