/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.renderer.kernel;

import java.net.URI;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Element;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable public final class SBTextureCubeDescription
{
  public static @Nonnull SBTextureCubeDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    RXMLUtilities.checkIsElement(e, "texture-cube", uri);
    final Element ep = RXMLUtilities.getChild(e, "path", uri);
    final Element ewr = RXMLUtilities.getChild(e, "wrap-mode-r", uri);
    final Element ews = RXMLUtilities.getChild(e, "wrap-mode-s", uri);
    final Element ewt = RXMLUtilities.getChild(e, "wrap-mode-t", uri);
    final Element emi = RXMLUtilities.getChild(e, "minification", uri);
    final Element ema = RXMLUtilities.getChild(e, "magnification", uri);

    final PathVirtual npath =
      PathVirtual.ofString(RXMLUtilities.getElementNonEmptyString(ep));
    final TextureWrapR nwrap_mode_r =
      TextureWrapR.valueOf(RXMLUtilities.getElementNonEmptyString(ewr));
    final TextureWrapS nwrap_mode_s =
      TextureWrapS.valueOf(RXMLUtilities.getElementNonEmptyString(ews));
    final TextureWrapT nwrap_mode_t =
      TextureWrapT.valueOf(RXMLUtilities.getElementNonEmptyString(ewt));
    final TextureFilterMinification ntexture_min =
      TextureFilterMinification.valueOf(RXMLUtilities
        .getElementNonEmptyString(emi));
    final TextureFilterMagnification ntexture_mag =
      TextureFilterMagnification.valueOf(RXMLUtilities
        .getElementNonEmptyString(ema));

    return new SBTextureCubeDescription(
      npath,
      nwrap_mode_r,
      nwrap_mode_s,
      nwrap_mode_t,
      ntexture_min,
      ntexture_mag);
  }

  private final @Nonnull PathVirtual                path;
  private final @Nonnull TextureWrapR               wrap_mode_r;
  private final @Nonnull TextureWrapS               wrap_mode_s;
  private final @Nonnull TextureWrapT               wrap_mode_t;
  private final @Nonnull TextureFilterMinification  texture_min;
  private final @Nonnull TextureFilterMagnification texture_mag;

  SBTextureCubeDescription(
    final @Nonnull PathVirtual path,
    final @Nonnull TextureWrapR wrap_mode_r,
    final @Nonnull TextureWrapS wrap_mode_s,
    final @Nonnull TextureWrapT wrap_mode_t,
    final @Nonnull TextureFilterMinification texture_min,
    final @Nonnull TextureFilterMagnification texture_mag)
    throws ConstraintError
  {
    this.path = Constraints.constrainNotNull(path, "Path");
    this.wrap_mode_r =
      Constraints.constrainNotNull(wrap_mode_r, "Wrap R mode");
    this.wrap_mode_s =
      Constraints.constrainNotNull(wrap_mode_s, "Wrap S mode");
    this.wrap_mode_t =
      Constraints.constrainNotNull(wrap_mode_t, "Wrap T mode");
    this.texture_min =
      Constraints.constrainNotNull(texture_min, "Texture minification");
    this.texture_mag =
      Constraints.constrainNotNull(texture_mag, "Texture magnification");
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final SBTextureCubeDescription other = (SBTextureCubeDescription) obj;
    if (this.path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!this.path.equals(other.path)) {
      return false;
    }
    if (this.texture_mag != other.texture_mag) {
      return false;
    }
    if (this.texture_min != other.texture_min) {
      return false;
    }
    if (this.wrap_mode_r != other.wrap_mode_r) {
      return false;
    }
    if (this.wrap_mode_s != other.wrap_mode_s) {
      return false;
    }
    if (this.wrap_mode_t != other.wrap_mode_t) {
      return false;
    }
    return true;
  }

  public PathVirtual getPath()
  {
    return this.path;
  }

  public TextureFilterMagnification getTextureMag()
  {
    return this.texture_mag;
  }

  public TextureFilterMinification getTextureMin()
  {
    return this.texture_min;
  }

  public TextureWrapR getWrapModeR()
  {
    return this.wrap_mode_r;
  }

  public TextureWrapS getWrapModeS()
  {
    return this.wrap_mode_s;
  }

  public TextureWrapT getWrapModeT()
  {
    return this.wrap_mode_t;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.path.hashCode();
    result = (prime * result) + this.texture_mag.hashCode();
    result = (prime * result) + this.texture_min.hashCode();
    result = (prime * result) + this.wrap_mode_r.hashCode();
    result = (prime * result) + this.wrap_mode_s.hashCode();
    result = (prime * result) + this.wrap_mode_t.hashCode();
    return result;
  }

  public @Nonnull Element toXML()
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    final Element e = new Element("s:texture-cube", uri.toString());
    RXMLUtilities.putElementString(e, "s", "path", this.path.toString(), uri);
    RXMLUtilities.putElementString(
      e,
      "s",
      "wrap-mode-r",
      this.wrap_mode_r.toString(),
      uri);
    RXMLUtilities.putElementString(
      e,
      "s",
      "wrap-mode-s",
      this.wrap_mode_s.toString(),
      uri);
    RXMLUtilities.putElementString(
      e,
      "s",
      "wrap-mode-t",
      this.wrap_mode_t.toString(),
      uri);
    RXMLUtilities.putElementString(
      e,
      "s",
      "minification",
      this.texture_min.toString(),
      uri);
    RXMLUtilities.putElementString(
      e,
      "s",
      "magnification",
      this.texture_mag.toString(),
      uri);

    return e;
  }
}
