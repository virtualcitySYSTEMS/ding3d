/*
 * $RCSfile: PositionInterpolatorState.java,v $
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
 * $Date: 2007/02/09 17:20:38 $
 * $State: Exp $
 */

package javax.media.ding3d.utils.scenegraph.io.state.javax.media.ding3d;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import javax.media.ding3d.PositionInterpolator;
import javax.media.ding3d.SceneGraphObject;
import javax.media.ding3d.Transform3D;
import javax.media.ding3d.TransformGroup;
import javax.media.ding3d.vecmath.Point3f;
import javax.media.ding3d.utils.scenegraph.io.retained.Controller;
import javax.media.ding3d.utils.scenegraph.io.retained.SymbolTableData;

public class PositionInterpolatorState extends TransformInterpolatorState {

    public PositionInterpolatorState(SymbolTableData symbol,Controller control) {
        super( symbol, control );
    }

    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );

	out.writeFloat( ((PositionInterpolator)node).getStartPosition() );
	out.writeFloat( ((PositionInterpolator)node).getEndPosition() );
    }

    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );

	((PositionInterpolator)node).setStartPosition( in.readFloat() );
	((PositionInterpolator)node).setEndPosition( in.readFloat() );
    }

    public SceneGraphObject createNode( Class Ding3dClass ) {
        return createNode( Ding3dClass, new Class[] { javax.media.ding3d.Alpha.class,
                                                    TransformGroup.class },
                                      new Object[] { null,
                                                     null } );
    }

    protected javax.media.ding3d.SceneGraphObject createNode() {
        return new PositionInterpolator( null, null );
    }
}
