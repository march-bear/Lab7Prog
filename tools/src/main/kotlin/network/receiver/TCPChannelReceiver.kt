package network.receiver

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import request.Response
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class TCPChannelReceiver(private val sock: SocketChannel): ReceiverInterface {
    private val msgLenBuf = ByteBuffer.allocate(4)
    private val msgBuf = ByteBuffer.allocate(65536)

    override fun receive(): Response? {
        msgLenBuf.clear()
        sock.read(msgLenBuf)
        val len = msgLenBuf.int

        msgBuf.clear()
        msgBuf.limit(len)
        sock.read(msgBuf)
        msgBuf.flip()

        val msgArr = ByteArray(msgBuf.remaining())
        msgBuf.get(msgArr)
        val response = String(msgArr)

        return ReceiverInterface.deserialize(response)
    }
}