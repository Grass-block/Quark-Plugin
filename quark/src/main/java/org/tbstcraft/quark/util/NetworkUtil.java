package org.tbstcraft.quark.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

@SuppressWarnings("unused")
public interface NetworkUtil {
    String USER_AGENT = "Mozilla/5.0";

    static String httpGet(String url) throws IOException {
        String str;
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent",USER_AGENT);

        InputStream in = con.getInputStream();
        str = new String(in.readAllBytes());
        in.close();
        con.disconnect();
        return str;
    }

    static String httpPost(String url){
        String str;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent",USER_AGENT);

            InputStream in = con.getInputStream();
            str = new String(in.readAllBytes());
            in.close();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return str;
    }

    static InetSocketAddress allocateLocalAddressUDP(){
        try {
            DatagramSocket socket=new DatagramSocket();
            InetSocketAddress addr= new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
            socket.close();
            return addr;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    static InetSocketAddress allocateLocalAddressTCP(){
        try {
            Socket socket=new Socket();
            InetSocketAddress addr= new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
            socket.close();
            return addr;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
