package com.twitter.finagle.websocket

import com.twitter.concurrent.AsyncStream

case class Response(messages: AsyncStream[Frame]) {
  override def toString: String = s"[messages = ${messages.toString}]"
}

