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

import java.io.File;
import java.net.URI;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Element;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.SBZipUtilities.BaseDirectory;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable final class SBTexture2DDescription
{
  static @Nonnull SBTexture2DDescription fromXML(
    final @CheckForNull BaseDirectory base,
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    RXMLUtilities.checkIsElement(e, "texture-2d", uri);
    final Element ef = RXMLUtilities.getChild(e, "file", uri);
    final Element en = RXMLUtilities.getChild(e, "name", uri);
    final String f = RXMLUtilities.getElementNonEmptyString(ef);
    final String n = RXMLUtilities.getElementNonEmptyString(en);

    if (base == null) {
      return new SBTexture2DDescription(new File(f), n);
    }

    return new SBTexture2DDescription(new File(base.getFile(), f), n);
  }

  private final @Nonnull File   file;
  private final @Nonnull String name;

  SBTexture2DDescription(
    final @Nonnull File file,
    final @Nonnull String name)
  {
    this.file = file;
    this.name = name;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBTexture2DDescription ");
    builder.append(this.file);
    builder.append(" ");
    builder.append(this.name);
    builder.append("]");
    return builder.toString();
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
    final SBTexture2DDescription other = (SBTexture2DDescription) obj;
    if (!this.file.equals(other.file)) {
      return false;
    }
    if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Nonnull File getFile()
  {
    return this.file;
  }

  @Nonnull String getName()
  {
    return this.name;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.file.hashCode();
    result = (prime * result) + this.name.hashCode();
    return result;
  }

  @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:texture-2d", uri);
    final Element ef = new Element("s:file", uri);
    ef.appendChild(this.file.toString());
    final Element en = new Element("s:name", uri);
    en.appendChild(this.getName());
    e.appendChild(ef);
    e.appendChild(en);
    return e;
  }
}
