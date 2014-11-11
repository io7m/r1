module ProjectiveMatrix where

import qualified Matrix4f

projective_matrix :: Matrix4f.T -> Matrix4f.T -> Matrix4f.T -> Matrix4f.T
projective_matrix camera_view light_view light_projection =
  Matrix4f.mult (Matrix4f.mult light_projection light_view) (Matrix4f.inverse camera_view)