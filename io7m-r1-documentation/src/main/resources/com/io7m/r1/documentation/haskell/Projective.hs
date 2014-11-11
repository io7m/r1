module Projective where

import qualified Attenuation
import qualified Color4
import qualified Direction
import qualified LightDiffuse
import qualified LightSpecular
import qualified LightSpherical
import qualified Normal
import qualified Position3
import qualified Specular
import qualified Spaces
import qualified Vector3f
import qualified Vector4f

projective :: Direction.T Spaces.Eye -> Normal.T -> Position3.T Spaces.Eye -> LightProjective.T -> Specular.T -> Float -> Color3.T -> Color4.T -> Vector3f.T
projective view n surface_position light specular shadow texture (Vector4f.V4 sr sg sb _) =
  let
    position_diff   = Position3.sub3 surface_position (LightSpherical.origin light)
    stl             = Vector3f.normalize (Vector3f.negation position_diff)
    distance        = Vector3f.magnitude (position_diff)
    attenuation_raw = Attenuation.attenuation (LightSpherical.radius light) (LightSpherical.falloff light) distance
    attenuation     = attenuation_raw * shadow
    light_color     = Vector3f.mult3 (LightSpherical.color light) texture
    light_intensity = LightSpherical.intensity light
    light_d         = LightDiffuse.diffuse stl n light_color light_intensity
    light_s         = LightSpecular.specular stl view n light_color light_intensity specular
    light_da        = Vector3f.scale light_d attenuation
    light_sa        = Vector3f.scale light_s attenuation
    lit_d           = Vector3f.mult3 (Vector3f.V3 sr sg sb) light_da
    lit_s           = Vector3f.add3 lit_d light_sa
  in 
    lit_s
