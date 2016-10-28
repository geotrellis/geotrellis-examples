package com.azavea.geotrellis.tutorial

import geotrellis.geotools._
import geotrellis.proj4.WebMercator
import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.hadoop._

import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.geotools.gce.geotiff._
import org.opengis.parameter.GeneralParameterValue


object EtlExercise {

  val logger = Logger.getLogger(EtlExercise.getClass)

  /**
    * Dump a layer to disk.
    */
  def dump(rdd: RDD[(SpatialKey, MultibandTile)] with Metadata[TileLayerMetadata[SpatialKey]], stem: String) = {
    val mt = rdd.metadata.mapTransform

    rdd.collect.foreach({ case (k, v) =>
      val extent = mt(k)
      val pr = ProjectedRaster(Raster(v, extent), WebMercator)
      val gc = pr.toGridCoverage2D
      val writer = new GeoTiffWriter(new java.io.File(s"/tmp/tif/${stem}-${System.currentTimeMillis}.tif"))
      writer.write(gc, Array.empty[GeneralParameterValue])
    })
  }

  /**
    * MAIN
    */
  def main(args: Array[String]) : Unit = {

    /* Command line arguments */
    if (args.length < 3) System.exit(-1)
    val hdfsUri = args(0)
    val layerName = args(1)
    val zoomLevel = args(2).toInt

    /* Spark context */
    val sparkConf = new SparkConf()
      .setAppName("EtlExercise")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sparkContext = new SparkContext(sparkConf)
    implicit val sc = sparkContext

    logger.info(s"Reading ${layerName}:${zoomLevel} from HDFS")
    val rdd0 = {
      val inLayerId = LayerId(layerName, zoomLevel)
      require(HadoopAttributeStore(hdfsUri).layerExists(inLayerId))
      HadoopLayerReader(hdfsUri).read[SpatialKey, MultibandTile, TileLayerMetadata[SpatialKey]](inLayerId)
    }

    logger.info("Histogram Equalization")
    val rdd1 = rdd0.equalize

    logger.info("Sigmoidal contrast")
    val rdd2 = ContextRDD(rdd0.sigmoidal(.5, 10), rdd0.metadata)

    logger.info("Dumping layers to disk")
    dump(rdd0, "raw")
    dump(rdd1, "equal")
    dump(rdd2, "sigmoidal")
  }

}
