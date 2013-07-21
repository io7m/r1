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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Element;
import nu.xom.ValidityException;

import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;

@Immutable final class SBInstanceDescription
{
  static @Nonnull SBInstanceDescription fromXML(
    final @Nonnull Element e)
    throws ValidityException
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    SBXMLUtilities.checkIsElement(e, "instance", uri);

    final Element eid = SBXMLUtilities.getChild(e, "id", uri);
    final Element ep = SBXMLUtilities.getChild(e, "position", uri);
    final Element eo = SBXMLUtilities.getChild(e, "orientation", uri);
    final Element ed = SBXMLUtilities.getChild(e, "diffuse", uri);
    final Element en = SBXMLUtilities.getChild(e, "normal", uri);
    final Element es = SBXMLUtilities.getChild(e, "specular", uri);
    final Element em = SBXMLUtilities.getChild(e, "model", uri);
    final Element emm = SBXMLUtilities.getChild(e, "model-object", uri);

    final Integer id = SBXMLUtilities.getInteger(eid);

    final RVectorI3F<RSpaceWorld> position =
      SBXMLUtilities.getVector3f(ep, uri);
    final RVectorI3F<SBDegrees> orientation =
      SBXMLUtilities.getVector3f(eo, uri);

    final String diffuse =
      (ed.getValue().length() == 0) ? null : ed.getValue();
    final String normal =
      (en.getValue().length() == 0) ? null : en.getValue();
    final String specular =
      (es.getValue().length() == 0) ? null : es.getValue();

    final String model = SBXMLUtilities.getNonEmptyString(em);
    final String object = SBXMLUtilities.getNonEmptyString(emm);

    return new SBInstanceDescription(
      id,
      position,
      orientation,
      model,
      object,
      diffuse,
      normal,
      specular);
  }

  private final @Nonnull Integer                 id;
  private final @Nonnull RVectorI3F<RSpaceWorld> position;
  private final @Nonnull RVectorI3F<SBDegrees>   orientation;
  private final @Nonnull String                  model;
  private final @Nonnull String                  model_object;
  private final @CheckForNull String             diffuse;
  private final @CheckForNull String             normal;
  private final @CheckForNull String             specular;

  public SBInstanceDescription(
    final @Nonnull Integer id,
    final @Nonnull RVectorI3F<RSpaceWorld> position,
    final @Nonnull RVectorI3F<SBDegrees> orientation,
    final @Nonnull String model,
    final @Nonnull String model_object,
    final @CheckForNull String diffuse,
    final @CheckForNull String normal,
    final @CheckForNull String specular)
  {
    this.id = id;
    this.position = position;
    this.orientation = orientation;
    this.model = model;
    this.model_object = model_object;
    this.diffuse = diffuse;
    this.normal = normal;
    this.specular = specular;
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
    if (this.diffuse == null) {
      if (other.diffuse != null) {
        return false;
      }
    } else if (!this.diffuse.equals(other.diffuse)) {
      return false;
    }
    if (!this.id.equals(other.id)) {
      return false;
    }
    if (!this.model.equals(other.model)) {
      return false;
    }
    if (!this.model_object.equals(other.model_object)) {
      return false;
    }
    if (this.normal == null) {
      if (other.normal != null) {
        return false;
      }
    } else if (!this.normal.equals(other.normal)) {
      return false;
    }
    if (!this.orientation.equals(other.orientation)) {
      return false;
    }
    if (!this.position.equals(other.position)) {
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

  public @CheckForNull String getDiffuse()
  {
    return this.diffuse;
  }

  public @Nonnull Integer getID()
  {
    return this.id;
  }

  public @Nonnull String getModel()
  {
    return this.model;
  }

  public @Nonnull String getModelObject()
  {
    return this.model_object;
  }

  public @CheckForNull String getNormal()
  {
    return this.normal;
  }

  public @Nonnull RVectorI3F<SBDegrees> getOrientation()
  {
    return this.orientation;
  }

  public @Nonnull RVectorI3F<RSpaceWorld> getPosition()
  {
    return this.position;
  }

  public @CheckForNull String getSpecular()
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
    result = (prime * result) + this.id.hashCode();
    result = (prime * result) + this.model.hashCode();
    result = (prime * result) + this.model_object.hashCode();
    result =
      (prime * result) + ((this.normal == null) ? 0 : this.normal.hashCode());
    result = (prime * result) + this.orientation.hashCode();
    result = (prime * result) + this.position.hashCode();
    result =
      (prime * result)
        + ((this.specular == null) ? 0 : this.specular.hashCode());
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
    builder.append(this.model);
    builder.append(" ");
    builder.append(this.model_object);
    builder.append(" ");
    builder.append(this.diffuse);
    builder.append(" ");
    builder.append(this.normal);
    builder.append(" ");
    builder.append(this.specular);
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

    final Element emo = new Element("s:model", uri);
    emo.appendChild(this.model);
    final Element eme = new Element("s:model-object", uri);
    eme.appendChild(this.model_object);

    final Element ed = new Element("s:diffuse", uri);
    if (this.diffuse != null) {
      ed.appendChild(this.diffuse);
    }
    final Element en = new Element("s:normal", uri);
    if (this.normal != null) {
      en.appendChild(this.normal);
    }
    final Element es = new Element("s:specular", uri);
    if (this.specular != null) {
      es.appendChild(this.specular);
    }

    e.appendChild(eid);
    e.appendChild(eo);
    e.appendChild(ep);
    e.appendChild(emo);
    e.appendChild(eme);
    e.appendChild(ed);
    e.appendChild(en);
    e.appendChild(es);
    return e;
  }
}
