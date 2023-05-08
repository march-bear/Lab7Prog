package network.sender

abstract class AbstractSenderWrapper<T> {
    protected abstract val sender: SenderInterface
    abstract fun send(msg: T)
}