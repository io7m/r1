#version 130

out vec4 out_frag_color;

void
main (void)
{
  out_frag_color = vec4(gl_FragCoord.z, gl_FragCoord.z, gl_FragCoord.z, 1.0);
}
