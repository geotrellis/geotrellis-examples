name := "tutorial"

val gtVersion = "1.0.0-cd1ca27"

resolvers +=  Resolver.bintrayRepo("azavea", "geotrellis")

libraryDependencies ++= Seq(
  "com.azavea.geotrellis" %% "geotrellis-geotools" % gtVersion,
  "com.azavea.geotrellis" %% "geotrellis-raster" % gtVersion,
  "com.azavea.geotrellis" %% "geotrellis-spark" % gtVersion,
  "com.azavea.geotrellis" %% "geotrellis-vector" % gtVersion,
  "org.apache.hadoop" % "hadoop-client" % Version.hadoop % "provided",
  "org.apache.spark" %% "spark-core" % Version.spark % "provided"
)

fork in Test := false
parallelExecution in Test := false
