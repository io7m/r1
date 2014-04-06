package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;

public interface SBKRendererVisitor
{
  void visitDebug(
    final @Nonnull KRendererDebugType r)
    throws RException,
      ConstraintError;

  void visitDeferred(
    final @Nonnull KRendererDeferredType r)
    throws RException,
      ConstraintError;

  void visitForward(
    final @Nonnull KRendererForwardType r)
    throws RException,
      ConstraintError;
}
