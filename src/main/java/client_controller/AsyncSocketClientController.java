package client_controller;

import info.HostInfo;
import ledger.Ledger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class AsyncSocketClientController extends SocketClientController {

    private Selector selector;

    public AsyncSocketClientController(HostInfo host, Ledger ledger) {
        super(host,ledger);
    }

    public void listen() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", super.host_info.getEndPointPort()));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(256);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (key.isAcceptable()) {
                        register(selector, serverSocket);
                    }
                    if (key.isReadable()) {
                        respondToClient(buffer, key);
                    }
                    iter.remove();
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void respondToClient(ByteBuffer buffer, SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        client.read(buffer);

        String content = new String(buffer.array()).trim();

        Charset charset = Charset.forName("ISO-8859-1");

        String result = super.processResponse(content);
        CharBuffer c = CharBuffer.wrap(result);

        ByteBuffer response = charset.encode(c);
        response.compact();
        response.flip();

        while (response.hasRemaining()) {
            client.write(response);
        }

        buffer.clear();
        client.close();
    }

    private void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

}
