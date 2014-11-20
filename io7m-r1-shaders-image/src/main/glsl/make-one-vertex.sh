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

VERSIONS_FULL="120 130 140 150 330 400 410 420 430 440 450"
VERSIONS_ES="300 310"

MODULE="com.io7m.r1.image.ImageFXAA_${QUALITY}.fxaa_v"
OUTDIR="${OUT}/${MODULE}"

if [ ! -d "${OUTDIR}" ]
then
  mkdir -p "${OUTDIR}" || fatal "could not create ${OUTDIR}"
fi

for v in ${VERSIONS_FULL}
do
  FILE="${OUTDIR}/glsl-${v}.v"

  cat > "${FILE}" <<EOF
#version ${v}

EOF

  if [ ${v} -ge 130 ]
  then
    cat "glsl-130.v" >> "${FILE}" || fatal "could not cat"
  else
    cat "glsl-120.v" >> "${FILE}" || fatal "could not cat"
  fi
done

for v in ${VERSIONS_ES}
do
  FILE="${OUTDIR}/glsl-es-${v}.v"

  cat > "${FILE}" <<EOF
#version ${v} es

EOF

  cat "glsl-es-300.v" >> "${FILE}" || fatal "could not cat"
done

cat > "${OUTDIR}/meta.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<g:meta-vertex g:version="5" xmlns:g="http://schemas.io7m.com/parasol/glsl-meta">
  <g:program-name>${MODULE}</g:program-name>

  <g:supports>
    <g:version g:number="130" g:api="glsl"/>
    <g:version g:number="100" g:api="glsl-es"/>
    <g:version g:number="400" g:api="glsl"/>
    <g:version g:number="140" g:api="glsl"/>
    <g:version g:number="110" g:api="glsl"/>
    <g:version g:number="410" g:api="glsl"/>
    <g:version g:number="430" g:api="glsl"/>
    <g:version g:number="440" g:api="glsl"/>
    <g:version g:number="420" g:api="glsl"/>
    <g:version g:number="330" g:api="glsl"/>
    <g:version g:number="150" g:api="glsl"/>
    <g:version g:number="120" g:api="glsl"/>
    <g:version g:number="300" g:api="glsl-es"/>
  </g:supports>

  <g:parameters-vertex>
    <g:declared-vertex-parameters>
    </g:declared-vertex-parameters>
    <g:declared-vertex-inputs>
      <g:input g:name="v_position" g:type="vec3"/>
    </g:declared-vertex-inputs>
    <g:declared-vertex-outputs>
      <g:output g:name="f_position_uv" g:type="vec2"/>
    </g:declared-vertex-outputs>
  </g:parameters-vertex>

</g:meta-vertex>
EOF
