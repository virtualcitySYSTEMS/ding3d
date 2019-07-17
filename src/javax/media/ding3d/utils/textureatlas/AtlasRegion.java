/*******************************************************************************
 * This file is part of the Texture Atlas Generation Tool.
 * Copyright (c) 2010 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The Texture Atlas Generation Tool is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * @author Babak Naderi <b.naderi@mailbox.tu-berlin.de>
 ******************************************************************************/
package javax.media.ding3d.utils.textureatlas;

import javax.media.ding3d.Texture;

/**
 * For packing algorithms each texture will be represented as an object of this class.
 * As a result of packing process these variables should be set: x,y, level, and rotated.
 * Because of optimization SET/GET methods are neglected and variables defined in public domain.
 *
 */
public class AtlasRegion {
	// must be equal to the name of the texture image
	public SimpleTexture texImageName;
	public int width;
	public int height;
	public int area; 
	public int x;
	public int y;
	public boolean isRotated;
	public int score = Integer.MIN_VALUE;
	public int file;
	public float imageScale = 1f;
	
	
	private AtlasRegion org;

	private AtlasRegion() {
	}
	
	@Override
	public String toString(){
		return "AtlasRegion ("+texImageName+" "+x+" "+y+" "+width+" "+height+" "+imageScale+" "+area+" "+score+" "+file+" "+isRotated+")";
	}

	public boolean equals(AtlasRegion other){
		if(this.texImageName != other.texImageName) return false;
		if(this.width != other.width) return false;
		if(this.height != other.height) return false;
		if(this.area != other.area) return false; 
		if(this.x != other.x) return false;
		if(this.y != other.y) return false;
		if(this.isRotated != other.isRotated) return false;
		if(this.score != other.score) return false;
		if(this.file != other.file) return false;
		if(this.imageScale != other.imageScale) return false;
		
		return true;
	}
	
	public AtlasRegion(SimpleTexture texImageName, int x, int y, int width, int height, float imageScale) {
		this.texImageName = texImageName;
		this.width = width;
		this.height = height;
		this.area = width * height;
		this.imageScale = imageScale;
		this.x = x;
		this.y = y;
		
		this.org = this.clone();
	}
	
	public void reset(){
		this.texImageName = org.texImageName;
		this.width = org.width;
		this.height = org.height;
		this.area = org.area; 
		this.x = org.x;
		this.y = org.y;
		this.isRotated = org.isRotated;
		this.score = org.score;
		this.file = org.file;
		this.imageScale = org.imageScale;
		
	}
	
	
	
	public AtlasRegion clone(){
		AtlasRegion copy = new AtlasRegion();
		copy.texImageName = this.texImageName;
		copy.width = this.width;
		copy.height = this.height;
		copy.area = this.area; 
		copy.x = this.x;
		copy.y = this.y;
		copy.isRotated = this.isRotated;
		copy.score = this.score;
		copy.file = this.file;
		copy.imageScale = this.imageScale;
		
		copy.org = this.org;
		
		return copy;
	}
	
	public boolean isSameAsOrg(){
		if(org.texImageName != this.texImageName) return false;
		if(org.width != this.width) return false;
		if(org.height != this.height) return false;
		if(org.area != this.area) return false; 
		if(org.x != this.x) return false;
		if(org.y != this.y) return false;
		if(org.isRotated != this.isRotated) return false;
		if(org.score != this.score) return false;
		if(org.file != this.file) return false;
		if(org.imageScale != this.imageScale) return false;
		
		return true;
	}
	

	public void rotate() {
		int tmp = width;
		width = height;
		height = tmp;
		isRotated = !isRotated;
	}

	public void setPosition(int x, int y, short file) {
		this.x = x;
		this.y = y;
		this.file = file;
	}

	public SimpleTexture getTexImageName(){
		return texImageName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AtlasRegion) {
			if (((AtlasRegion)obj).getTexImageName() != null && ((AtlasRegion)obj).getTexImageName() == this.texImageName)
				return true;
		}
		
		return false;
	}

	public void clear() {
		texImageName = null;
	}

}
