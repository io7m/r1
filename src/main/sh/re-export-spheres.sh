#!/bin/sh -ex

VERSION="0.9.0"

JAR="io7m-renderer-meshes-tools/target/io7m-renderer-meshes-tools-${VERSION}-meshtool.jar"

for size in 16 32 64
do
  java -jar "${JAR}" --convert \
    sphere${size}-mesh \
    io7m-renderer-meshes/src/main/blend/unit_spheres.dae \
    io7m-renderer-kernel/src/main/resources/com/io7m/renderer/kernel/sphere${size}.rmxz

  java -jar "${JAR}" --convert \
    sphere${size}-mesh \
    io7m-renderer-meshes/src/main/blend/unit_spheres.dae \
    io7m-renderer-kernel/src/main/resources/com/io7m/renderer/kernel/sphere${size}.rmbz
done
