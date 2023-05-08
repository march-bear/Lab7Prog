package network.receiver

abstract class AbstractReceiverWrapper<T> {
    protected abstract val receiver: ReceiverInterface
    abstract fun receive(): T?
}