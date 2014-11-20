#!/bin/sh

for quality in 10 15 20 25 29 39
do
  echo "making quality ${quality}" 1>&2
  ./make-one.sh "${quality}"
done
