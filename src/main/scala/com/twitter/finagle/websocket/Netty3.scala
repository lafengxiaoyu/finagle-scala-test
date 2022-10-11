package com.twitter.finagle.websocket

import com.twitter.finagle.Stack
import com.twitter.finagle.client.Transporter
import com.twitter.finagle.netty3._
import com.twitter.finagle.server.Listener
import com.twitter.finagle.transport.TransportContext
import org.jboss.netty.channel.Channels
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.websocketx._

import java.net.{SocketAddress, URI}
import scala.collection.JavaConverters._

private[finagle] object Netty3 {
  import Frame._

  private def serverPipeline = {
    val pipeline = Channels.pipeline()
    pipeline.addLast("decoder", new HttpRequestDecoder)
    pipeline.addLast("encoder", new HttpResponseEncoder)
    pipeline.addLast("handler", new WebSocketServerHandler)
    pipeline
  }

  private def clientPipeline() = {
    val pipeline = Channels.pipeline()
    pipeline.addLast("decoder", new HttpResponseDecoder)
    pipeline.addLast("encoder", new HttpRequestEncoder)
    pipeline.addLast("handler", new WebSocketClientHandler)
    pipeline
  }

  def newListener[In, Out](params: Stack.Params): Listener[In, Out, TransportContext] =
    Netty3Listener(() => serverPipeline, params)

  def newTransporter[In, Out](
    addr: SocketAddress,
    params: Stack.Params
  ): Transporter[In, Out, TransportContext] =
    Netty3Transporter[In, Out](() => clientPipeline(), addr, params)

  def fromNetty(m: Any): Frame = m match {
    case text: TextWebSocketFrame =>
      Text(text.getText)

    case cont: ContinuationWebSocketFrame =>
      Text(cont.getText)

    case frame =>
      throw new IllegalStateException(s"unknown frame: $frame")
  }

  def toNetty(frame: Frame): WebSocketFrame = frame match {
    case Text(message) =>
      new TextWebSocketFrame(message)
  }

  def newHandshaker(uri: URI, headers: Map[String, String]): WebSocketClientHandshaker = {
    val factory = new WebSocketClientHandshakerFactory
    factory.newHandshaker(uri, WebSocketVersion.V13, null, false, headers.asJava)
  }
}
