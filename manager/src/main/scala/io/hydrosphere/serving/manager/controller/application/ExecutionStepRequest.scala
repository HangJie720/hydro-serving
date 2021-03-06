package io.hydrosphere.serving.manager.controller.application

case class ExecutionStepRequest(
  services: List[ServiceCreationDescription]
)

object ExecutionStepRequest {
  import io.hydrosphere.serving.manager.model.protocol.CompleteJsonProtocol._
  implicit val format = jsonFormat1(ExecutionStepRequest.apply)
}