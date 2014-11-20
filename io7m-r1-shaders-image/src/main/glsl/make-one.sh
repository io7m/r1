#!/bin/sh

fatal()
{
  echo "fatal: $1" 1>&2
  exit 1
}

if [ $# -ne 1 ]
then
  echo "usage: quality" 1>&2
  exit 1
fi

QUALITY="$1"
shift

./make-one-fragment.sh "${QUALITY}" || fatal "could not generate fragment shader"
./make-one-vertex.sh "${QUALITY}" || fatal "could not generate vertex shader"
./make-one-program.sh "${QUALITY}" || fatal "could not generate program"
