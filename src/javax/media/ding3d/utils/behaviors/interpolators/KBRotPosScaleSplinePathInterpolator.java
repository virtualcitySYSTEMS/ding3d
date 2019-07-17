/*
 * $RCSfile: KBRotPosScaleSplinePathInterpolator.java,v $
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 * $Revision: 1.4 $
 * $Date: 2007/02/09 17:20:11 $
 * $State: Exp $
 */

package javax.media.ding3d.utils.behaviors.interpolators;

import javax.media.ding3d.*;
import java.util.*;
import javax.media.ding3d.vecmath.*;


/**
 * KBRotPosScaleSplinePathInterpolator; A rotation and position path
 * interpolation behavior node using Kochanek-Bartels cubic splines
 * (also known as TCB or Tension-Continuity-Bias Splines).
 *
 * @since Java3D 1.2
 */

/**
 * KBRotPosScaleSplinePathInterpolator behavior.  This class defines a
 * behavior that varies the rotational, translational, and scale components
 * of its target TransformGroup by using the Kochanek-Bartels cubic spline
 * interpolation to interpolate among a series of key frames
 * (using the value generated by the specified Alpha object).  The
 * interpolated position, orientation, and scale are used to generate
 * a transform in the local coordinate system of this interpolator.
 */

public class KBRotPosScaleSplinePathInterpolator
                                        extends KBSplinePathInterpolator {

    private Transform3D    rotation    = new Transform3D();

    private Matrix4d       pitchMat    = new Matrix4d();   // pitch matrix
    private Matrix4d       bankMat     = new Matrix4d();   // bank matrix
    private Matrix4d       tMat        = new Matrix4d();   // transformation matrix
    private Matrix4d       sMat        = new Matrix4d();   // scale matrix
    //Quat4f         iQuat       = new Quat4f();     // interpolated quaternion
    private Vector3f       iPos        = new Vector3f();   // interpolated position
    private Point3f        iScale      = new Point3f();    // interpolated scale
    float          iHeading, iPitch, iBank;        // interpolated heading,
                                                   // pitch and bank

    KBCubicSplineCurve   cubicSplineCurve = new KBCubicSplineCurve();
    KBCubicSplineSegment cubicSplineSegments[];
    int                  numSegments;
    int                  currentSegmentIndex;
    KBCubicSplineSegment currentSegment;

    // non-public, default constructor used by cloneNode
    KBRotPosScaleSplinePathInterpolator() {
    }

    /**
      * Constructs a new KBRotPosScaleSplinePathInterpolator object that
      * varies the rotation, translation, and scale of the target
      * TransformGroup's transform.  At least 2 key frames are required for
      * this interpolator. The first key
      * frame's knot must have a value of 0.0 and the last knot must have a
      * value of 1.0.  An intermediate key frame with index k must have a
      * knot value strictly greater than the knot value of a key frame with
      * index less than k.
      * @param alpha the alpha object for this interpolator
      * @param target the TransformGroup node affected by this interpolator
      * @param axisOfTransform the transform that specifies the local
      * coordinate system in which this interpolator operates.
      * @param keys an array of key frames that defien the motion path
      */
    public KBRotPosScaleSplinePathInterpolator(Alpha alpha,
				       TransformGroup target,
				       Transform3D axisOfTransform,
				       KBKeyFrame keys[]) {
	super(alpha,target, axisOfTransform, keys);

        // Create a spline curve using the derived key frames
        cubicSplineCurve = new KBCubicSplineCurve(this.keyFrames);
        numSegments = cubicSplineCurve.numSegments;

    }

    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>TransformInterpolator.setTransformAxis(Transform3D)</code>
     */
    public void setAxisOfRotPosScale(Transform3D axisOfRotPosScale) {
        setTransformAxis(axisOfRotPosScale);
    }

    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>TransformInterpolator.getTransformAxis()</code>
     */
    public Transform3D getAxisOfRotPosScale() {
        return getTransformAxis();
    }

    /**
     * Set the key frame at the specified index to <code>keyFrame</code>
     * @param index Index of the key frame to change
     * @param keyFrame The new key frame
     */
    public void setKeyFrame( int index, KBKeyFrame keyFrame ) {
	super.setKeyFrame( index, keyFrame );

	// TODO Optimize this
        // Create a spline curve using the derived key frames
        cubicSplineCurve = new KBCubicSplineCurve(this.keyFrames);
        numSegments = cubicSplineCurve.numSegments;
    }

    /**
     * Set all the key frames
     * @param keyFrame The new key frames
     */
    public void setKeyFrames( KBKeyFrame[] keyFrame ) {
	super.setKeyFrames( keyFrame );

        // Create a spline curve using the derived key frames
        cubicSplineCurve = new KBCubicSplineCurve(this.keyFrames);
        numSegments = cubicSplineCurve.numSegments;
    }

    /**
     * Computes the new transform for this interpolator for a given
     * alpha value.
     *
     * @param alphaValue alpha value between 0.0 and 1.0
     * @param transform object that receives the computed transform for
     * the specified alpha value
     *
     * @since Java 3D 1.3
     */
    public void computeTransform(float alphaValue, Transform3D transform) {
	// compute the current value of u from alpha and the
	// determine lower and upper knot points
	computePathInterpolation( alphaValue );

	// Determine the segment within which we will be interpolating
	currentSegmentIndex = this.lowerKnot - 1;

	// if we are at the start of the curve
	if (currentSegmentIndex == 0 && currentU == 0f) {

	    iHeading = keyFrames[1].heading;
	    iPitch = keyFrames[1].pitch;
	    iBank = keyFrames[1].bank;
	    iPos.set(keyFrames[1].position);
	    iScale.set(keyFrames[1].scale);

            // if we are at the end of the curve
	} else if (currentSegmentIndex == (numSegments-1) &&
		   currentU == 1.0) {

	    iHeading = keyFrames[upperKnot].heading;
	    iPitch = keyFrames[upperKnot].pitch;
	    iBank = keyFrames[upperKnot].bank;
	    iPos.set(keyFrames[upperKnot].position);
	    iScale.set(keyFrames[upperKnot].scale);

            // if we are somewhere in between the curve
	} else {

	    // Get a reference to the current spline segment i.e. the
	    // one bounded by lowerKnot and upperKnot
	    currentSegment
		= cubicSplineCurve.getSegment(currentSegmentIndex);

	    // interpolate quaternions
	    iHeading = currentSegment.getInterpolatedHeading (currentU);
	    iPitch = currentSegment.getInterpolatedPitch (currentU);
	    iBank = currentSegment.getInterpolatedBank (currentU);

	    // interpolate position
	    currentSegment.getInterpolatedPositionVector(currentU,iPos);

	    // interpolate position
	    currentSegment.getInterpolatedScale(currentU,iScale);

	}

	// Generate a transformation matrix in tMat using interpolated
	// heading, pitch and bank
	pitchMat.setIdentity();
	pitchMat.rotX(-iPitch);
	bankMat.setIdentity();
	bankMat.rotZ(iBank);
	tMat.setIdentity();
	tMat.rotY(-iHeading);
	tMat.mul(pitchMat);
	tMat.mul(bankMat);

	// TODO: Handle Non-Uniform scale
	// Currently this interpolator does not handle non uniform scale
	// We cheat by just taking the x scale component

	// Scale the transformation matrix
	sMat.set((double)iScale.x);
	tMat.mul(sMat);

	// Set the translation components.
	tMat.m03 = iPos.x;
	tMat.m13 = iPos.y;
	tMat.m23 = iPos.z;
	rotation.set(tMat);

	// construct a Transform3D from:  axis * rotation * axisInverse
	transform.mul(axis, rotation);
	transform.mul(transform, axisInverse);
    }

    /**
     * Copies KBRotPosScaleSplinePathInterpolator information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P>
     *
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        KBRotPosScaleSplinePathInterpolator spline =
          new KBRotPosScaleSplinePathInterpolator();

        spline.duplicateNode(this, forceDuplicate);
        return spline;
    }

					    /**
     * Copies KBRotPosScaleSplinePathInterpolator information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P>
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
        super.duplicateNode(originalNode, forceDuplicate);

	KBRotPosScaleSplinePathInterpolator interpolator =
	    (KBRotPosScaleSplinePathInterpolator)originalNode;
        setAxisOfRotPosScale(interpolator.axis);
        target = interpolator.target;
        cubicSplineCurve = new KBCubicSplineCurve(interpolator.keyFrames);
        numSegments = cubicSplineCurve.numSegments;
    }
}


