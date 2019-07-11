package com.github.geirolz.advxml.exceptions

/**
  * Advxml
  * Created by geirolad on 11/07/2019.
  *
  * @author geirolad
  */
class AggregatedException(msg: String, exceptions: Seq[Throwable])
  extends RuntimeException(msg + exceptions.map(_.getMessage)
    .foldLeft("")((acc,value) => acc + ",\n" + value))


