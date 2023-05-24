package network.sender

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class TCPChannelSender(private val sock: SocketChannel): SenderInterface {
    private val msgBuf = ByteBuffer.allocate(65536)

    override fun send(msg: String) {
        val msgLen = msg.length
        msgBuf.clear()
        msgBuf.limit(msgLen + 4)
        msgBuf.putInt(msgLen)
        msgBuf.put(msg.toByteArray())

        msgBuf.flip()
        sock.write(msgBuf)
    }
}