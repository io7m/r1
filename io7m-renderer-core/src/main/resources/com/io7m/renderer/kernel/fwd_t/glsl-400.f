#version 400


uniform sampler2D t_diffuse_0;
in vec2 f_uv;
out vec4 out_0;


void
main (void)
{
  vec4 rgba = texture(t_diffuse_0, f_uv);
  out_0 = rgba;
}
