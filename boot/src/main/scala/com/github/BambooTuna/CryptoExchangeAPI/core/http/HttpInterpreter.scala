package com.github.BambooTuna.CryptoExchangeAPI.core.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import cats.data.EitherT
import io.circe.{Decoder, parser}
import monix.eval.Task

object HttpInterpreter {

  def runRequest[O](request: HttpRequest)(
      implicit decoderO: Decoder[O],
      system: ActorSystem,
      materializer: Materializer): EitherT[Task, HttpInternalException, O] = {
    EitherT {
      Task
        .deferFutureAction { implicit ec =>
          Http()
            .singleRequest(request)
            .flatMap(r => {
              Unmarshal(r)
                .to[String]
                .map(
                  u =>
                    parser
                      .decode[O](u)
                      .left
                      .map(e =>
                        DecodeResponseBodyException(e.getMessage,
                                                    r.status.intValue(),
                                                    Some(u))))
                .recover {
                  case e: Throwable =>
                    Left(
                      UnmarshalToStringException(e.getMessage,
                                                 r.status.intValue(),
                                                 None))
                }
            })
            .recover {
              case e: Throwable =>
                Left(HttpSingleRequestException(e.getMessage))
            }
        }
    }
  }

}
