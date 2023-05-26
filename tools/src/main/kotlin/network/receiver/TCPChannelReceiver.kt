package network.receiver

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class TCPChannelReceiver(private val sock: SocketChannel): ReceiverInterface {
    private val msgLenBuf = ByteBuffer.allocate(4)
    private val msgBuf = ByteBuffer.allocate(65536)

    override fun receive(): String {
        msgLenBuf.clear()
        sock.read(msgLenBuf)
        msgLenBuf.flip()
        val len = msgLenBuf.int
        msgLenBuf.array().toList()
        msgBuf.clear()
        msgBuf.limit(len)
        sock.read(msgBuf)
        msgBuf.flip()

        val msgArr = ByteArray(msgBuf.remaining())
        msgBuf.get(msgArr)
        println(String(msgArr))
        return String(msgArr)
    }
}