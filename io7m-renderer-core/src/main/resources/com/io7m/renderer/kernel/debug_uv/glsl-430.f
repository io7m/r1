#version 430
// Generated: 2013-09-30 14:44:33 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
// section: inputs
in vec2 f_uv;
// section: outputs
out vec4 out_0;
// section: main
void
main (void)
{
  vec4 rgba = vec4(f_uv, 0.0, 1.0);
  out_0 = rgba;
}
