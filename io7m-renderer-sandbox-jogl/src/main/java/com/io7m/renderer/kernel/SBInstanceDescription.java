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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable final class SBInstanceDescription
{
  static @Nonnull SBInstanceDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    RXMLUtilities.checkIsElement(e, "instance", uri);

    final Element eid = RXMLUtilities.getChild(e, "id", uri);
    final Element ep = RXMLUtilities.getChild(e, "position", uri);
    final Element eo = RXMLUtilities.getChild(e, "orientation", uri);
    final Element em = RXMLUtilities.getChild(e, "mesh", uri);
    final Element emat = RXMLUtilities.getChild(e, "material", uri);

    final int id = RXMLUtilities.getElementInteger(eid);

    final RVectorI3F<RSpaceWorld> position =
      RXMLUtilities.getElementVector3f(ep, uri);
    final RVectorI3F<SBDegrees> orientation =
      RXMLUtilities.getElementVector3f(eo, uri);

    final PathVirtual mesh =
      PathVirtual.ofString(RXMLUtilities.getElementNonEmptyString(em));
    final SBMaterialDescription mat = SBMaterialDescription.fromXML(emat);

    return new SBInstanceDescription(
      Integer.valueOf(id),
      position,
      orientation,
      mesh,
      mat);
  }

  private final @Nonnull Integer                 id;
  private final @Nonnull RVectorI3F<RSpaceWorld> position;
  private final @Nonnull RVectorI3F<SBDegrees>   orientation;
  private final @Nonnull PathVirtual             mesh;
  private final @Nonnull SBMaterialDescription   material;

  public SBInstanceDescription(
    final @Nonnull Integer id,
    final @Nonnull RVectorI3F<RSpaceWorld> position,
    final @Nonnull RVectorI3F<SBDegrees> orientation,
    final @Nonnull PathVirtual mesh,
    final @Nonnull SBMaterialDescription material)
  {
    this.id = id;
    this.position = position;
    this.orientation = orientation;
    this.mesh = mesh;
    this.material = material;
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
    final SBInstanceDescription other = (SBInstanceDescription) obj;
    if (this.id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!this.id.equals(other.id)) {
      return false;
    }
    if (this.material == null) {
      if (other.material != null) {
        return false;
      }
    } else if (!this.material.equals(other.material)) {
      return false;
    }
    if (this.mesh == null) {
      if (other.mesh != null) {
        return false;
      }
    } else if (!this.mesh.equals(other.mesh)) {
      return false;
    }
    if (this.orientation == null) {
      if (other.orientation != null) {
        return false;
      }
    } else if (!this.orientation.equals(other.orientation)) {
      return false;
    }
    if (this.position == null) {
      if (other.position != null) {
        return false;
      }
    } else if (!this.position.equals(other.position)) {
      return false;
    }
    return true;
  }

  public @Nonnull Integer getID()
  {
    return this.id;
  }

  public @Nonnull PathVirtual getMesh()
  {
    return this.mesh;
  }

  public @Nonnull RVectorI3F<SBDegrees> getOrientation()
  {
    return this.orientation;
  }

  public @Nonnull RVectorI3F<RSpaceWorld> getPosition()
  {
    return this.position;
  }

  public @Nonnull SBMaterialDescription getMaterial()
  {
    return this.material;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
    result =
      (prime * result)
        + ((this.material == null) ? 0 : this.material.hashCode());
    result =
      (prime * result) + ((this.mesh == null) ? 0 : this.mesh.hashCode());
    result =
      (prime * result)
        + ((this.orientation == null) ? 0 : this.orientation.hashCode());
    result =
      (prime * result)
        + ((this.position == null) ? 0 : this.position.hashCode());
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBInstanceDescription ");
    builder.append(this.id);
    builder.append(" ");
    builder.append(this.position);
    builder.append(" ");
    builder.append(this.orientation);
    builder.append(" ");
    builder.append(this.mesh);
    builder.append(" ");
    builder.append(this.material);
    builder.append("]");
    return builder.toString();
  }

  @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:instance", uri);

    final Element eid = new Element("s:id", uri);
    eid.appendChild(this.getID().toString());

    final Element eo = new Element("s:orientation", uri);
    final Element eox = new Element("s:x", uri);
    eox.appendChild(Float.toString(this.orientation.getXF()));
    final Element eoy = new Element("s:y", uri);
    eoy.appendChild(Float.toString(this.orientation.getYF()));
    final Element eoz = new Element("s:z", uri);
    eoz.appendChild(Float.toString(this.orientation.getZF()));
    eo.appendChild(eox);
    eo.appendChild(eoy);
    eo.appendChild(eoz);

    final Element ep = new Element("s:position", uri);
    final Element epx = new Element("s:x", uri);
    epx.appendChild(Float.toString(this.position.getXF()));
    final Element epy = new Element("s:y", uri);
    epy.appendChild(Float.toString(this.position.getYF()));
    final Element epz = new Element("s:z", uri);
    epz.appendChild(Float.toString(this.position.getZF()));
    ep.appendChild(epx);
    ep.appendChild(epy);
    ep.appendChild(epz);

    final Element em = new Element("s:mesh", uri);
    em.appendChild(this.mesh.toString());

    final Element emat = this.material.toXML();

    e.appendChild(eid);
    e.appendChild(eo);
    e.appendChild(ep);
    e.appendChild(em);
    e.appendChild(emat);
    return e;
  }
}
