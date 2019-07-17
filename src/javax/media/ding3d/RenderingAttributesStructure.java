/*
 * $RCSfile: RenderingAttributesStructure.java,v $
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * $Revision: 1.7 $
 * $Date: 2008/02/28 20:17:29 $
 * $State: Exp $
 */

package javax.media.ding3d;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A rendering attributes structure is an object that handles
 * NodeComponent object updates.
 */

class RenderingAttributesStructure extends Ding3dStructure implements ObjectUpdate {
    // List of textures whose resourceCreation mask should be updated
    ArrayList objList = new ArrayList();

    RenderingAttributesStructure() {
	super(null, Ding3dThread.UPDATE_RENDERING_ATTRIBUTES);
    }

    void processMessages(long referenceTime) {
	Ding3dMessage m;
	boolean addMirrorObj = false;

	Ding3dMessage messages[] = getMessages(referenceTime);
	int nMsg = getNumMessage();

	if (nMsg <= 0) {
	    return;
	}

	for (int i=0; i < nMsg; i++) {
	    m = messages[i];
	    switch (m.type) {
		// Appearance is always updated immediately, since rBin needs
		// the most up-to-date values for restructuring
	    case Ding3dMessage.APPEARANCE_CHANGED:
	    case Ding3dMessage.SHADER_APPEARANCE_CHANGED:
	    case Ding3dMessage.TEXTURE_UNIT_STATE_CHANGED:
		{
		    // System.err.println("1 RAS : Ding3dMessage type : " + m.type);
		    int component = ((Integer)m.args[1]).intValue();
		    NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
		    nc.mirror.changedFrequent = ((Integer)m.args[3]).intValue();
		    updateNodeComponent((Object[])m.args);
		    if (nc.mirror.changedFrequent != 0) {
			objList.add(m);
			addMirrorObj = true;
			nc.mirror.compChanged |= component;
		    }
		    else {
			m.decRefcount();
		    }
		}
		break;
	    case Ding3dMessage.COLORINGATTRIBUTES_CHANGED:
	    case Ding3dMessage.LINEATTRIBUTES_CHANGED:
	    case Ding3dMessage.POINTATTRIBUTES_CHANGED:
	    case Ding3dMessage.POLYGONATTRIBUTES_CHANGED:
	    case Ding3dMessage.RENDERINGATTRIBUTES_CHANGED:
	    case Ding3dMessage.TRANSPARENCYATTRIBUTES_CHANGED:
	    case Ding3dMessage.MATERIAL_CHANGED:
	    case Ding3dMessage.TEXCOORDGENERATION_CHANGED:
	    case Ding3dMessage.SHADER_ATTRIBUTE_CHANGED:
	    case Ding3dMessage.SHADER_ATTRIBUTE_SET_CHANGED:
		{
		    // System.err.println("2 RAS : Ding3dMessage type : " + m.type);

		    NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
		    nc.mirror.changedFrequent = ((Integer)m.args[3]).intValue();
		    if (nc.mirror.changedFrequent != 0) {
			objList.add(m);
			addMirrorObj = true;
			nc.mirror.compChanged = 1;
		    }
		    else {
			updateNodeComponent((Object[])m.args);
			m.decRefcount();
		    }
		}
		break;
	    case Ding3dMessage.IMAGE_COMPONENT_CHANGED:
		{
		    NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
		    int changes =  ((Integer)m.args[3]).intValue();
		    if(nc.mirror != null)
			nc.mirror.changedFrequent = changes;
		    if (changes != 0) {
			objList.add(m);
			addMirrorObj = true;
		    }
		    else {
			updateNodeComponent((Object[])m.args);
			m.decRefcount();
		    }
		}
		break;
	    case Ding3dMessage.TEXTUREATTRIBUTES_CHANGED:
		{

		    NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
		    nc.mirror.changedFrequent = ((Integer)m.args[4]).intValue();

		    if (nc.mirror.changedFrequent != 0) {
			objList.add(m);
			addMirrorObj = true;
			nc.mirror.compChanged = 1;
		    }
		    else {
			updateTextureAttributes((Object[])m.args);
			m.decRefcount();
		    }
		}
		break;
	    case Ding3dMessage.TEXTURE_CHANGED:
		{
		    NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
		    nc.mirror.changedFrequent = ((Integer)m.args[3]).intValue();

		    objList.add(m);
		    nc.mirror.compChanged = 1;
		    addMirrorObj = true;
		}
		break;
	    case Ding3dMessage.GEOMETRY_CHANGED:
		GeometryRetained geo = (GeometryRetained) m.args[1];
		int val;
		if (geo instanceof IndexedGeometryArrayRetained) {
		    objList.add(m);
		    addMirrorObj = true;
		    if (m.args[2] == null) {
			val = ((Integer)m.args[3]).intValue();
			geo.cachedChangedFrequent = val;
		    }
		}
		else {
		    val = ((Integer)m.args[3]).intValue();
		    geo.cachedChangedFrequent = val;
		    m.decRefcount();
		}
		break;
	    case Ding3dMessage.MORPH_CHANGED:
		objList.add(m);
		addMirrorObj = true;
		break;
	    default:
		m.decRefcount();
	    }
	}
	if (addMirrorObj) {
	    VirtualUniverse.mc.addMirrorObject(this);
	}

	// clear array to prevent memory leaks
	Arrays.fill(messages, 0, nMsg, null);
    }

    public void updateObject() {

	int size = objList.size();
	for (int i = 0; i < size; i++) {
	    Ding3dMessage m = (Ding3dMessage)objList.get(i);
	    // Message Only sent to RenderingAttributesStructure
	    // when the geometry type is indexed
	    if (m.type == Ding3dMessage.GEOMETRY_CHANGED) {
		GeometryArrayRetained geo = (GeometryArrayRetained)m.args[1];
		if (m.args[2] == null) {
		    geo.updateMirrorGeometry();
		}
		else {
		    geo.initMirrorGeometry();
		}
	    }
	    else if (m.type == Ding3dMessage.MORPH_CHANGED) {
		MorphRetained morph = (MorphRetained) m.args[0];
		GeometryArrayRetained geo = (GeometryArrayRetained)morph.morphedGeometryArray.retained;
		geo.updateMirrorGeometry();
	    }
	    else if (m.type == Ding3dMessage.TEXTUREATTRIBUTES_CHANGED) {
		NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
		nc.mirror.compChanged = 0;
		updateTextureAttributes((Object[])m.args);
	    }
	    else if (m.type == Ding3dMessage.APPEARANCE_CHANGED ||
		     m.type == Ding3dMessage.SHADER_APPEARANCE_CHANGED ||
		     m.type == Ding3dMessage.TEXTURE_UNIT_STATE_CHANGED){
		NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
		nc.mirror.compChanged = 0;
	    }
	    else {
		NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
		if (nc.mirror != null)
		    nc.mirror.compChanged = 0;
		updateNodeComponent(m.args);
	    }
	    m.decRefcount();
	}
	objList.clear();
    }


    private void updateNodeComponent(Object[] args) {
	// System.err.println("RAS : updateNodeComponent : " + this);
	NodeComponentRetained n = (NodeComponentRetained)args[0];
	n.updateMirrorObject(((Integer)args[1]).intValue(), args[2]);
    }

    private void updateTextureAttributes(Object[] args) {
      TextureAttributesRetained n = (TextureAttributesRetained)args[0];
      n.updateMirrorObject(((Integer)args[1]).intValue(), args[2], args[3]);
    }

    void removeNodes(Ding3dMessage m) {}

    void cleanup() {}


}


