/*
 * $RCSfile: GeometryDecompressorShape3D.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/02/09 17:20:22 $
 * $State: Exp $
 */

package javax.media.ding3d.utils.geometry.compression;

import javax.media.ding3d.internal.Ding3dUtilsI18N;
import java.util.ArrayList;
import javax.media.ding3d.Appearance;
import javax.media.ding3d.GeometryArray;
import javax.media.ding3d.GeometryStripArray;
import javax.media.ding3d.LineStripArray;
import javax.media.ding3d.Material;
import javax.media.ding3d.PointArray;
import javax.media.ding3d.Shape3D;
import javax.media.ding3d.TriangleArray;
import javax.media.ding3d.TriangleStripArray;
import javax.media.ding3d.vecmath.Color4f;
import javax.media.ding3d.vecmath.Point3f;
import javax.media.ding3d.vecmath.Vector3f;

/**
 * This class implements a Shape3D backend for the abstract
 * GeometryDecompressor.
 */
class GeometryDecompressorShape3D extends GeometryDecompressor {
    private static final boolean debug = false ;
    private static final boolean benchmark = false ;
    private static final boolean statistics = false ;
    private static final boolean printInfo = debug || benchmark || statistics ;

    // Type of connections in the compressed data:
    // TYPE_POINT (1), TYPE_LINE (2), or TYPE_TRIANGLE (4).
    private int bufferDataType ;

    // Data bundled with each vertex: bitwise combination of
    // NORMAL_IN_BUFFER (1), COLOR_IN_BUFFER (2), ALPHA_IN_BUFFER (4).
    private int dataPresent ;

    // List for accumulating the output of the decompressor and converting to
    // GeometryArray representations.
    private GeneralizedVertexList vlist ;

    // Accumulates Shape3D objects constructed from decompressor output.
    private ArrayList shapes ;

    // Decompressor output state variables.
    private Color4f curColor ;
    private Vector3f curNormal ;

    // Variables for gathering statistics.
    private int origVertexCount ;
    private int stripCount ;
    private int vertexCount ;
    private int triangleCount ;
    private long startTime ;
    private long endTime ;

    // Triangle array type to construct.
    private int triOutputType ;

    // Types of triangle output available.
    private static final int TRI_SET = 0 ;
    private static final int TRI_STRIP_SET = 1 ;
    private static final int TRI_STRIP_AND_FAN_SET = 2 ;
    private static final int TRI_STRIP_AND_TRI_SET = 3 ;

    // Private convenience copies of various constants.
    private static final int TYPE_POINT =
	CompressedGeometryRetained.TYPE_POINT ;
    private static final int TYPE_LINE =
	CompressedGeometryRetained.TYPE_LINE ;
    private static final int TYPE_TRIANGLE =
	CompressedGeometryRetained.TYPE_TRIANGLE ;
    private static final int FRONTFACE_CCW =
	GeneralizedStripFlags.FRONTFACE_CCW ;

    /**
     * Decompress the given compressed geometry.
     * @param cgr CompressedGeometryRetained object with compressed geometry
     * @return an array of Shape3D with TriangleArray geometry if compressed
     * data contains triangles; otherwise, Shape3D array containing PointArray
     * or LineStripArray geometry
     * @see CompressedGeometry
     * @see GeometryDecompressor
     */
    Shape3D[] toTriangleArrays(CompressedGeometryRetained cgr) {
	return decompress(cgr, TRI_SET) ;
    }


    /**
     * Decompress the given compressed geometry.
     * @param cgr CompressedGeometryRetained object with compressed geometry
     * @return an array of Shape3D with TriangleStripArray geometry if
     * compressed data contains triangles; otherwise, Shape3D array containing
     * PointArray or LineStripArray geometry
     * @see CompressedGeometry
     * @see GeometryDecompressor
     */
    Shape3D[] toTriangleStripArrays(CompressedGeometryRetained cgr) {
	return decompress(cgr, TRI_STRIP_SET) ;
    }


    /**
     * Decompress the given compressed geometry.
     * @param cgr CompressedGeometryRetained object with compressed geometry
     * @return an array of Shape3D with TriangleStripArray and
     * TriangleFanArray geometry if compressed data contains triangles;
     * otherwise, Shape3D array containing PointArray or LineStripArray
     * geometry
     * @see CompressedGeometry
     * @see GeometryDecompressor
     */
    Shape3D[] toStripAndFanArrays(CompressedGeometryRetained cgr) {
	return decompress(cgr, TRI_STRIP_AND_FAN_SET) ;
    }


    /**
     * Decompress the given compressed geometry.
     * @param cgr CompressedGeometryRetained object with compressed geometry
     * @return an array of Shape3D with TriangleStripArray and
     * TriangleArray geometry if compressed data contains triangles;
     * otherwise, Shape3D array containing PointArray or LineStripArray
     * geometry
     * @see CompressedGeometry
     * @see GeometryDecompressor
     */
    Shape3D[] toStripAndTriangleArrays(CompressedGeometryRetained cgr) {
	return decompress(cgr, TRI_STRIP_AND_TRI_SET) ;
    }

    /**
     * Decompress the data contained in a CompressedGeometryRetained and
     * return an array of Shape3D objects using the specified triangle output
     * type.  The triangle output type is ignored if the compressed data
     * contains points or lines.
     */
    private Shape3D[] decompress(CompressedGeometryRetained cgr,
				 int triOutputType) {

	if (! checkVersion(cgr.majorVersionNumber, cgr.minorVersionNumber)) {
	    return null ;
	}

	vlist = null ;
	curColor = null ;
	curNormal = null ;

	// Get descriptors for compressed data.
	bufferDataType = cgr.bufferType ;
	dataPresent = cgr.bufferContents ;
	if (printInfo) beginPrint() ;

	// Initialize the decompressor backend.
	this.triOutputType = triOutputType ;
	shapes = new ArrayList() ;

	// Call the superclass decompress() method which calls the output
	// methods of this subclass.  The results are stored in vlist.
	super.decompress(cgr.offset, cgr.size, cgr.compressedGeometry) ;

	// Convert the decompressor output to Shape3D objects.
	addShape3D() ;
	if (printInfo) endPrint() ;

	// Return the fixed-length output array.
	Shape3D shapeArray[] = new Shape3D[shapes.size()] ;
	return (Shape3D[])shapes.toArray(shapeArray) ;
    }

    /**
     * Initialize the vertex output list based on the vertex format provided
     * by the SetState decompression command.
     */
    void outputVertexFormat(boolean bundlingNorm, boolean bundlingColor,
			    boolean doingAlpha) {

	if (vlist != null)
	    // Construct shapes using the current vertex format.
	    addShape3D() ;

	int vertexFormat = GeometryArray.COORDINATES ;

	if (bundlingNorm) {
            vertexFormat |= GeometryArray.NORMALS ;
        }

        if (bundlingColor) {
            if (doingAlpha) {
                vertexFormat |= GeometryArray.COLOR_4;
            } else {
                vertexFormat |= GeometryArray.COLOR_3;
            }
        }

	vlist = new GeneralizedVertexList(vertexFormat, FRONTFACE_CCW) ;
    }

    /**
     * Add a new decompressed vertex to the current list.
     */
    void outputVertex(Point3f position, Vector3f normal,
		      Color4f color, int vertexReplaceCode) {

	if (curNormal != null) normal = curNormal ;
	vlist.addVertex(position, normal, color, vertexReplaceCode) ;

	if (debug) {
	    System.out.println(" outputVertex: flag " + vertexReplaceCode) ;
	    System.out.println("  position " + position.toString()) ;
	    if (normal != null)
		System.out.println("  normal " + normal.toString()) ;
	    if (color != null)
		System.out.println("  color " + color.toString()) ;
	}
    }

    /**
     * Create a Shape3D using the current color for both the ambient and
     * diffuse material colors, then start a new vertex list for the new
     * color.  The outputColor() method is never called if colors are bundled
     * with each vertex in the compressed buffer.
     */
    void outputColor(Color4f color) {
	if (debug) System.out.println(" outputColor: " + color.toString()) ;

	if (vlist.size() > 0) {
	    // Construct Shape3D using the current color.
	    addShape3D() ;

	    // Start a new vertex list for the new color.
	    vlist = new GeneralizedVertexList(vlist.vertexFormat,
					      FRONTFACE_CCW) ;
	}
	if (curColor == null) curColor = new Color4f() ;
	curColor.set(color) ;
    }

    /**
     * Set the current normal that will be copied to each succeeding vertex
     * output by the decompressor.  The per-vertex copy is needed since in
     * Java 3D a normal is always associated with a vertex.  This method is
     * never called if normals are bundled with each vertex in the compressed
     * buffer.
     */
    void outputNormal(Vector3f normal) {
	if (debug) System.out.println(" outputNormal: " + normal.toString()) ;

	if ((vlist.vertexFormat & GeometryArray.NORMALS) == 0) {
	    if (vlist.size() > 0)
		// Construct Shape3D using the current vertex format.
		addShape3D() ;

	    // Start a new vertex list with the new format.
	    vlist = new GeneralizedVertexList
		(vlist.vertexFormat|GeometryArray.NORMALS, FRONTFACE_CCW) ;
	}
	if (curNormal == null) curNormal = new Vector3f() ;
	curNormal.set(normal) ;
    }

    /**
     * Create a Shape3D object of the desired type from the current vertex
     * list.  Apply the current color, if non-null, as a Material attribute.
     */
    private void addShape3D() {
	Material m = new Material() ;

	if (curColor != null) {
	    if ((vlist.vertexFormat & GeometryArray.COLOR_4) != GeometryArray.COLOR_4) {
		m.setAmbientColor(curColor.x, curColor.y, curColor.z) ;
		m.setDiffuseColor(curColor.x, curColor.y, curColor.z) ;
	    }
	    else {
		m.setAmbientColor(curColor.x, curColor.y, curColor.z) ;
		m.setDiffuseColor(curColor.x, curColor.y, curColor.z,
				  curColor.w) ;
	    }
	}

	if ((vlist.vertexFormat & GeometryArray.NORMALS) == 0)
	    m.setLightingEnable(false) ;
	else
	    m.setLightingEnable(true) ;

	Appearance a = new Appearance() ;
	a.setMaterial(m) ;

	switch(bufferDataType) {
	  case TYPE_TRIANGLE:
	    switch(triOutputType) {
	      case TRI_SET:
		TriangleArray ta = vlist.toTriangleArray() ;
		if (ta != null)
		    shapes.add(new Shape3D(ta, a)) ;
		break ;
	      case TRI_STRIP_SET:
		TriangleStripArray tsa = vlist.toTriangleStripArray() ;
		if (tsa != null)
		    shapes.add(new Shape3D(tsa, a)) ;
		break ;
	      case TRI_STRIP_AND_FAN_SET:
		GeometryStripArray gsa[] = vlist.toStripAndFanArrays() ;
		if (gsa[0] != null)
		    shapes.add(new Shape3D(gsa[0], a)) ;
		if (gsa[1] != null)
		    shapes.add(new Shape3D(gsa[1], a)) ;
		break ;
	      case TRI_STRIP_AND_TRI_SET:
		GeometryArray ga[] = vlist.toStripAndTriangleArrays() ;
		if (ga[0] != null)
		    shapes.add(new Shape3D(ga[0], a)) ;
		if (ga[1] != null)
		    shapes.add(new Shape3D(ga[1], a)) ;
		break ;
	      default:
		throw new IllegalArgumentException
		    (Ding3dUtilsI18N.getString("GeometryDecompressorShape3D0")) ;
	    }
 	    break ;

	  case TYPE_LINE:
	    LineStripArray lsa = vlist.toLineStripArray() ;
	    if (lsa != null)
		shapes.add(new Shape3D(lsa, a)) ;
	    break ;

	  case TYPE_POINT:
	    PointArray pa = vlist.toPointArray() ;
	    if (pa != null)
		shapes.add(new Shape3D(pa, a)) ;
	    break ;

	  default:
	    throw new IllegalArgumentException
		(Ding3dUtilsI18N.getString("GeometryDecompressorShape3D1")) ;
	}

	if (benchmark  || statistics) {
	    origVertexCount += vlist.size() ;
	    vertexCount += vlist.vertexCount ;
	    stripCount += vlist.stripCount ;
	    triangleCount += vlist.triangleCount ;
	}
    }

    private void beginPrint() {
	System.out.println("\nGeometryDecompressorShape3D") ;

	switch(bufferDataType) {
	  case TYPE_TRIANGLE:
	    System.out.println(" buffer TYPE_TRIANGLE") ;
 	    break ;
	  case TYPE_LINE:
	    System.out.println(" buffer TYPE_LINE") ;
	    break ;
	  case TYPE_POINT:
	    System.out.println(" buffer TYPE_POINT") ;
	    break ;
	  default:
	    throw new IllegalArgumentException
		(Ding3dUtilsI18N.getString("GeometryDecompressorShape3D1")) ;
	}

	System.out.print(" buffer data present: coords") ;

	if ((dataPresent & CompressedGeometryData.Header.NORMAL_IN_BUFFER) != 0)
	    System.out.print(" normals") ;
	if ((dataPresent & CompressedGeometryData.Header.COLOR_IN_BUFFER) != 0)
	    System.out.print(" colors") ;
	if ((dataPresent & CompressedGeometryData.Header.ALPHA_IN_BUFFER) != 0)
	    System.out.print(" alpha") ;

	System.out.println() ;

	stripCount = 0 ;
	vertexCount = 0 ;
	triangleCount = 0 ;
	origVertexCount = 0 ;

	startTime = System.currentTimeMillis() ;
    }

    private void endPrint() {
	endTime = System.currentTimeMillis() ;

	if (benchmark || statistics)
	    printBench() ;

	if (statistics)
	    printStats() ;
    }

    private void printBench() {
	float t = (endTime - startTime) / 1000.0f ;
	System.out.println
	    (" decompression + strip conversion took " + t + " sec.") ;

	switch(bufferDataType) {
  	  case TYPE_POINT:
	    System.out.println
		(" points decompressed: " + vertexCount + "\n" +
		 " net decompression rate: " + (vertexCount/t) +
		 " points/sec.\n") ;
	    break ;
	  case TYPE_LINE:
	    System.out.println
		(" lines decompressed: " + (vertexCount - stripCount) + "\n" +
		 " net decompression rate: " + ((vertexCount - stripCount)/t) +
		 " lines/sec.\n") ;
	    break ;
	  case TYPE_TRIANGLE:
	    System.out.println
		(" triangles decompressed: " +
		   (vertexCount - 2*stripCount) + "\n" +
		 " net decompression rate: " +
		   ((vertexCount - 2*stripCount)/t) + " triangles/sec.\n") ;
	    break ;
	}
    }

    private void printStats() {
	switch(triOutputType) {
	  case TRI_SET:
	    System.out.println(" using individual triangle output") ;
	    break ;
	  case TRI_STRIP_SET:
	    System.out.println(" using strip output") ;
	    break ;
	  case TRI_STRIP_AND_FAN_SET:
	    System.out.println(" using strips and fans for output") ;
	    break ;
	  case TRI_STRIP_AND_TRI_SET:
	    System.out.println(" using strips and triangles for output") ;
	    break ;
	}

	System.out.print
	    (" number of Shape3D objects: " + shapes.size() +
	     "\n number of Shape3D decompressed vertices: ") ;

	if (triOutputType == TRI_SET || bufferDataType == TYPE_POINT) {
	    System.out.println(vertexCount) ;
	}
	else if (triOutputType == TRI_STRIP_AND_TRI_SET) {
	    System.out.println((vertexCount + triangleCount*3) +
			       "\n number of strips: " + stripCount +
			       "\n number of individual triangles: " +
			       triangleCount) ;
	    if (stripCount > 0)
		System.out.println
		    (" vertices/strip: " + (float)vertexCount/stripCount +
		     "\n triangles represented in strips: " +
		     (vertexCount - 2*stripCount)) ;
	}
	else {
	    System.out.println(vertexCount +
			       "\n number of strips: " + stripCount) ;
	    if (stripCount > 0)
		System.out.println
		    (" vertices/strip: " + (float)vertexCount/stripCount) ;
	}

	System.out.print(" vertex data present in last Shape3D: coords") ;
	if ((vlist.vertexFormat & GeometryArray.NORMALS) != 0)
	    System.out.print(" normals") ;

        boolean color4 =
                (vlist.vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4;
        boolean color3 = !color4 &&
                (vlist.vertexFormat & GeometryArray.COLOR_3) == GeometryArray.COLOR_3;
	if (color3 || color4) {
	    System.out.print(" colors") ;
	    if (color4)
		System.out.print(" alpha") ;
	}
	System.out.println() ;
    }
}

