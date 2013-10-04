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

import nu.xom.Element;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.SBZipUtilities.BaseDirectory;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

public final class SBTextureCubeDescription
{
  private final @Nonnull String name;
  private final @Nonnull File   epzf;
  private final @Nonnull File   enzf;
  private final @Nonnull File   epyf;
  private final @Nonnull File   enyf;
  private final @Nonnull File   epxf;
  private final @Nonnull File   enxf;

  public SBTextureCubeDescription(
    final @Nonnull String name,
    final @Nonnull File epzf,
    final @Nonnull File enzf,
    final @Nonnull File epyf,
    final @Nonnull File enyf,
    final @Nonnull File epxf,
    final @Nonnull File enxf)
  {
    this.name = name;
    this.epzf = epzf;
    this.enzf = enzf;
    this.epyf = epyf;
    this.enyf = enyf;
    this.epxf = epxf;
    this.enxf = enxf;
  }

  static @Nonnull SBTextureCubeDescription fromXML(
    final @CheckForNull BaseDirectory base,
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    RXMLUtilities.checkIsElement(e, "texture-cube", uri);
    final Element en = RXMLUtilities.getChild(e, "name", uri);
    final Element epz = RXMLUtilities.getChild(e, "positive-z", uri);
    final Element enz = RXMLUtilities.getChild(e, "negative-z", uri);
    final Element epx = RXMLUtilities.getChild(e, "positive-x", uri);
    final Element enx = RXMLUtilities.getChild(e, "negative-x", uri);
    final Element epy = RXMLUtilities.getChild(e, "positive-y", uri);
    final Element eny = RXMLUtilities.getChild(e, "negative-y", uri);

    final String name = RXMLUtilities.getElementNonEmptyString(en);
    final String epzn = RXMLUtilities.getElementNonEmptyString(epz);
    final String enzn = RXMLUtilities.getElementNonEmptyString(enz);
    final String epxn = RXMLUtilities.getElementNonEmptyString(epx);
    final String enxn = RXMLUtilities.getElementNonEmptyString(enx);
    final String epyn = RXMLUtilities.getElementNonEmptyString(epy);
    final String enyn = RXMLUtilities.getElementNonEmptyString(eny);

    final File epzf =
      (base != null)
        ? new File(new File(base.getFile(), name), epzn)
        : new File(name, epzn);
    final File enzf =
      (base != null)
        ? new File(new File(base.getFile(), name), enzn)
        : new File(name, enzn);
    final File epyf =
      (base != null)
        ? new File(new File(base.getFile(), name), epyn)
        : new File(name, epyn);
    final File enyf =
      (base != null)
        ? new File(new File(base.getFile(), name), enyn)
        : new File(name, enyn);
    final File epxf =
      (base != null)
        ? new File(new File(base.getFile(), name), epxn)
        : new File(name, epxn);
    final File enxf =
      (base != null)
        ? new File(new File(base.getFile(), name), enxn)
        : new File(name, enxn);

    return new SBTextureCubeDescription(
      name,
      epzf,
      enzf,
      epyf,
      enyf,
      epxf,
      enxf);
  }

  @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:texture-cube", uri);
    final Element en = new Element("s:name", uri);
    final Element epz = new Element("s:positive-z", uri);
    final Element enz = new Element("s:negative-z", uri);
    final Element epy = new Element("s:positive-y", uri);
    final Element eny = new Element("s:negative-y", uri);
    final Element epx = new Element("s:positive-x", uri);
    final Element enx = new Element("s:negative-x", uri);

    en.appendChild(this.name);
    epz.appendChild(this.epzf.toString());
    epy.appendChild(this.epyf.toString());
    epx.appendChild(this.epxf.toString());
    enz.appendChild(this.enzf.toString());
    eny.appendChild(this.enyf.toString());
    enx.appendChild(this.enxf.toString());

    e.appendChild(en);
    e.appendChild(epz);
    e.appendChild(epy);
    e.appendChild(epx);
    e.appendChild(enz);
    e.appendChild(eny);
    e.appendChild(enx);
    return e;
  }
}
