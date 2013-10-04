#version 120


uniform sampler2D t_diffuse_0;
varying vec2 f_uv;



void
main (void)
{
  vec4 rgba = texture2D(t_diffuse_0, f_uv);
  gl_FragColor = rgba;
}
