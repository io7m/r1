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
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable public final class SBMaterialDescription
{
  public static @Nonnull SBMaterialDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    RXMLUtilities.checkIsElement(e, "material", uri);

    final Element ed = RXMLUtilities.getChild(e, "diffuse", uri);
    final Element es = RXMLUtilities.getChild(e, "specular", uri);
    final Element en = RXMLUtilities.getChild(e, "normal", uri);
    final Element ee = RXMLUtilities.getChild(e, "environment", uri);

    return new SBMaterialDescription(
      SBMaterialDiffuseDescription.fromXML(ed),
      SBMaterialSpecularDescription.fromXML(es),
      SBMaterialEnvironmentDescription.fromXML(ee),
      SBMaterialNormalDescription.fromXML(en));
  }

  private final @Nonnull SBMaterialDiffuseDescription     diffuse;
  private final @Nonnull SBMaterialSpecularDescription    specular;
  private final @Nonnull SBMaterialEnvironmentDescription environment;
  private final @Nonnull SBMaterialNormalDescription      normal;

  SBMaterialDescription(
    final @Nonnull SBMaterialDiffuseDescription diffuse,
    final @Nonnull SBMaterialSpecularDescription specular,
    final @Nonnull SBMaterialEnvironmentDescription environment,
    final @Nonnull SBMaterialNormalDescription normal)
    throws ConstraintError
  {
    this.diffuse = Constraints.constrainNotNull(diffuse, "Diffuse");
    this.specular = Constraints.constrainNotNull(specular, "Specular");
    this.environment =
      Constraints.constrainNotNull(environment, "Environment");
    this.normal = Constraints.constrainNotNull(normal, "Normal");
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
    final SBMaterialDescription other = (SBMaterialDescription) obj;
    if (this.diffuse == null) {
      if (other.diffuse != null) {
        return false;
      }
    } else if (!this.diffuse.equals(other.diffuse)) {
      return false;
    }
    if (this.environment == null) {
      if (other.environment != null) {
        return false;
      }
    } else if (!this.environment.equals(other.environment)) {
      return false;
    }
    if (this.normal == null) {
      if (other.normal != null) {
        return false;
      }
    } else if (!this.normal.equals(other.normal)) {
      return false;
    }
    if (this.specular == null) {
      if (other.specular != null) {
        return false;
      }
    } else if (!this.specular.equals(other.specular)) {
      return false;
    }
    return true;
  }

  public @Nonnull SBMaterialDiffuseDescription getDiffuse()
  {
    return this.diffuse;
  }

  public @Nonnull SBMaterialEnvironmentDescription getEnvironment()
  {
    return this.environment;
  }

  public @Nonnull SBMaterialNormalDescription getNormal()
  {
    return this.normal;
  }

  public @Nonnull SBMaterialSpecularDescription getSpecular()
  {
    return this.specular;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result =
      (prime * result)
        + ((this.diffuse == null) ? 0 : this.diffuse.hashCode());
    result =
      (prime * result)
        + ((this.environment == null) ? 0 : this.environment.hashCode());
    result =
      (prime * result) + ((this.normal == null) ? 0 : this.normal.hashCode());
    result =
      (prime * result)
        + ((this.specular == null) ? 0 : this.specular.hashCode());
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialDescription [diffuse=");
    builder.append(this.diffuse);
    builder.append(", specular=");
    builder.append(this.specular);
    builder.append(", environment=");
    builder.append(this.environment);
    builder.append(", normal=");
    builder.append(this.normal);
    builder.append("]");
    return builder.toString();
  }

  @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:material", uri);

    e.appendChild(this.diffuse.toXML());
    e.appendChild(this.normal.toXML());
    e.appendChild(this.specular.toXML());
    e.appendChild(this.environment.toXML());
    return e;
  }
}
