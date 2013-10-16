#!/bin/sh -ex

rm -rf out-parasol
mkdir out-parasol
rm -rf out-glsl
mkdir out-glsl

cp *.p out-parasol
./sources
./batch > out-parasol/batch.txt

java \
  -jar io7m-parasol-compiler-frontend-0.1.14-parasol-c.jar \
  --Yno-comments \
  --require-full "[110,430]" \
  --require-es "[100,300]" \
  --compile-batch out-glsl out-parasol/batch.txt out-parasol/*.p

