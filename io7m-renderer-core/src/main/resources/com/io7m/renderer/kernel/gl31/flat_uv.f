#version 140

uniform sampler2D t_diffuse_0;

in vec2 f_uv;

layout(location = 0) out vec4 out_frag_color;

void
main (void)
{
  out_frag_color = texture2D(t_diffuse_0, f_uv);
}