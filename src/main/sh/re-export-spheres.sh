#!/bin/sh -ex

VERSION="0.9.0"

JAR="io7m-renderer-collada/target/io7m-renderer-collada-${VERSION}-collada-to-rmx.jar"

java -jar "${JAR}" -i io7m-renderer-meshes/src/main/blend/unit_spheres.dae --compress -e sphere64-mesh -n sphere64 -o io7m-renderer-kernel/src/main/resources/com/io7m/renderer/kernel/sphere64.rmxz
java -jar "${JAR}" -i io7m-renderer-meshes/src/main/blend/unit_spheres.dae --compress -e sphere32-mesh -n sphere32 -o io7m-renderer-kernel/src/main/resources/com/io7m/renderer/kernel/sphere32.rmxz
java -jar "${JAR}" -i io7m-renderer-meshes/src/main/blend/unit_spheres.dae --compress -e sphere16-mesh -n sphere16 -o io7m-renderer-kernel/src/main/resources/com/io7m/renderer/kernel/sphere16.rmxz

