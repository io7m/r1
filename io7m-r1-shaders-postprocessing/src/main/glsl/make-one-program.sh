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

OUT=`head -n 1 config-outdir` || fatal "could not read config-outdir"
MODULE="com.io7m.r1.postprocessing.PostprocessingFXAA_${QUALITY}.fxaa"
OUTDIR="${OUT}/fxaa_${QUALITY}"

if [ ! -d "${OUTDIR}" ]
then
  mkdir -p "${OUTDIR}" || fatal "could not create ${OUTDIR}"
fi

cat > "${OUTDIR}/meta.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<g:meta-program g:version="5" xmlns:g="http://schemas.io7m.com/parasol/glsl-meta">
  <g:program-name>${MODULE}</g:program-name>

  <g:supports>
    <g:version g:number="120" g:api="glsl"/>
    <g:version g:number="130" g:api="glsl"/>
    <g:version g:number="140" g:api="glsl"/>
    <g:version g:number="150" g:api="glsl"/>
    <g:version g:number="300" g:api="glsl-es"/>
    <g:version g:number="310" g:api="glsl-es"/>
    <g:version g:number="330" g:api="glsl"/>
    <g:version g:number="400" g:api="glsl"/>
    <g:version g:number="410" g:api="glsl"/>
    <g:version g:number="420" g:api="glsl"/>
    <g:version g:number="430" g:api="glsl"/>
    <g:version g:number="440" g:api="glsl"/>
    <g:version g:number="450" g:api="glsl"/>
  </g:supports>

  <g:shaders-vertex>
    <g:shader-vertex>${MODULE}_v</g:shader-vertex>
  </g:shaders-vertex>

  <g:shader-fragment>${MODULE}_f</g:shader-fragment>

</g:meta-program>
EOF
