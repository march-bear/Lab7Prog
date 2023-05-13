package network.receiver

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class TCPChannelReceiver(private val sock: SocketChannel): ReceiverInterface {
    private val msgLenBuf = ByteBuffer.allocate(4)
    private val msgBuf = ByteBuffer.allocate(65536)

    override fun receive(): String {
        msgLenBuf.clear()
        while (sock.read(msgLenBuf) > 0) {}
        msgLenBuf.flip()
        val len = msgLenBuf.int
        msgLenBuf.array().toList()
        msgBuf.clear()
        msgBuf.limit(len)
        while (sock.read(msgBuf) > 0) { }
        msgBuf.flip()

        val msgArr = ByteArray(msgBuf.remaining())
        msgBuf.get(msgArr)
        return String(msgArr)
    }
}