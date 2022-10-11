import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.Service
import com.twitter.finagle.websocket.{Frame, Request, Response}
import com.twitter.util.Future

class TestService extends Service[Request, Response] {
  override def apply(request: Request): Future[Response] = {
    Future {
      val response = Response(handler(request.messages))
      println(response)
      println(response.toString())
      response
    }
  }

  def handler(messages: AsyncStream[Frame]): AsyncStream[Frame] = {
    messages.map {
      case Frame.Text("1") => Frame.Text("one")
      case Frame.Text("2") => Frame.Text("two")
      case Frame.Text("3") => Frame.Text("three")
      case Frame.Text("4") => Frame.Text("cuatro")
      case Frame.Text("5") => Frame.Text("five")
      case Frame.Text("6") => Frame.Text("6")
      case _ => Frame.Text("??")
    }
  }
}