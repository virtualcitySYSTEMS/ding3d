/*
 * $RCSfile: SoundscapeState.java,v $
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
 * $Date: 2007/02/09 17:20:39 $
 * $State: Exp $
 */

package javax.media.ding3d.utils.scenegraph.io.state.javax.media.ding3d;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import javax.media.ding3d.Soundscape;
import javax.media.ding3d.SceneGraphObject;
import javax.media.ding3d.BoundingLeaf;
import javax.media.ding3d.AuralAttributes;
import javax.media.ding3d.utils.scenegraph.io.retained.Controller;
import javax.media.ding3d.utils.scenegraph.io.retained.SymbolTableData;

public class SoundscapeState extends LeafState {

    private int boundingLeaf;
    private int auralAttributes;

    public SoundscapeState(SymbolTableData symbol,Controller control) {
        super( symbol, control );

    }

    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );

        out.writeInt( control.getSymbolTable().addReference( ((Soundscape)node).getApplicationBoundingLeaf() ));
        control.writeBounds( out, ((Soundscape)node).getApplicationBounds() );
        out.writeInt( control.getSymbolTable().addReference( ((Soundscape)node).getAuralAttributes() ));
    }

    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );

        boundingLeaf = in.readInt();
        ((Soundscape)node).setApplicationBounds( control.readBounds(in));
        auralAttributes = in.readInt();
    }

    /**
     * Called when this component reference count is incremented.
     * Allows this component to update the reference count of any components
     * that it references.
     */
    public void addSubReference() {
        control.getSymbolTable().incNodeComponentRefCount( auralAttributes );
    }

    public void buildGraph() {
        ((Soundscape)node).setApplicationBoundingLeaf( (BoundingLeaf)control.getSymbolTable().getDing3dNode( boundingLeaf ));
        ((Soundscape)node).setAuralAttributes( (AuralAttributes)control.getSymbolTable().getDing3dNode( auralAttributes ));
        super.buildGraph();     // Must be last call in method
    }

    protected javax.media.ding3d.SceneGraphObject createNode() {
        return new Soundscape();
    }


}
