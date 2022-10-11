//#imports
import Server.server
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.{Service, Websocket}
import com.twitter.finagle.websocket.{Frame, Request, Response}
import com.twitter.util.{Await, Future}
//#imports

object Server extends App {
  // A server that when given a number, responds with a word (mostly).
  val service: Service[Request, Response] = new TestService
  val server = Websocket.serve(":14000", service)
  Await.ready(server)
}




