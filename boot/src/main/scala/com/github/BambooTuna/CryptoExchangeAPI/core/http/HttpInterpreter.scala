package com.github.BambooTuna.CryptoExchangeAPI.core.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import cats.data.EitherT
import io.circe._
import monix.eval.Task

object HttpInterpreter {

  def runRequest[O](request: HttpRequest)(
      andThen: HttpInterpreterResponse[String] => Either[
        HttpInternalException,
        HttpInterpreterResponse[O]])(implicit system: ActorSystem,
                                     materializer: Materializer)
    : EitherT[Task, HttpInternalException, HttpInterpreterResponse[O]] = {
    EitherT {
      Task
        .deferFutureAction { implicit ec =>
          Http()
            .singleRequest(request)
            .flatMap(r => {
              Unmarshal(r)
                .to[String]
                .map(a =>
                  andThen(HttpInterpreterResponse(r.status.intValue(), a)))
                .recover {
                  case e: Throwable =>
                    Left(UnmarshalToStringException(r.status.intValue(),
                                                    e.getMessage))
                }
            })
            .recover {
              case e: Throwable =>
                Left(HttpSingleRequestException(e.getMessage))
            }
        }
    }
  }

  def parseResponse[O](implicit decoder: Decoder[O]): HttpInterpreterResponse[
    String] => Either[HttpInternalException, HttpInterpreterResponse[O]] = {
    response: HttpInterpreterResponse[String] =>
      parser
        .decode[O](response.body)
        .right
        .map(a => response.copy(body = a))
        .left
        .map(
          e =>
            DecodeResponseBodyException(response.statusCode,
                                        response.body,
                                        e.getMessage))
  }

  def checkStatusCode(allowStatusCodes: Set[Int] = Set(200))
    : HttpInterpreterResponse[String] => Either[
      HttpInternalException,
      HttpInterpreterResponse[String]] = {
    response: HttpInterpreterResponse[String] =>
      if (allowStatusCodes.contains(response.statusCode)) Right(response)
      else
        Left(
          BadResponseStatusCodeException(
            response.statusCode,
            response.body,
            s"StatusCodes(${response.statusCode}) is not allowed. Allowed StatusCodes is: $allowStatusCodes"))
  }

}
