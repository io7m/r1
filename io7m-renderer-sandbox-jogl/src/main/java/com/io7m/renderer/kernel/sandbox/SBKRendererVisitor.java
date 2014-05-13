package com.io7m.renderer.kernel.sandbox;

import com.io7m.renderer.kernel.KRendererDebugType;
import com.io7m.renderer.kernel.KRendererDeferredType;
import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.types.RException;

public interface SBKRendererVisitor
{
  void visitDebug(
    final KRendererDebugType r)
    throws RException;

  void visitDeferred(
    final KRendererDeferredType r)
    throws RException;

  void visitForward(
    final KRendererForwardType r)
    throws RException;
}
