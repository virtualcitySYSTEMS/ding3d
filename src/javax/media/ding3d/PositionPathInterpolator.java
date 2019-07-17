/*
 * $RCSfile: PositionPathInterpolator.java,v $
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 1.5 $
 * $Date: 2008/02/28 20:17:28 $
 * $State: Exp $
 */

package javax.media.ding3d;

import javax.media.ding3d.vecmath.Point3f;
import javax.media.ding3d.vecmath.Vector3f;


/**
 * PositionPathInterpolator behavior.  This class defines a behavior
 * that modifies the translational component of its target TransformGroup
 * by linearly interpolating among a series of predefined knot/position
 * pairs (using the value generated by the specified Alpha object).  The
 * interpolated position is used to generate a translation transform
 * in the local coordinate system of this interpolator.  The first knot
 * must have a value of 0.0.  The last knot must have a value of 1.0.  An
 * intermediate knot with index k must have a value strictly greater
 * than any knot with index less than k.
 */

public class PositionPathInterpolator extends PathInterpolator {
    private Transform3D position = new Transform3D();
    private Vector3f pos = new Vector3f();

    // Array of positions at each knot
    private Point3f positions[];
    private float prevInterpolationValue = Float.NaN;

    // We can't use a boolean flag since it is possible
    // that after alpha change, this procedure only run
    // once at alpha.finish(). So the best way is to
    // detect alpha value change.
    private float prevAlphaValue = Float.NaN;
    private WakeupCriterion passiveWakeupCriterion =
    (WakeupCriterion) new WakeupOnElapsedFrames(0, true);

    // non-public, default constructor used by cloneNode
    PositionPathInterpolator() {
    }


    /**
     * Constructs a new PositionPathInterpolator that varies the transform
     * of the target TransformGroup.
     * @param alpha the alpha object for this interpolator
     * @param target the TransformGroup node affected by this translator
     * @param axisOfTransform the transform that defines the local
     * coordinate system in which this interpolator operates
     * @param knots an array of knot values that specify interpolation points.
     * @param positions an array of position values at the knots.
     * @exception IllegalArgumentException if the lengths of the
     * knots and positions arrays are not the same.
     */
    public PositionPathInterpolator(Alpha alpha,
				    TransformGroup target,
				    Transform3D axisOfTransform,
				    float[] knots,
				    Point3f[] positions) {

	super(alpha, target, axisOfTransform, knots);

	if (knots.length != positions.length)
	    throw new IllegalArgumentException(Ding3dI18N.getString("PositionPathInterpolator0"));
	setPathArrays(positions);
    }


    /**
     * Sets the position at the specified index for this
     * interpolator.
     * @param index the index of the position to be changed
     * @param position the new position at the index
     */
    public void setPosition(int index, Point3f position) {
	this.positions[index].set(position);
    }


    /**
     * Retrieves the position value at the specified index.
     * @param index the index of the value requested
     * @param position the variable to receive the position value at
     * the specified index
     */
    public void getPosition(int index, Point3f position) {
	position.set(this.positions[index]);
    }


    /**
     * Replaces the existing arrays of knot values
     * and position values with the specified arrays.
     * The arrays of knots and positions are copied
     * into this interpolator object.
     * @param knots a new array of knot values that specify
     * interpolation points
     * @param positions a new array of position values at the knots
     * @exception IllegalArgumentException if the lengths of the
     * knots and positions arrays are not the same.
     *
     * @since Java 3D 1.2
     */
    public void setPathArrays(float[] knots,
			      Point3f[] positions) {

	if (knots.length != positions.length)
	    throw new IllegalArgumentException(Ding3dI18N.getString("PositionPathInterpolator0"));

	setKnots(knots);
	setPathArrays(positions);
    }


    // Set the specific arrays for this path interpolator
    private void setPathArrays(Point3f[] positions) {

	this.positions = new Point3f[positions.length];
	for(int i = 0; i < positions.length; i++) {
	    this.positions[i] = new Point3f();
	    this.positions[i].set(positions[i]);
	}
    }


    /**
     * Copies the array of position values from this interpolator
     * into the specified array.
     * The array must be large enough to hold all of the positions.
     * The individual array elements must be allocated by the caller.
     * @param positions array that will receive the positions
     *
     * @since Java 3D 1.2
     */
    public void getPositions(Point3f[] positions) {
	for (int i = 0; i < this.positions.length; i++)  {
	    positions[i].set(this.positions[i]);
	}
    }

    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>TransformInterpolator.setTransformAxis(Transform3D)</code>
     */
    public void setAxisOfTranslation(Transform3D axisOfTranslation) {
        setTransformAxis(axisOfTranslation);
    }
    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>TransformInterpolator.getTransformAxis()</code>
     */
    public Transform3D getAxisOfTranslation() {
        return getTransformAxis();
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

	computePathInterpolation(alphaValue);

	if (currentKnotIndex == 0 &&
	    currentInterpolationValue == 0f) {
	    pos.x = positions[0].x;
	    pos.y = positions[0].y;
	    pos.z = positions[0].z;
	} else {
	    pos.x = positions[currentKnotIndex].x +
		(positions[currentKnotIndex+1].x -
		 positions[currentKnotIndex].x) * currentInterpolationValue;
	    pos.y = positions[currentKnotIndex].y +
		(positions[currentKnotIndex+1].y -
		 positions[currentKnotIndex].y) * currentInterpolationValue;
	    pos.z = positions[currentKnotIndex].z +
		(positions[currentKnotIndex+1].z -
		 positions[currentKnotIndex].z) * currentInterpolationValue;
	}
	position.setIdentity();
	position.setTranslation(pos);

	// construct a Transform3D from:  axis * position * axisInverse
	transform.mul(axis, position);
	transform.mul(transform, axisInverse);
    }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        PositionPathInterpolator ppi = new PositionPathInterpolator();
        ppi.duplicateNode(this, forceDuplicate);
        return ppi;
    }


   /**
     * Copies all PositionPathInterpolator information from
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
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
        super.duplicateAttributes(originalNode, forceDuplicate);

	PositionPathInterpolator pi =
	    (PositionPathInterpolator) originalNode;

        int len = pi.getArrayLengths();

	// No API available to set the size of positions
        positions = new Point3f[len];

        Point3f dupPoint = new Point3f();
        for (int i = 0; i < len; i++) {
            positions[i] = new Point3f();
            pi.getPosition(i, dupPoint);
            setPosition(i, dupPoint);
        }
    }
}