#version 400
// Generated: 2013-09-30 12:46:59 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
uniform sampler2D t_normal;
// section: inputs
in vec2 f_uv;
// section: outputs
out vec4 out_0;
// section: main
void
main (void)
{
  vec4 rgba = vec4(texture(t_normal, f_uv).xyz, 1.0);
  out_0 = rgba;
}
