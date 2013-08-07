#version 110

varying vec2 f_uv;

void
main (void)
{
  gl_FragColor = vec4(f_uv, 0, 1);
}
