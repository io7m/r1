#!/bin/sh -ex

VERSION="0.9.0"

JAR="io7m-r1-meshes-tools/target/io7m-r1-meshes-tools-${VERSION}-meshtool.jar"

java -jar "${JAR}" --convert \
  cube-mesh \
  io7m-r1-meshes/src/main/blend/unit_cube.dae \
  io7m-r1-kernel/src/main/resources/com/io7m/r1/kernel/cube.rmxz

java -jar "${JAR}" --convert \
  cube-mesh \
  io7m-r1-meshes/src/main/blend/unit_cube.dae \
  io7m-r1-kernel/src/main/resources/com/io7m/r1/kernel/cube.rmbz
