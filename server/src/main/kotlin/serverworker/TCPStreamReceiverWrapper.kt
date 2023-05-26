package serverworker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import message.Message
import network.receiver.AbstractReceiverWrapper
import network.receiver.ReceiverInterface
import network.receiver.TCPStreamReceiver
import message.Request
import java.net.Socket

class TCPStreamReceiverWrapper(
    sock: Socket,
): AbstractReceiverWrapper<Message>() {
    override val receiver: ReceiverInterface = TCPStreamReceiver(sock)

    override fun receive(): Message? {
        val msgString = receiver.receive()
        return deserialize(msgString)
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