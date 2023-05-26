package clientworker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import message.Message
import network.receiver.AbstractReceiverWrapper
import network.receiver.ReceiverInterface
import network.receiver.TCPChannelReceiver
import message.Response
import java.nio.channels.SocketChannel

class TCPChannelReceiverWrapper(sock: SocketChannel): AbstractReceiverWrapper<Message>() {
    override val receiver: ReceiverInterface = TCPChannelReceiver(sock)

    override fun receive(): Message? {
        return deserialize(receiver.receive())
    }

    private fun deserialize(msg: String): Message? {
        return try {
            Json.decodeFromString<Message>(msg)
        } catch (ex: SerializationException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }
    }
}