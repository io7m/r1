#version 300

precision highp float;
precision highp int;

out vec4 out_0;


void
main (void)
{
  vec4 rgba = vec4(gl_FragCoord.zzz, 1.0);
  out_0 = rgba;
}
