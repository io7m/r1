#version 120

varying vec3 f_normal_tangent;

void
main (void)
{
  gl_FragColor = vec4(f_normal_tangent, 1);
}