package javax.media.ding3d.utils.textureatlas;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.ding3d.Texture;



public class LightmapAlgorithm{
	private int atlasWidth;
	private int atlasHeight;
	private boolean useRotation;
	
    private static final int DEBUG = 0;


	public LightmapAlgorithm(int atlasWidth, int atlasHeight, boolean useRotation) {
		this.atlasWidth = atlasWidth;
		this.atlasHeight = atlasHeight;
		this.useRotation = useRotation;

	}


	public TextureAtlas createTextureAtlas(LinkedList<AtlasRegion> regions) {

		if(DEBUG == 1) {
			System.out.println("LightmapAlgorithm creating TextureAtlas from "+regions.size()+" regions");
		}
		TextureAtlas atlas = new TextureAtlas();
		atlas.setBindingBox(atlasWidth, atlasHeight);

		// sort regions
		Collections.sort(regions, new DescendingDiagonalComparator());

		List<Node> roots = new ArrayList<Node>();
		Node root0 = new Node(0, 0, atlasWidth, atlasHeight);
		roots.add(root0);
		Map<Node, List<AtlasRegion>> fileRegionsMap = new HashMap<Node, List<AtlasRegion>>(); 
		
		Map<SimpleTexture, AtlasRegion> cloneMap = new HashMap<SimpleTexture, AtlasRegion>();
		for (AtlasRegion region : regions) {
			AtlasRegion regionClone = region.clone();
			cloneMap.put(region.texImageName, regionClone);
		}
		

		for (AtlasRegion region : regions) {
			boolean added = false;
			AtlasRegion regionClone = region.clone();

			for (Node root : roots) {
				if (root.insert(region) != null) {
					added = true;

					List<AtlasRegion> fileRegions = fileRegionsMap.get(root);
					if(fileRegions == null){
						fileRegions = new ArrayList<AtlasRegion>();
						fileRegionsMap.put(root, fileRegions);
					}
					fileRegions.add(regionClone);

					break;
				}
			}

			if (!added) {
				Node root = new Node(0, 0, atlasWidth, atlasHeight);
				root.insert(region);
				List<AtlasRegion> fileRegions = fileRegionsMap.get(root);
				if(fileRegions == null){
					fileRegions = new ArrayList<AtlasRegion>();
					fileRegionsMap.put(root, fileRegions);
				}
				fileRegions.add(regionClone);


				roots.add(root);
			}
		}

		Dimension lastDimension = new Dimension(atlasWidth, atlasHeight);
		if(true){
			Node lastRoot = roots.get(roots.size()-1);
			List<AtlasRegion> lastFileRegions = fileRegionsMap.get(lastRoot);
			boolean stillFits = true;
			float[] reduce = new float[2];
			reduce[0] = 1.0f;
			reduce[1] = 0.5f;


			Node newLastRoot = lastRoot;
			int r=0;
			while(stillFits && reduce[0] > 1f/64f && reduce[1] > 1f/64f){
				Node newRoot = new Node(0, 0, (int)(atlasWidth*reduce[0]), (int)(atlasHeight*reduce[1]));
				
				//if(reduce[0]== 0.25 && reduce[1]==0.125) break;
				//System.out.println("newRoot  "+Integer.toHexString(System.identityHashCode(newRoot))+" "+reduce[0]+", "+reduce[1]);
				
				for (AtlasRegion region : lastFileRegions) {
//					if(!region.isSameAsOrg()){
//						System.out.println("!region.isSameAsOrg()");
//					}
					//region.reset();
					AtlasRegion regionClone = cloneMap.get(region.texImageName).clone();
										
//					if(!regionClone.equals(region)){
//						System.out.println("clone not same as "+regionClone+" of "+region);
//					}
					
					if (newRoot.insert(regionClone) == null) {
						stillFits = false;
						break;
					}
				}		
				if(stillFits){
					newLastRoot = newRoot;
					lastDimension.width = (int)(atlasWidth*reduce[0]);
					lastDimension.height = (int)(atlasHeight*reduce[1]);
				}
				reduce[(r++)%2] *= 0.5f;
				
				
			//	System.out.println("\n\nnewRoot "+Integer.toHexString(System.identityHashCode(newRoot))+" stillFits "+stillFits+" \n"+newRoot.debug());
				
			}

			if(lastRoot != newLastRoot){
				roots.remove(lastRoot);
				roots.add(newLastRoot);
				
				//System.out.println("replacing "+Integer.toHexString(System.identityHashCode(lastRoot))+" with "+Integer.toHexString(System.identityHashCode(newLastRoot)));
			//	System.out.println("\n\n\nnewLastRoot \n"+newLastRoot.debug());
			}
		}





		Map<Integer, Dimension> fileDimensions = atlas.getFileDimensions();

		if(DEBUG == 1) {
			System.out.println("LightmapAlgorithm filling TextureAtlas from "+roots.size()+" root nodes");
		}
		// fill texture atlas from root nodes
		for (int i = 0; i < roots.size(); i++){
			fillAtlas(atlas, roots.get(i), i);
			fileDimensions.put(i, new Dimension(atlasWidth, atlasHeight));
		}

		fileDimensions.put(roots.size()-1, lastDimension);


		return atlas;
	}


	private void fillAtlas(TextureAtlas atlas, Node node, int file) {
		if (node == null)
			return;

		if (node.region.texImageName != null) {
			node.region.file = file;
			atlas.addRegion(node.region);
		}

		fillAtlas(atlas, node.childs[0], file);
		fillAtlas(atlas, node.childs[1], file);
	}

	private class Node {
		private Node[] childs;
		private AtlasRegion region;
		private short file;

		private Node(int x, int y, int width, int height) {
			childs = new Node[2];
			region = new AtlasRegion(null, x, y, width, height, 1f);
		}

		private boolean isLeaf() {
			return childs[0] == null && childs[1] == null;
		}
		
		public String debug(){
			StringBuffer sb = new StringBuffer();
			ArrayList<AtlasRegion> debugRegions = new ArrayList<AtlasRegion>();
			this.collectRegions(debugRegions);
			for(AtlasRegion debugRegion : debugRegions){
				sb.append( Integer.toHexString(System.identityHashCode(debugRegion))+" "+debugRegion.toString()+"\n");
			}
			return sb.toString();
		}
		
		public void collectRegions(ArrayList<AtlasRegion> regions){
			if(isLeaf()){
				regions.add(region);
			}
			else{
				childs[0].collectRegions(regions);
				childs[1].collectRegions(regions);
			}
		}

		private Node insert(AtlasRegion candidate) {
			if (!isLeaf()) {
				Node node = childs[0].insert(candidate);
				if (node != null)
					return node;

				return childs[1].insert(candidate);
			}

			else {
				if (region.texImageName != null)
					return null;

				if (!fitsInRegion(candidate))
					return null;

				if (candidate.width == region.width && candidate.height == region.height) {
					candidate.x = region.x;
					candidate.y = region.y;
					candidate.file = file;
					region = candidate;
					return this;
				}

				int dw = region.width - candidate.width;
				int dh = region.height - candidate.height;

				if (dw > dh) {
					childs[0] = new Node(region.x, region.y, candidate.width, region.height);
					childs[1] = new Node(region.x + candidate.width, region.y, region.width - candidate.width, region.height);
				} else {
					childs[0] = new Node(region.x, region.y, region.width, candidate.height);
					childs[1] = new Node(region.x, region.y + candidate.height, region.width, region.height - candidate.height);
				}

				return childs[0].insert(candidate);
			}
		}

		private boolean fitsInRegion(AtlasRegion candidate) {
			boolean fits = candidate.width <= region.width && candidate.height <= region.height;
			if (!fits && useRotation) {
				fits = candidate.height <= region.width && candidate.width <= region.height;
				if (fits)
					candidate.rotate();
			}

			return fits;
		}

	}

}
