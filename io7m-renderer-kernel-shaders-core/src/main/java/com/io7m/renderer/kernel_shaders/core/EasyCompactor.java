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

package com.io7m.renderer.kernel_shaders.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jparasol.xml.Batch;
import com.io7m.jparasol.xml.PGLSLCompactor;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Utility for compacting directories of Parasol/GLSL shaders.
 */

public final class EasyCompactor
{
  /**
   * Compact all of the given shaders.
   * 
   * @param log
   *          A log interface.
   * @param threads
   *          The number of threads to use.
   * @param batch
   *          A batch file containing a list of shaders.
   * @param out_dir
   *          The directory containing the uncompacted shaders.
   * @param out_compact_dir
   *          A directory containing the resulting compacted shaders.
   * @throws IOException
   *           If an I/O error occurs.
   * @throws InterruptedException
   *           If something interrupts execution.
   * @throws ExecutionException
   *           If an error occurs during task execution.
   * @throws TimeoutException
   *           If execution times out.
   */

  public static void compact(
    final LogUsableType log,
    final int threads,
    final File batch,
    final File out_dir,
    final File out_compact_dir)
    throws IOException,
      InterruptedException,
      ExecutionException,
      TimeoutException
  {
    final Batch b = Batch.fromFile(out_dir, batch);
    final List<Future<Unit>> futures = new ArrayList<Future<Unit>>();
    final ExecutorService exec = Executors.newFixedThreadPool(threads);

    try {
      final List<Pair<String, String>> targets = b.getTargets();
      for (int index = 0; index < targets.size(); ++index) {
        final Pair<String, String> k = targets.get(index);

        futures.add(exec.submit(new Callable<Unit>() {
          @Override public Unit call()
            throws Exception
          {
            final File program_in = new File(out_dir, k.getLeft());
            final File program_out = new File(out_compact_dir, k.getLeft());
            log.info("compact: " + program_in);
            PGLSLCompactor.newCompactor(program_in, program_out, log);
            return Unit.unit();
          }
        }));
      }

      for (int index = 0; index < futures.size(); ++index) {
        final Future<Unit> f = futures.get(index);
        f.get(10, TimeUnit.SECONDS);
      }

      log.info("compactions completed");
    } finally {
      exec.shutdown();
    }
  }

  private EasyCompactor()
  {
    throw new UnreachableCodeException();
  }
}
