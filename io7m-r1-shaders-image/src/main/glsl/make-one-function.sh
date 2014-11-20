#!/bin/sh

if [ $# -ne 2 ]
then
  echo "usage: glsl-version quality" 1>&2
  exit 1
fi

GLSL="$1"
shift

QUALITY="$1"
shift

ARGS="-nostdinc"
ARGS="${ARGS} -DFXAA_PC=1"
ARGS="${ARGS} -DFXAA_GREEN_AS_LUMA=1"
ARGS="${ARGS} -DFXAA_QUALITY__PRESET=${QUALITY}"

if [ ${GLSL} -ge 130 ]
then
  ARGS="${ARGS} -DFXAA_GLSL_130=1"
else
  ARGS="${ARGS} -DFXAA_GLSL_120=1"
fi

cpp ${ARGS} "Fxaa3_11_mod.h" | grep -v '^#' | cat -s
