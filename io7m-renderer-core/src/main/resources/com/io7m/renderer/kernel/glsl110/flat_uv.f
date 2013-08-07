#version 110

uniform sampler2D t_diffuse_0;

varying vec2 f_uv;

void
main (void)
{
  gl_FragColor = texture2D(t_diffuse_0, f_uv);
}
