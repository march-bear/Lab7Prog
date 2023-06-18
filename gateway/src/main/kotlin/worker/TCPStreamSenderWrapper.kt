package worker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import message.Message
import network.sender.AbstractSenderWrapper
import network.sender.SenderInterface
import network.sender.TCPStreamSender
import java.net.Socket

class TCPStreamSenderWrapper(sock: Socket) : AbstractSenderWrapper<Message>() {
    override val sender: SenderInterface = TCPStreamSender(sock)

    override fun send(msg: Message) {
        println(msg)
        val msgString = serialize(msg) ?: throw Exception()
        sender.send(msgString)
    }

    private fun serialize(msg: Message): String? {
        return try {
            Json.encodeToString<Message>(msg)
        } catch (ex: SerializationException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }
    }
}