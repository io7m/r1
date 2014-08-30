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

package com.io7m.renderer.kernel.sandbox;

import javax.swing.JComboBox;

import com.io7m.jcanephora.TextureFilterMagnification;

public final class SBTextureMagFilterSelector extends
  JComboBox<TextureFilterMagnification>
{
  private static final long serialVersionUID = -1080392482805214287L;

  public SBTextureMagFilterSelector()
  {
    for (final TextureFilterMagnification type : TextureFilterMagnification
      .values()) {
      this.addItem(type);
    }
    this.setSelectedItem(TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  }

  @Override public TextureFilterMagnification getSelectedItem()
  {
    return (TextureFilterMagnification) super.getSelectedItem();
  }
}