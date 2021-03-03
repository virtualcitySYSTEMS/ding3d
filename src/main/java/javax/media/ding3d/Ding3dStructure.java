/*
 * $RCSfile: Ding3dStructure.java,v $
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
 * $Revision: 1.5 $
 * $Date: 2008/02/28 20:17:25 $
 * $State: Exp $
 */

package javax.media.ding3d;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Ding3dStructure is the super class of all structures in Java 3D.
 * A structure is a object that organizes a collection of objects.
 */

abstract class Ding3dStructure extends Object {
    /**
     * This is the list of messages to be processed by this structure
     */
    UnorderList messageList = new UnorderList(5, Ding3dMessage.class);

    /**
     * This is the update Thread for this structure
     */

    StructureUpdateThread updateThread = null;

    /**
     * This is the type of update thread
     */
    int threadType = -1;

    /**
     * The universe of this structure
     */
    VirtualUniverse universe = null;

    /**
     * The thread data for the update thread
     */
    Ding3dThreadData threadData = new Ding3dThreadData();

    /**
     * number of messages for this snapshot of time
     */
    int nMessage = 0;
    Ding3dMessage[] msgList = new Ding3dMessage[5];

    /**
     * This constructor does nothing
     */
    Ding3dStructure(VirtualUniverse u, int type) {
	universe = u;
	threadType = type;
	threadData.threadType = type;
    }

    /**
     * This returns the thread data for this thread.
     */
    final Ding3dThreadData getUpdateThreadData() {
	return (threadData);
    }

    /**
     * This adds a message to the list of messages for this structure
     */
    final void addMessage(Ding3dMessage message) {

	if (threadData != null) {
	    threadData.lastUpdateTime = message.time;
	} else {
	    // this force message to consume when initialized
	    message.time = -1;
	}
	message.incRefcount();
	messageList.add(message);
    }


    /**
     * This returns whether or not there are any pending messages
     */
    final Ding3dMessage[] getMessages(long referenceTime) {
	int sz, n = 0;

	synchronized (messageList) {
	    if ((sz = messageList.size()) > 0) {
		Ding3dMessage mess[] = (Ding3dMessage []) messageList.toArray(false);
		for (n = 0; n < sz; n++) {
		    if (mess[n].time > referenceTime) {
			break;
		    }
		}
		if (n > 0) {
		    if (msgList.length < n) {
			msgList = new Ding3dMessage[n];
		    }
		    messageList.shift(msgList, n);
		}
	    }
	}

	nMessage = n;
	return msgList;
    }

    final void clearMessages() {
	synchronized (messageList) {
            int nmessage = messageList.size();
	    if (nmessage > 0) {
		Ding3dMessage mess[] = (Ding3dMessage []) messageList.toArray(false);
		for (int i = nmessage-1; i >=0; i--) {
		    mess[i].decRefcount();
		}
		messageList.clear();
	    }
	    nMessage = 0;
	    msgList = new Ding3dMessage[5];
	}

    }

    int  getNumMessage() {
	return nMessage;
    }

    /**
     * This gets overriden by the structure
     */
    abstract void processMessages(long referenceTime);

    /**
     * This is used by MasterControl to process any unused message
     * for final cleanup. DON'T decrememt message count in
     * the method, as it is done by MasterControl.
     */
    abstract void removeNodes(Ding3dMessage m);

    /**
     * Release resource associate with this structure before GC
     * We need to clear all those IndexedUnorderSet/WakeupIndexedList
     * so that the listIdx associate with IndexedObject reset to -1.
     */
    abstract void cleanup();
}
