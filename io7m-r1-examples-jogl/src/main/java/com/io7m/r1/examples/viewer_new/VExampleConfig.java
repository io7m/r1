/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.r1.examples.viewer_new;

import java.io.File;
import java.util.Properties;

import com.io7m.jnull.NullCheck;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;
import com.io7m.jproperties.JPropertyIncorrectType;
import com.io7m.r1.kernel.types.KNormalPrecision;

final class VExampleConfig
{
  public static VExampleConfig fromProperties(
    final Properties props)
    throws JPropertyException
  {
    return new VExampleConfig(props);
  }

  private final boolean          eclipse;
  private String                 program_version;
  private final File             replacement_results_directory;
  private final KNormalPrecision normal_precision;

  public VExampleConfig(
    final Properties in_props)
    throws JPropertyException
  {
    NullCheck.notNull(in_props, "Properties");

    this.eclipse = System.getenv("ECLIPSE") != null;
    if (this.eclipse) {
      final String v = System.getenv("ECLIPSE");
      final String r = "[0-9]+\\.[0-9]+\\.[0-9]+";
      if (v.matches(r)) {
        this.program_version = v;
      } else {
        throw new JPropertyIncorrectType("ECLIPSE variable must match: " + r);
      }
    } else {
      this.program_version =
        NullCheck.notNull(this
          .getClass()
          .getPackage()
          .getImplementationVersion(), "Version");
    }

    this.replacement_results_directory =
      new File(JProperties.getString(
        in_props,
        "com.io7m.r1.examples.viewer.replacement-results-directory"));
    this.normal_precision =
      KNormalPrecision.valueOf(JProperties.getStringOptional(
        in_props,
        "com.io7m.r1.examples.viewer.normal_precision",
        NullCheck.notNull(KNormalPrecision.NORMAL_PRECISION_16F.toString())));
  }

  public String getProgramVersion()
  {
    return this.program_version;
  }

  public File getReplacementResultsDirectory()
  {
    return this.replacement_results_directory;
  }

  public boolean isEclipse()
  {
    return this.eclipse;
  }

  public KNormalPrecision getNormalPrecision()
  {
    return this.normal_precision;
  }
}
