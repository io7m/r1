--
-- Copyright © 2013 <code@io7m.com> http://io7m.com
-- 
-- Permission to use, copy, modify, and/or distribute this software for any
-- purpose with or without fee is hereby granted, provided that the above
-- copyright notice and this permission notice appear in all copies.
-- 
-- THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
-- WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
-- MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
-- SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
-- WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
-- ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
-- IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
--

package com.io7m.renderer;

module Materials is

  --
  -- Material information relating to diffuse terms
  --

  type diffuse is record
    colour : vector_4f -- The base surface diffuse colour
    mix    : float     -- The linear mix between the diffuse colour and texture, in the range [0, 1]
  end;

  --
  -- Material information relating to specular terms
  --

  type specular is record
    exponent  : float, -- The specular exponent in the range [1, 127]
    intensity : float  -- The specular intensity in the range [0, 1]
  end;

  --
  -- Material information relating to environment mapping
  --

  type environment is record
    mix : float -- The linear mix between the environment map and the diffuse mix, in the range [0, 1]
  end;

  --
  -- The type of surface materials.
  --

  type t is record
    diffuse     : diffuse,
    specular    : specular,
    environment : environment
  end;

end;
