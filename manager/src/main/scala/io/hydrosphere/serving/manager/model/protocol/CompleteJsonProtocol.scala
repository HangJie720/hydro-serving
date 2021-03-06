package io.hydrosphere.serving.manager.model.protocol

import io.hydrosphere.serving.manager.controller.environment.CreateEnvironmentRequest
import io.hydrosphere.serving.manager.model.Result.{ClientError, ErrorCollection, HError, InternalError}
import io.hydrosphere.serving.manager.model.db.{CreateRuntimeRequest, PullRuntime}
import io.hydrosphere.serving.manager.service._
import io.hydrosphere.serving.manager.service.aggregated_info.{AggregatedModelBuild, AggregatedModelInfo, AggregatedModelVersion}
import io.hydrosphere.serving.manager.service.clouddriver.{MetricServiceTargetLabels, MetricServiceTargets}
import io.hydrosphere.serving.manager.service.model._
import io.hydrosphere.serving.manager.service.model_version.CreateModelVersionRequest
import io.hydrosphere.serving.manager.service.runtime._
import io.hydrosphere.serving.manager.service.service.CreateServiceRequest
import io.hydrosphere.serving.manager.util.task.ServiceTask
import io.hydrosphere.serving.manager.util.task.ServiceTask.ServiceTaskStatus
import spray.json.{JsObject, JsString, JsValue, _}

trait CompleteJsonProtocol extends CommonJsonProtocol with ContractJsonProtocol with ModelJsonProtocol {
  implicit val createServiceRequest = jsonFormat5(CreateServiceRequest)

  implicit val createRuntimeRequest = jsonFormat5(CreateRuntimeRequest)

  implicit def serviceTask[Req, Res](implicit j1: JsonFormat[Req], j2: JsonFormat[Res]) = {
    jsonFormat8(ServiceTask.apply[Req, Res])
  }

  implicit val createModelRequest = jsonFormat4(CreateModelRequest)

  implicit val pullDockerRequest = jsonFormat12(PullRuntime.apply)

  implicit val updateModelRequest = jsonFormat5(UpdateModelRequest)

  implicit val createModelVersionRequest = jsonFormat12(CreateModelVersionRequest)

  implicit val createEnvironmentRequest = jsonFormat2(CreateEnvironmentRequest)

  implicit val aggregatedModelInfoFormat = jsonFormat4(AggregatedModelInfo)

  implicit val aggregatedModelVersionFormat = jsonFormat11(AggregatedModelVersion.apply)

  implicit val aggregatedModelBuildFormat = jsonFormat9(AggregatedModelBuild.apply)

  implicit val metricServiceTargetLabelsFormat = jsonFormat11(MetricServiceTargetLabels)

  implicit val metricServiceTargetsFormat = jsonFormat2(MetricServiceTargets)

  implicit def internalErrorFormat[T <: Throwable] = new RootJsonFormat[InternalError[T]] {
    override def write(obj: InternalError[T]): JsValue = {
      val fields = Map(
        "exception" -> JsString(obj.exception.getMessage)
      )
      val reasonField = obj.reason.map { r =>
        Map("reason" -> JsString(r))
      }.getOrElse(Map.empty)

      JsObject(fields ++ reasonField)
    }

    override def read(json: JsValue): InternalError[T] = ???
  }

  implicit val clientErrorFormat = jsonFormat1(ClientError.apply)

  implicit val errorFormat = new RootJsonFormat[HError] {
    override def write(obj: HError): JsValue = {
      obj match {
        case x: ClientError => JsObject(Map(
          "error" -> JsString("Client"),
          "information" -> x.toJson
        ))
        case x: InternalError[_] => JsObject(Map(
          "error" -> JsString("Internal"),
          "information" -> x.toJson
        ))
        case ErrorCollection(errors) => JsObject(Map(
          "error" -> JsString("Multiple"),
          "information" -> JsArray(errors.map(write).toVector)
        ))
      }
    }

    override def read(json: JsValue): HError = ???
  }
}

object CompleteJsonProtocol extends CompleteJsonProtocol
