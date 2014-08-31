#!/bin/sh -ex

VERSION="0.9.0"

JAR="io7m-r1-meshes-tools/target/io7m-r1-meshes-tools-${VERSION}-meshtool.jar"

for size in 16 32 64
do
  java -jar "${JAR}" --convert \
    sphere${size}-mesh \
    io7m-r1-meshes/src/main/blend/unit_spheres.dae \
    io7m-r1-kernel/src/main/resources/com/io7m/r1/kernel/sphere${size}.rmxz

  java -jar "${JAR}" --convert \
    sphere${size}-mesh \
    io7m-r1-meshes/src/main/blend/unit_spheres.dae \
    io7m-r1-kernel/src/main/resources/com/io7m/r1/kernel/sphere${size}.rmbz
done
