#version 130
// Generated: 2013-09-30 14:44:00 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
uniform sampler2D t_diffuse_0;
// section: inputs
in vec2 f_uv;
// section: outputs
out vec4 out_0;
// section: main
void
main (void)
{
  vec4 rgba = texture(t_diffuse_0, f_uv);
  out_0 = rgba;
}
