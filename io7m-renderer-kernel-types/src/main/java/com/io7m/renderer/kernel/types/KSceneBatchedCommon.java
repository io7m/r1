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

package com.io7m.renderer.kernel.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * Utility functions for scene batching.
 */

@EqualityReference final class KSceneBatchedCommon
{
  static Map<String, Set<KInstanceOpaqueType>> makeUnlitBatches(
    final Set<KInstanceOpaqueType> unlits)
    throws RException,
      JCGLException
  {
    final Map<String, Set<KInstanceOpaqueType>> batches =
      new HashMap<String, Set<KInstanceOpaqueType>>();

    for (final KInstanceOpaqueType o : unlits) {
      o
        .opaqueAccept(new KInstanceOpaqueVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit regular(
            final KInstanceOpaqueRegular or)
          {
            final KMaterialOpaqueRegular material = or.getMaterial();

            final String code = material.materialGetCode();
            Set<KInstanceOpaqueType> batch_instances;
            if (batches.containsKey(code)) {
              batch_instances = batches.get(code);
            } else {
              batch_instances = new HashSet<KInstanceOpaqueType>();
              batches.put(code, batch_instances);
            }

            batch_instances.add(or);
            return Unit.unit();
          }
        });
    }

    return batches;
  }

  private KSceneBatchedCommon()
  {
    throw new UnreachableCodeException();
  }
}
