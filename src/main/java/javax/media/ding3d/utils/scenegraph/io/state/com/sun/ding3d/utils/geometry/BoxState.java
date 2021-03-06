/*
 * $RCSfile: BoxState.java,v $
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
 * $Date: 2007/02/09 17:20:30 $
 * $State: Exp $
 */

package javax.media.ding3d.utils.scenegraph.io.state.com.sun.ding3d.utils.geometry;

import java.io.*;
import javax.media.ding3d.utils.geometry.Box;
import javax.media.ding3d.Appearance;
import javax.media.ding3d.SceneGraphObject;
import javax.media.ding3d.vecmath.Color3f;
import javax.media.ding3d.utils.scenegraph.io.retained.Controller;
import javax.media.ding3d.utils.scenegraph.io.retained.SymbolTableData;

public class BoxState extends PrimitiveState {

    private float xdim;
    private float ydim;
    private float zdim;
    private int frontAppearance;
    private int backAppearance;
    private int topAppearance;
    private int bottomAppearance;
    private int leftAppearance;
    private int rightAppearance;

    public BoxState( SymbolTableData symbol, Controller control ) {
	super( symbol, control );

        if (node!=null) {
            frontAppearance = control.getSymbolTable().addReference( ((Box)node).getShape( Box.FRONT ).getAppearance() );
            backAppearance = control.getSymbolTable().addReference( ((Box)node).getShape( Box.BACK ).getAppearance() );
            topAppearance = control.getSymbolTable().addReference( ((Box)node).getShape( Box.TOP ).getAppearance() );
            bottomAppearance = control.getSymbolTable().addReference( ((Box)node).getShape( Box.BOTTOM ).getAppearance() );
            leftAppearance = control.getSymbolTable().addReference( ((Box)node).getShape( Box.LEFT ).getAppearance() );
            rightAppearance = control.getSymbolTable().addReference( ((Box)node).getShape( Box.RIGHT ).getAppearance() );
        }
    }

    public void writeObject( DataOutput out ) throws IOException {
	super.writeObject( out );

        out.writeInt( frontAppearance );
        out.writeInt( backAppearance );
        out.writeInt( topAppearance );
        out.writeInt( bottomAppearance );
        out.writeInt( leftAppearance );
        out.writeInt( rightAppearance );
    }

    public void readObject( DataInput in ) throws IOException {
       super.readObject(in);

       frontAppearance = in.readInt();
       backAppearance = in.readInt();
       topAppearance = in.readInt();
       bottomAppearance = in.readInt();
       leftAppearance = in.readInt();
       rightAppearance = in.readInt();
    }

    public void writeConstructorParams( DataOutput out ) throws IOException {
	super.writeConstructorParams( out );

        out.writeFloat( ((Box)node).getXdimension() );
        out.writeFloat( ((Box)node).getYdimension() );
        out.writeFloat( ((Box)node).getZdimension() );
    }

    public void readConstructorParams( DataInput in ) throws IOException {
       super.readConstructorParams(in);

       xdim = in.readFloat();
       ydim = in.readFloat();
       zdim = in.readFloat();
    }

    public void buildGraph() {
        if (frontAppearance == backAppearance &&
            frontAppearance == topAppearance &&
            frontAppearance == bottomAppearance &&
            frontAppearance == leftAppearance &&
            frontAppearance == rightAppearance ) {
            ((Box)node).setAppearance( (Appearance)control.getSymbolTable().getDing3dNode( frontAppearance ));
        } else {
            ((Box)node).setAppearance( Box.FRONT, (Appearance)control.getSymbolTable().getDing3dNode( frontAppearance ));
            ((Box)node).setAppearance( Box.BACK, (Appearance)control.getSymbolTable().getDing3dNode( backAppearance ));
            ((Box)node).setAppearance( Box.TOP, (Appearance)control.getSymbolTable().getDing3dNode( topAppearance ));
            ((Box)node).setAppearance( Box.BOTTOM, (Appearance)control.getSymbolTable().getDing3dNode( bottomAppearance ));
            ((Box)node).setAppearance( Box.LEFT, (Appearance)control.getSymbolTable().getDing3dNode( leftAppearance ));
            ((Box)node).setAppearance( Box.RIGHT, (Appearance)control.getSymbolTable().getDing3dNode( rightAppearance ));
        }

        super.buildGraph(); // This must be the last call in the method
    }

    public SceneGraphObject createNode( Class Ding3dClass ) {
        Box box = (Box)super.createNode( Ding3dClass, new Class[] { Float.TYPE,
                                                   Float.TYPE,
                                                   Float.TYPE,
                                                   Integer.TYPE,
                                                   Appearance.class },
                                     new Object[] { new Float( xdim ),
                                                    new Float( ydim ),
                                                    new Float( zdim ),
                                                    new Integer( primflags ),
                                                    null } );
        return box;
    }

    protected javax.media.ding3d.SceneGraphObject createNode() {
        return new Box( xdim, ydim, zdim, primflags, null );
    }

}
