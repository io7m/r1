#version 120
// Generated: 2013-09-30 14:44:00 +0000
// Generator: http://io7m.com/software/parasol
// Do not edit this file! Edit the original source.
// section: types
// section: terms
// section: parameters
uniform sampler2D t_diffuse_0;
// section: inputs
varying vec2 f_uv;
// section: outputs
// output 0 out_0 omitted
// section: main
void
main (void)
{
  vec4 rgba = texture2D(t_diffuse_0, f_uv);
  gl_FragColor = rgba;
}
