package com.lattice.facerecognition.data

data class ModelInfo(val name : String ,
                     val assetsFilename : String ,
                     val cosineThreshold : Float ,
                     val l2Threshold : Float ,
                     val outputDims : Int ,
                     val inputDims : Int )
