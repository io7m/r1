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
import nu.xom.ValidityException;

import com.io7m.renderer.kernel.SBZipUtilities.BaseDirectory;

@Immutable final class SBModelDescription
{
  static @Nonnull SBModelDescription fromXML(
    final @CheckForNull BaseDirectory base,
    final @Nonnull Element e)
    throws ValidityException
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    SBXMLUtilities.checkIsElement(e, "model", uri);
    final Element ef = SBXMLUtilities.getChild(e, "file", uri);
    final Element en = SBXMLUtilities.getChild(e, "name", uri);
    final String f = SBXMLUtilities.getNonEmptyString(ef);
    final String n = SBXMLUtilities.getNonEmptyString(en);

    if (base == null) {
      return new SBModelDescription(new File(f), n);
    }

    return new SBModelDescription(new File(base.getFile(), f), n);
  }

  private final @Nonnull File   file;
  private final @Nonnull String name;

  SBModelDescription(
    final @Nonnull File file,
    final @Nonnull String name)
  {
    this.file = file;
    this.name = name;
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
    final SBModelDescription other = (SBModelDescription) obj;
    if (!this.file.equals(other.file)) {
      return false;
    }
    if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBModelDescription ");
    builder.append(this.file);
    builder.append(" ");
    builder.append(this.name);
    builder.append("]");
    return builder.toString();
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
    final Element e = new Element("s:model", uri);
    final Element ef = new Element("s:file", uri);
    ef.appendChild(this.file.toString());
    final Element en = new Element("s:name", uri);
    en.appendChild(this.getName());
    e.appendChild(ef);
    e.appendChild(en);
    return e;
  }
}
