package collection

import kotlinx.serialization.Serializable
import serializers.DateAsLongSerializer
import java.lang.IllegalArgumentException
import java.util.Date
import java.util.function.Predicate
import java.util.stream.Stream

@Serializable
class CollectionWrapper<E>(private var collection: CollectionWrapperInterface<E>) : CollectionWrapperInterface<E> {
    override val size: Int
        get() = collection.size

    override fun isEmpty(): Boolean = collection.isEmpty()

    override fun clear() = collection.clear()

    override fun remove(): E = collection.remove()
    override fun removeIf(filter: Predicate<in E>): Boolean = collection.removeIf(filter)

    override fun getCollectionName(): String = collection.getCollectionName()

    override fun remove(element: E): Boolean = collection.remove(element)

    override fun replace(curr: E, new: E) = collection.replace(curr, new)

    override fun replaceBy(new: E, predicate: (E) -> Boolean) = collection.replaceBy(new, predicate)

    override fun add(element: E): Boolean = collection.add(element)

    override fun iterator(): Iterator<E> = collection.iterator()

    fun replaceCollectionWrapper(wrapper: CollectionWrapperInterface<E>) {
        if (wrapper::class == this::class)
            throw IllegalArgumentException("значением поля collection не может быть объект класса" +
                    "CollectionWrapper")

        val tmpCollection = collection
        collection = wrapper.clone()
        collection.addAll(tmpCollection)
    }

    override fun clone(): CollectionWrapperInterface<E> {
        return CollectionWrapper(collection.clone())
    }

    override fun getCollectionType(): CollectionType = collection.getCollectionType()

    override fun stream(): Stream<E> = collection.stream()
}