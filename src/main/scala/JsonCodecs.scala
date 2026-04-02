import cats.effect.IO
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import io.circe.generic.auto.deriveDecoder


class JsonCodecs {

  given EntityDecoder[IO, DebitRequest] =
    jsonOf[IO, DebitRequest]

  given EntityDecoder[IO, CreateAccountRequest] =
    jsonOf[IO, CreateAccountRequest]

}