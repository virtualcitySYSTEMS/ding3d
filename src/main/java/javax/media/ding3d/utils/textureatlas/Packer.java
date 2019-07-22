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

import java.util.LinkedList;

import javax.media.ding3d.Texture;



public class Packer {
	private final LinkedList<AtlasRegion> regions;
	private int binWidth;
	private int binHeight;
	private final boolean fourChannel;

	public Packer(int binWidth, int binHeight, boolean fourChannel) {
		regions = new LinkedList<AtlasRegion>();

		this.fourChannel = fourChannel;
		this.binWidth = binWidth;
		this.binHeight = binHeight;

	}


	public void setBinSize(int width, int height) {
		this.binWidth = width;
		this.binHeight = height;
	}

	public boolean addRegion(SimpleTexture uri, int width, int  height, float imageScale) {
		return regions.add(new AtlasRegion(uri, 0, 0, width, height, imageScale));
	}

	public boolean addRegion(AtlasRegion region) {
		return regions.add(region);
	}

	public boolean removeRegion(SimpleTexture uri) {
		return regions.remove(new AtlasRegion(uri, 0, 0, 0, 0, 0f));
	}

	public int getRegions() {
		return regions != null ? regions.size() : 0;
	}

	public TextureAtlas pack() {
		TextureAtlas atlas = null;
		LightmapAlgorithm packingAlgorithm = new LightmapAlgorithm(binWidth, binHeight, true);

		atlas = packingAlgorithm.createTextureAtlas(regions);
		atlas.setFourChannels(fourChannel);
		
		return atlas;
	}

}
