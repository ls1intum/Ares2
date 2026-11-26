package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

@SuppressWarnings("AopLanguageInspection") public aspect JavaAspectJNetworkSystemPointcutDefinitions {

    pointcut networkConnectMethods(): (
            call(* java.net.Socket+.connect(..)) ||
            call(java.net.Socket+.new(..)) ||
            call(* java.net.DatagramSocket+.connect(..)) ||
            call(java.net.DatagramSocket+.new(..)) ||
            call(* java.nio.channels.DatagramChannel+.connect(..)) ||
            call(java.nio.channels.DatagramChannel+.new(..)) ||
            call(* java.nio.channels.SocketChannel+.connect(..)) ||
            call(java.nio.channels.SocketChannel+.new(..)) ||
            call(* java.nio.channels.SocketChannel.open(java.net.SocketAddress)) ||
            call(* java.nio.channels.AsynchronousSocketChannel+.connect(..)) ||
            call(java.nio.channels.AsynchronousSocketChannel+.new(..)) ||
            call(* java.net.HttpURLConnection+.connect(..)) ||
            call(java.net.HttpURLConnection+.new(..)) ||
            call(* javax.net.ssl.HttpsURLConnection+.connect(..)) ||
            call(javax.net.ssl.HttpsURLConnection+.new(..)) ||
            call(* javax.net.SocketFactory+.createSocket(..)) ||
            call(* javax.net.ssl.SSLSocketFactory+.createSocket(..)) ||
            call(* java.net.URL.openConnection(..)) ||
            call(* java.net.URL.openStream(..)) ||
            call(* java.net.URLConnection+.connect(..))
            );

    pointcut networkSendMethods(): (
            call(* java.net.Socket+.getOutputStream(..)) ||
            call(* java.net.DatagramSocket+.send(..)) ||
            call(* java.nio.channels.DatagramChannel+.send(..)) ||
            call(* java.nio.channels.DatagramChannel+.write(..)) ||
            // A datagram channel held as a WritableByteChannel or GatheringByteChannel is
            // matched by its runtime type, since the call names only the interface.
            (call(* java.nio.channels.WritableByteChannel+.write(..)) && target(java.nio.channels.DatagramChannel)) ||
            call(* java.net.http.HttpClient+.send(..)) ||
            call(* java.net.http.HttpClient+.sendAsync(..)) ||
            call(* java.net.URLConnection+.getOutputStream(..)) ||
            call(* java.net.HttpURLConnection+.getOutputStream(..))
            );

    pointcut networkReceiveMethods(): (
            call(* java.net.DatagramSocket+.receive(..)) ||
            call(* java.nio.channels.DatagramChannel+.receive(..)) ||
            call(* java.nio.channels.DatagramChannel+.read(..)) ||
            // Likewise for a datagram channel held as a ReadableByteChannel or
            // ScatteringByteChannel.
            (call(* java.nio.channels.ReadableByteChannel+.read(..)) && target(java.nio.channels.DatagramChannel)) ||
            call(* java.net.Socket+.getInputStream(..)) ||
            call(* java.net.URLConnection+.getInputStream(..)) ||
            call(* java.net.HttpURLConnection+.getInputStream(..))
            );
}
