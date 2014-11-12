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

MODULE="com.io7m.r1.postprocessing.PostprocessingFXAA_${QUALITY}.fxaa_f"
OUTDIR="${OUT}/${MODULE}"

if [ ! -d "${OUTDIR}" ]
then
  mkdir -p "${OUTDIR}" || fatal "could not create ${OUTDIR}"
fi

for v in ${VERSIONS_FULL}
do
  FILE="${OUTDIR}/glsl-${v}.f"

  cat > "${FILE}" <<EOF
#version ${v}

EOF

  if [ ${v} -lt 130 ]
  then
  cat >> "${FILE}" <<EOF
#extension GL_ARB_shader_texture_lod : enable

EOF
  fi

  if [ ${v} -ge 130 ]
  then
    cat >> "${FILE}" <<EOF
noperspective in vec2 f_position_uv;

EOF
  else
    cat >> "${FILE}" <<EOF
noperspective vec2 f_position_uv;

EOF
  fi

  cat >> "${FILE}" << EOF
uniform sampler2D t_image;
uniform vec2      fxaa_screen_inverse;
uniform float     fxaa_subpixel;
uniform float     fxaa_edge_threshold;
uniform float     fxaa_edge_threshold_min;

float
FxaaLuma (in vec4 c)
{
  return c.y;
}

EOF

  ./make-one-function.sh "${v}" "${QUALITY}" >> "${FILE}"

  if [ ${v} -ge 330 ]
  then
    cat >> "${FILE}" <<EOF
layout(location = 0) out vec4 out_rgba;
EOF
  else
    if [ ${v} -ge 130 ]
    then
      cat >> "${FILE}" <<EOF
out vec4 out_rgba;
EOF
    fi
  fi

  if [ ${v} -ge 130 ]
  then
    cat >> "${FILE}" <<EOF
void
main (void)
{
  out_rgba = FxaaPixelShader(
    f_position_uv,
    t_image,
    fxaa_screen_inverse,
    fxaa_subpixel,
    fxaa_edge_threshold,
    fxaa_edge_threshold_min
  );
}
EOF
  else
    cat >> "${FILE}" << EOF
void
main (void)
{
  gl_FragColor = FxaaPixelShader(
    f_position_uv,
    t_image,
    fxaa_screen_inverse,
    fxaa_subpixel,
    fxaa_edge_threshold,
    fxaa_edge_threshold_min
  );
}
EOF
  fi
done

for v in ${VERSIONS_ES}
do
  FILE="${OUTDIR}/glsl-es-${v}.f"

  cat > "${FILE}" <<EOF
#version ${v} es

precision highp float;
precision highp int;

in vec2 f_position_uv;

uniform sampler2D t_image;
uniform vec2      fxaa_screen_inverse;
uniform float     fxaa_subpixel;
uniform float     fxaa_edge_threshold;
uniform float     fxaa_edge_threshold_min;

float
FxaaLuma (in vec4 c)
{
  return c.y;
}

EOF

  ./make-one-function.sh "${v}" "${QUALITY}" >> "${FILE}"

  cat >> "${FILE}" <<EOF
layout(location = 0) out vec4 out_rgba;

void
main (void)
{
  out_rgba = FxaaPixelShader(
    f_position_uv,
    t_image,
    fxaa_screen_inverse,
    fxaa_subpixel,
    fxaa_edge_threshold,
    fxaa_edge_threshold_min
  );
}
EOF

done

cat > "${OUTDIR}/meta.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<g:meta-fragment g:version="5" xmlns:g="http://schemas.io7m.com/parasol/glsl-meta">
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

  <g:parameters-fragment>
    <g:declared-fragment-parameters>
      <g:parameter g:name="t_image" g:type="sampler2D"/>
      <g:parameter g:name="fxaa_screen_inverse" g:type="vec2"/>
      <g:parameter g:name="fxaa_subpixel" g:type="float"/>
      <g:parameter g:name="fxaa_edge_threshold" g:type="float"/>
      <g:parameter g:name="fxaa_edge_threshold_min" g:type="float"/>
    </g:declared-fragment-parameters>
    <g:declared-fragment-inputs>
      <g:input g:name="f_position_uv" g:type="vec2"/>
    </g:declared-fragment-inputs>
    <g:declared-fragment-outputs>
      <g:fragment-output g:name="out_rgba" g:type="vec4" g:index="0"/>
    </g:declared-fragment-outputs>
  </g:parameters-fragment>

</g:meta-fragment>
EOF
