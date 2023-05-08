package clientworker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import network.receiver.AbstractReceiverWrapper
import network.receiver.ReceiverInterface
import network.receiver.TCPChannelReceiver
import request.Response
import java.nio.channels.SocketChannel

class TCPChannelReceiverWrapper(sock: SocketChannel): AbstractReceiverWrapper<Response>() {
    override val receiver: ReceiverInterface = TCPChannelReceiver(sock)

    override fun receive(): Response? {
        return deserialize(receiver.receive())
    }

    private fun deserialize(msg: String): Response? {
        return try {
            Json.decodeFromString<Response>(msg)
        } catch (ex: SerializationException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }
    }
}