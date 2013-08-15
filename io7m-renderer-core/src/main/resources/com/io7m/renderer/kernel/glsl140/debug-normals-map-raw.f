#version 140

uniform sampler2D t_normal;

in vec2 f_uv;

out vec4 out_frag_color;

void
main (void)
{
  out_frag_color = vec4(texture2D (t_normal, f_uv).xyz, 1.0);
}
