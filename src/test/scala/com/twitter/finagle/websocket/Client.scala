import com.twitter.concurrent.AsyncStream
import com.twitter.conversions.time._
import com.twitter.finagle.Websocket
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle.websocket.{Frame, Request, Response}
import com.twitter.util.{Await, Future, Promise}

import java.net.URI


object Client extends App {
  // Responds to messages from the server.
  implicit val timer = DefaultTimer.twitter
  def handler(messages: AsyncStream[Frame]): AsyncStream[Frame] =
    messages.flatMap {
      case Frame.Text(message) =>
        // Print the received message.
        println(message)

        AsyncStream.fromFuture(
          // Sleep for a second...
          Future.sleep(1.second).map { _ =>
            // ... and then send a message to the server.
            Frame.Text(message.length.toString)
          })

      case _ => AsyncStream.of(Frame.Text("??"))
    }

  val incoming = new Promise[AsyncStream[Frame]]
  val outgoing =
    Frame.Text("1") +:: handler(
      AsyncStream.fromFuture(incoming).flatten)

  val client = Websocket.client.newService(":14000")
  val req = Request(new URI("/"), Map.empty, null, outgoing)

  // Take the messages of the response and fulfill `incoming`.
  client(req).map(_.messages).proxyTo(incoming)

  val response: Future[Response] = client(req)

  Await.result(response.onSuccess{rep: Response => println("GET success: " + rep) } )
  System.out.println(response)
  System.out.println(outgoing)


}
