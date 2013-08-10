#version 120

uniform sampler2D t_normal;

varying vec2 f_uv;
varying vec3 f_normal;

void
main (void)
{
  vec3 N  = normalize (f_normal);
  vec3 NM = texture2D (t_normal, f_uv).rgb;
  NM = (NM * 2.0) - 1.0;

  gl_FragColor = vec4(normalize (N + NM), 1);
}
