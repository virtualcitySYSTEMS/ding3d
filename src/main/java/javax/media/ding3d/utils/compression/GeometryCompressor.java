/*
 * $RCSfile: GeometryCompressor.java,v $
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
 * $Revision: 1.5 $
 * $Date: 2007/02/09 17:20:16 $
 * $State: Exp $
 */

package javax.media.ding3d.utils.compression;

import java.io.IOException;
import javax.media.ding3d.CompressedGeometry;
import javax.media.ding3d.CompressedGeometryHeader;
import javax.media.ding3d.vecmath.Point3d;

/**
 * A GeometryCompressor takes a stream of geometric elements and
 * quantization parameters (the CompressionStream object) and
 * compresses it into a stream of commands as defined by appendix B
 * of the Java 3D specification.  The resulting data may be output
 * in the form of a CompressedGeometry node component or appended
 * to a CompressedGeometryFile.
 *
 * @see CompressionStream
 * @see CompressedGeometry
 * @see CompressedGeometryFile
 *
 * @deprecated As of Java 3D 1.5, replaced by
 * javax.media.ding3d.utils.geometry.compression.{@link javax.media.ding3d.utils.geometry.compression.GeometryCompressor}.
 */
public class GeometryCompressor {
    private static final boolean benchmark = false ;
    private static final boolean printStream = false ;
    private static final boolean printHuffman = false ;

    private HuffmanTable huffmanTable ;
    private CommandStream outputBuffer ;
    private CompressedGeometryHeader cgHeader ;
    private long startTime ;

    public GeometryCompressor() {
	// Create a compressed geometry header.
	cgHeader = new CompressedGeometryHeader() ;

	// v1.0.0 - pre-FCS
	// v1.0.1 - fixed winding order, FCS version (Ding3d 1.1.2)
	// v1.0.2 - normal component length maximum 6, LII hardware (Ding3d 1.2)
	cgHeader.majorVersionNumber = 1 ;
	cgHeader.minorVersionNumber = 0 ;
	cgHeader.minorMinorVersionNumber = 2 ;
    }

    /**
     * Compress a stream into a CompressedGeometry node component.
     *
     * @param stream CompressionStream containing the geometry to be compressed
     * @return a CompressedGeometry node component
     */
    public CompressedGeometry compress(CompressionStream stream) {
	CompressedGeometry cg ;

	compressStream(stream) ;
	cg = new CompressedGeometry(cgHeader, outputBuffer.getBytes()) ;

	outputBuffer.clear() ;
	return cg ;
    }

    /**
     * Compress a stream and append the output to a CompressedGeometryFile.
     * The resource remains open for subsequent updates; its close() method
     * must be called to create a valid compressed geometry resource file.
     *
     * @param stream CompressionStream containing the geometry to be compressed
     * @param f a currently open CompressedGeometryFile with write access
     * @exception IOException if write fails
     */
    public void compress(CompressionStream stream, CompressedGeometryFile f)
	throws IOException {

	compressStream(stream) ;
	f.write(cgHeader, outputBuffer.getBytes()) ;

	outputBuffer.clear() ;
    }

    //
    // Compress the stream and put the results in the output buffer.
    // Set up the CompressedGeometryHeader object.
    //
    private void compressStream(CompressionStream stream) {
	if (benchmark) startTime = System.currentTimeMillis() ;

	// Create the Huffman table.
	huffmanTable = new HuffmanTable() ;

	// Quantize the stream, compute deltas between consecutive elements if
	// possible, and histogram the data length distribution.
	stream.quantize(huffmanTable) ;

	// Compute tags for stream tokens.
	huffmanTable.computeTags() ;

	// Create the output buffer and assemble the compressed output.
	outputBuffer = new CommandStream(stream.getByteCount() / 3) ;
	stream.outputCommands(huffmanTable, outputBuffer) ;

	// Print any desired info.
	if (benchmark) printBench(stream) ;
	if (printStream) stream.print() ;
	if (printHuffman) huffmanTable.print() ;

	// Set up the compressed geometry header object.
	cgHeader.bufferType = stream.streamType ;
	cgHeader.bufferDataPresent = 0 ;
	cgHeader.lowerBound = new Point3d(stream.ncBounds[0]) ;
	cgHeader.upperBound = new Point3d(stream.ncBounds[1]) ;

	if (stream.vertexNormals)
	    cgHeader.bufferDataPresent |=
		CompressedGeometryHeader.NORMAL_IN_BUFFER ;

	if (stream.vertexColor3 || stream.vertexColor4)
	    cgHeader.bufferDataPresent |=
		CompressedGeometryHeader.COLOR_IN_BUFFER ;

	if (stream.vertexColor4)
	    cgHeader.bufferDataPresent |=
		CompressedGeometryHeader.ALPHA_IN_BUFFER ;

	cgHeader.start = 0 ;
	cgHeader.size = outputBuffer.getByteCount() ;

	// Clear the huffman table for next use.
	huffmanTable.clear() ;
    }

    private void printBench(CompressionStream stream) {
	long t = System.currentTimeMillis() - startTime ;
	int vertexCount = stream.getVertexCount() ;
	int meshReferenceCount = stream.getMeshReferenceCount() ;
	int totalVertices = meshReferenceCount + vertexCount ;
	float meshPercent = 100f * meshReferenceCount/(float)totalVertices ;

	float compressionRatio =
	    stream.getByteCount() / ((float)outputBuffer.getByteCount()) ;

	int vertexBytes =
	    12 + (stream.vertexColor3 ? 12 : 0) +
	    (stream.vertexColor4 ? 16 : 0) + (stream.vertexNormals ? 12 : 0) ;

	float compressedVertexBytes =
	    outputBuffer.getByteCount() / (float)totalVertices ;

	System.out.println
	    ("\nGeometryCompressor:\n" + totalVertices + " total vertices\n" +
	     vertexCount + " streamed vertices\n" + meshReferenceCount +
	     " mesh buffer references (" + meshPercent + "%)\n" +
	     stream.getByteCount() + " bytes streamed geometry compressed to " +
	     outputBuffer.getByteCount() + " in " + (t/1000f) + " sec\n" +
	     (stream.getByteCount()/(float)t) + " kbytes/sec, " +
	     "stream compression ratio " + compressionRatio + "\n\n" +
	     vertexBytes + " original bytes per vertex, " +
	     compressedVertexBytes + " compressed bytes per vertex\n" +
	     "total vertex compression ratio " +
	     (vertexBytes / (float)compressedVertexBytes) + "\n\n" +
	     "lower bound " + stream.ncBounds[0].toString() +"\n" +
	     "upper bound " + stream.ncBounds[1].toString()) ;
    }
}
