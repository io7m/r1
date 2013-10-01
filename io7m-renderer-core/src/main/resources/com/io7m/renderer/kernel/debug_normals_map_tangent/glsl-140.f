#version 140


uniform sampler2D t_normal;
in vec2 f_uv;
out vec4 out_0;


void
main (void)
{
  vec4 rgba = vec4(texture(t_normal, f_uv).xyz, 1.0);
  out_0 = rgba;
}
