package serverworker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import network.receiver.AbstractReceiverWrapper
import network.receiver.ReceiverInterface
import network.receiver.TCPStreamReceiver
import request.Request
import java.net.Socket

class TCPStreamReceiverWrapper(
    sock: Socket,
): AbstractReceiverWrapper<Request>() {
    override val receiver: ReceiverInterface = TCPStreamReceiver(sock)

    override fun receive(): Request? {
        val msgString = receiver.receive()
        return deserialize(msgString)
    }

    private fun deserialize(msg: String): Request? {
        return try {
            Json.decodeFromString<Request>(msg)
        } catch (ex: SerializationException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }
    }
}