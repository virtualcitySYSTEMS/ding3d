package javax.media.ding3d.utils.textureatlas;

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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.media.ding3d.Appearance;
import javax.media.ding3d.Geometry;
import javax.media.ding3d.GeometryArray;
import javax.media.ding3d.Group;
import javax.media.ding3d.ImageComponent2D;
import javax.media.ding3d.Material;
import javax.media.ding3d.Node;
import javax.media.ding3d.Shape3D;
import javax.media.ding3d.Texture;
import javax.media.ding3d.Texture2D;
import javax.media.ding3d.Transform3D;
import javax.media.ding3d.TriangleArray;
import javax.media.ding3d.vecmath.Point2f;
import javax.media.ding3d.vecmath.Point3f;
import javax.media.ding3d.vecmath.Vector2f;
import javax.media.ding3d.vecmath.Vector3f;


import javax.media.ding3d.utils.textureatlas.AppearanceInfo;
import javax.media.ding3d.utils.textureatlas.TextureAtlasMergeInfo;
import javax.media.ding3d.utils.textureatlas.LoaderOptions;
import javax.media.ding3d.utils.textureatlas.SimpleTexture;
import javax.media.ding3d.utils.textureatlas.TextureInfo;

public class TextureAtlasGenerator {

	private LoaderOptions loaderOptions;
	private int pow2MaxImageWidth;
	private int pow2MaxImageHeight;
	private static int idCounter = 0;

	private static final int DEBUG = 0;
	
	private TextureAtlasMergeInfo textureAtlasMergeInfo = new TextureAtlasMergeInfo();

	public TextureAtlasGenerator(LoaderOptions loaderOptions) {
		this.loaderOptions = loaderOptions;

		pow2MaxImageHeight = (int) Math.pow(2, Math.floor(Math.log10(loaderOptions.getMaxImageHeight()) / Math.log10(2)));
		pow2MaxImageWidth = (int) Math.pow(2, Math.floor(Math.log10(loaderOptions.getAtlasMaxWidth()) / Math.log10(2)));
	}

	
	
	public void run(Group group) {
		getTextureAtlasMergeInfo(group);
		
		generateTextureAtlas();
		
		
		deleteAllTexturedShapes(group);
	}
	
	
	
	
	private void deleteAllTexturedShapes(Node node){
		if(node instanceof Group){
			deleteAllTexturedShapes((Group)node);
		}
		else{
			System.err.println("TextureAtlasGenerator node type not supported: "+node.getClass().getName());
		}
	}
	
	
	private void deleteAllTexturedShapes(Group group){
		
		ArrayList<Shape3D> deleteShapes = new ArrayList<Shape3D>(); 
		int numChildren = group.numChildren();
		for(int i=0; i<numChildren; i++){
			Node childNode = group.getChild(i);
			if(childNode instanceof Shape3D){
				Shape3D shape = (Shape3D)childNode;
				Appearance appearance = shape.getAppearance();
			}
			else{
				deleteAllTexturedShapes(childNode);
			}
		}
	}

	
	
	private void getTextureAtlasMergeInfo(Node node){
		if(node instanceof Shape3D){
			getTextureAtlasMergeInfo((Shape3D)node);
		}
		else if(node instanceof Group){
			getTextureAtlasMergeInfo((Group)node);
		}
		else{
			System.err.println("TextureAtlasGenerator node type not supported: "+node.getClass().getName());
		}
	}

	private void getTextureAtlasMergeInfo(Group group){
		int numChildren = group.numChildren();
		for(int i=0; i<numChildren; i++){
			Node childNode = group.getChild(i);
			if(childNode instanceof Shape3D){
				getTextureAtlasMergeInfo((Shape3D)childNode);
			}
			else{
				getTextureAtlasMergeInfo(childNode);
			}
		}
	}

	
	
	
	
	

	public void getTextureAtlasMergeInfo(Shape3D shape) {

		
		AppearanceInfo appearanceInfo = textureAtlasMergeInfo.getAppearanceInfo();
		ArrayList<TextureInfo> allTextureInfos = appearanceInfo.getAllTextureInfos();
	//	HashMap<String, TextureInfo> imageUriTextureInfoMap = appearanceInfo.getImageUriTextureInfoMap();
	//	HashMap<String, Material> materialMap = appearanceInfo.getMaterialMap();
	//	HashMap<String, Material> multiSurfaceMaterialMap = appearanceInfo.getMultiSurfaceMaterialMap();
		HashMap<String, TextureInfo> ringIdTextureInfoMap = appearanceInfo.getRingIdTextureInfoMap();


	//	Map<Appearance, GeometryArrayBuilder> appearanceGeometryArrayBuilderMap = textureAtlasMergeInfo.getAppearanceGeometryArrayBuilderMap();
	//	HashMap<String, Appearance> appearanceMap = textureAtlasMergeInfo.getAppearanceMap();
		HashMap<String, Point3f[]> ringIdVerticesMap = textureAtlasMergeInfo.getRingIdVerticesMap();
		
		HashMap<Texture, TextureInfo> textureTextureInfoMap = new HashMap<Texture, TextureInfo>();

			Appearance appearance = shape.getAppearance();
			if(appearance != null){
				
				
				Texture texture = appearance.getTexture();
				if(texture != null){
					
					TextureInfo textureInfo = textureTextureInfoMap.get(texture);
					if(textureInfo == null){
						textureInfo = new TextureInfo(null);
						textureTextureInfoMap.put(texture, textureInfo);
						allTextureInfos.add(textureInfo);
					}
					
					
					
					Transform3D localToVworld = new Transform3D();
					shape.getLocalToVworld(localToVworld);
					
					
					HashMap<String, Point2f[]> ringIdTextureCoordinateMap = textureInfo.getRingIdTextureCoordinateMap();
					
					int numGeometries = shape.numGeometries();
					for(int i=0; i<numGeometries; i++){
						Geometry geometry = shape.getGeometry(i);
						if(geometry instanceof TriangleArray){
							TriangleArray ta = (TriangleArray)geometry;
							int vertexFormat = ta.getVertexFormat();
							int vertexCount = ta.getVertexCount();
							int numTriangles = vertexCount/3;
							if((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0){
								
								float[] coordinateData = new float[vertexCount*3];
								ta.getCoordinate(0, coordinateData);
								
								float[] textureCoordinateData = new float[vertexCount*2];
								ta.getTextureCoordinates(0, textureCoordinateData);
								
								for(int r=0; r<numTriangles; r++){
									String ringId = geometry.hashCode()+"_"+r;
									
									ringIdTextureInfoMap.put(ringId, textureInfo);
									
									Point2f[] ringTextureCoordinates = new Point2f[3];
									int tOffset = r*3*2;
									ringTextureCoordinates[0] = new Point2f(textureCoordinateData[tOffset+0], textureCoordinateData[tOffset + 1]);
									ringTextureCoordinates[1] = new Point2f(textureCoordinateData[tOffset+2], textureCoordinateData[tOffset + 3]);
									ringTextureCoordinates[2] = new Point2f(textureCoordinateData[tOffset+4], textureCoordinateData[tOffset + 5]);
									ringIdTextureCoordinateMap.put(ringId, ringTextureCoordinates);
									
									Point3f[] ringCoordinates = new Point3f[3];
									int cOffset = r*3*3;
									ringCoordinates[0] = new Point3f(coordinateData[cOffset+0], coordinateData[cOffset + 1], coordinateData[cOffset + 2]);
									ringCoordinates[1] = new Point3f(coordinateData[cOffset+3], coordinateData[cOffset + 4], coordinateData[cOffset + 5]);
									ringCoordinates[2] = new Point3f(coordinateData[cOffset+6], coordinateData[cOffset + 7], coordinateData[cOffset + 8]);
									
									localToVworld.transform(ringCoordinates[0]);
									localToVworld.transform(ringCoordinates[1]);
									localToVworld.transform(ringCoordinates[2]);
									
									ringIdVerticesMap.put(ringId, ringCoordinates);
								}
								
								
							}

						}
						else{
							System.err.println("TextureAtlasGenerator geometry not supported: "+geometry.getClass().getName());
						}
					}
				}
			}
		

	}




	public void generateTextureAtlas() {

		AppearanceInfo appearanceInfo = textureAtlasMergeInfo.getAppearanceInfo();

		HashMap<SimpleTexture, TextureInfo> atlasTextureTextureInfoMap = new HashMap<SimpleTexture, TextureInfo>();
		HashMap<String, SimpleTexture> ringIdImageUriMap = new HashMap<String, SimpleTexture>();
		HashMap<String, Point2f[]> ringIdTextureCoordinatesMap = new HashMap<String, Point2f[]>();

		Set<Entry<SimpleTexture, TextureInfo>> entrySet10 = appearanceInfo.getTextureTextureInfoMap().entrySet();
		Iterator<Entry<SimpleTexture, TextureInfo>> it10 = entrySet10.iterator();
		while(it10.hasNext()){
			Entry<SimpleTexture, TextureInfo> entry = it10.next();
			SimpleTexture texture = entry.getKey();
			TextureInfo textureInfo = entry.getValue();
			atlasTextureTextureInfoMap.put(texture, textureInfo);

			Set<Entry<String, Point2f[]>> entrySet1 = textureInfo.getRingIdTextureCoordinateMap().entrySet();
			Iterator<Entry<String, Point2f[]>> it1 = entrySet1.iterator();
			while(it1.hasNext()){
				Entry<String, Point2f[]> entry1 = it1.next();
				String ringId = entry1.getKey();
				Point2f[] textureCoordinates = entry1.getValue();
				ringIdTextureCoordinatesMap.put(ringId, textureCoordinates);
				ringIdImageUriMap.put(ringId, texture);
			}
		}


		Map<String, TextureInfo> ringIdTextureInfoMap = appearanceInfo.getRingIdTextureInfoMap();

		HashMap<SimpleTexture, Boolean> acceptedTextureAtlasTextures = new HashMap<SimpleTexture, Boolean>();
		HashMap<SimpleTexture, SimpleTexture> textureMapping = new HashMap<SimpleTexture, SimpleTexture>();
		HashMap<SimpleTexture, Float> textureScaleMap = new HashMap<SimpleTexture, Float>();

		// create packers for 3 and 4 channels textures.
		Packer packer3C = new Packer(pow2MaxImageWidth, pow2MaxImageHeight, false);
		Packer packer4C = new Packer(pow2MaxImageWidth, pow2MaxImageHeight, true);

		if (ringIdImageUriMap == null || ringIdImageUriMap.isEmpty()) {
			// it does not contain any texture!
			return;
		}

		// step 1: go through all objects having texture information and
		// check whether the (rescaled) texture image fits the binding box
		// of the atlas and whether the texture coordinates are sane
		for (Entry<String, SimpleTexture> entry : ringIdImageUriMap.entrySet()) {
			String ringId = entry.getKey();
			SimpleTexture textureUrl = entry.getValue();
			SimpleTexture textureImage = null;
			TextureInfo textureInfo = null;

			// change the name of the texture image if not already done so
			SimpleTexture mapping = textureMapping.get(textureUrl);
			if (mapping == null) {
				textureInfo = atlasTextureTextureInfoMap.get(textureUrl);						
				textureImage = textureInfo.getTexture();
//				if(textureImage == null){
//					String imageUri = textureInfo.getSourceUri();
//					try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(imageUri));	){  
//						String suffix =	imageUri.substring(imageUri.lastIndexOf('.') + 1).toLowerCase();
//						String ldr_format = "RGBA";
//						if (suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("jpeg") || suffix.equalsIgnoreCase("jp2") || suffix.equalsIgnoreCase("j2c")) {
//							ldr_format = "RGB";
//						}				
//						TextureLoader loader = new TextureLoader(imageUri, ldr_format, TextureLoader.BY_REFERENCE|TextureLoader.ALLOW_NON_POWER_OF_TWO, null);
//						textureImage = (TextureImage)loader.getTexture();
//						textureImage.setUserData(textureInfo.getTargetUri());
//						textureInfo.setTextureImage(textureImage);
//						
//						Texture2D texture2D = new Texture2D();
//						
//					}
//					catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
				if (textureImage != null && textureImage.getBufferedImage() != null) {
//					mapping = getNewTexImageName(textureUrl, textureImage.getChannels());
					mapping = textureUrl;
					atlasTextureTextureInfoMap.remove(textureUrl);
					atlasTextureTextureInfoMap.put(mapping, textureInfo);
					textureMapping.put(textureUrl, mapping);
				} else
					continue;
			}

			// update texture image mapping
			textureUrl = mapping;
			ringIdImageUriMap.put(ringId, mapping);

			if (textureInfo == null){
				continue;	
			}
			// check whether texture image can be decoded
			if (textureImage.getBufferedImage() == null) {
				acceptedTextureAtlasTextures.put(textureUrl, false);
				continue;			
			}

			// get number of channels
			boolean hasFourChannels = textureImage.getChannels() == 4;

			// the image was accepted before
			Boolean isAccepted = acceptedTextureAtlasTextures.get(textureUrl);
			if (isAccepted != null) {
				if (!isAccepted.booleanValue()) {
					// the texture images has already been rejected
					continue;
				}

				// if the image has already been accepted,
				// we only need to check the texture coordinates 
				Point2f[] textureCoordinates = ringIdTextureCoordinatesMap.get(ringId);
				boolean textureCoordinatesValid = checkValidTexCoordinates(textureCoordinates);
				if (!textureCoordinatesValid) {
					// in a previous run the texture coordinates of another object pointing to this texture image
					// could be successfully parsed. Now we failed to parse the texture coordinates of this object.
					// Hence, the texture image is not accepted in order to keep the original texturing.
					acceptedTextureAtlasTextures.put(textureUrl, false);

					// remove the texture image from the packer
					if (hasFourChannels) {
						packer4C.removeRegion(textureUrl);
					} else {
						packer3C.removeRegion(textureUrl);
					}

					continue;
				}

				continue;
			}

			// check texture coordinates of object
			Point2f[] ringTextureCoordinates = ringIdTextureCoordinatesMap.get(ringId);
			boolean textureCoordinatesValid = checkValidTexCoordinates(ringTextureCoordinates);
			if (!textureCoordinatesValid) {
				acceptedTextureAtlasTextures.put(textureUrl, false);
				//continue;
			}

			int width = textureImage.getWidth();
			int height = textureImage.getHeight();
			float imageScale = 1f;

			Map<String, Point3f[]> ringIdVerticesMap = textureAtlasMergeInfo.getRingIdVerticesMap();
			Point3f[] ringCoordinates = ringIdVerticesMap.get(ringId);
			if( ringCoordinates != null){
				int numRingCoordinates = ringCoordinates.length;
				if(numRingCoordinates > 2 && ringTextureCoordinates.length == numRingCoordinates){
					Vector3f vec3f = new Vector3f();
					Vector2f vec2f = new Vector2f();
					float length3f = 0f;
					float length2f = 0f;
					for(int i=0; i<numRingCoordinates; i++){
						Point3f v0 = ringCoordinates[i];
						Point3f v1 = ringCoordinates[(i+1)%numRingCoordinates];

						Point2f t0 = ringTextureCoordinates[i];
						Point2f t1 = ringTextureCoordinates[(i+1)%numRingCoordinates];

						vec3f.x = v1.x-v0.x;
						vec3f.y = v1.y-v0.y;
						vec3f.z = v1.z-v0.z;
						length3f += vec3f.length();

						vec2f.x = (t1.x-t0.x)*width;
						vec2f.y = (t1.y-t0.y)*height;
						length2f += vec2f.length();
					}


					float texelDensity = length2f/length3f;
					imageScale = 1f/(loaderOptions.getTargetTexelSize()*texelDensity);
					if(imageScale > 1f) imageScale = 1f;
					//	log.debug"imageScale "+imageScale);

					width *= imageScale;
					height *= imageScale;
				}
			}
			else{
				System.err.println("TextureAtlasGenerator.run  numRingCoordinates == null");
			}
			if(width > pow2MaxImageWidth || height > pow2MaxImageHeight){
				float wfac = (float)pow2MaxImageWidth/width;
				float hfac = (float)pow2MaxImageHeight/height;
				float fac = Math.min(wfac, hfac); 
				width *= fac;
				height *= fac;
				imageScale *= fac;
			}

			textureScaleMap.put(textureUrl, imageScale);

			if (textureCoordinatesValid) {
				if (hasFourChannels) {
					packer4C.addRegion(textureUrl, width, height, imageScale);
				} else {
					packer3C.addRegion(textureUrl, width,height, imageScale);
				}

				acceptedTextureAtlasTextures.put(textureUrl, true);
			}
		}



		// step 2: the previous step resulted in
		// - unsupported images; they will not be touched
		// - a group of 3-channel images
		// - a group of 4-channel images
		// next, atlases will be generated for both 3-channel and 4-channel groups.  

		ArrayList<TextureAtlas> atlasMR = new ArrayList<TextureAtlas>(2);
		if (packer4C.getRegions() != 0){
			TextureAtlas t = pack(packer4C);
			atlasMR.add(t);
		}
		if (packer3C.getRegions() != 0){
			TextureAtlas t = pack(packer3C);
			atlasMR.add(t);
		}

		// for all available atlases modify the coordinates and image names
		for (TextureAtlas atlas : atlasMR) {	
			if (Math.min(atlas.getBindingBoxHeight(), atlas.getBindingBoxWidth()) < 1)
				continue;

			// list of all items which will be drawn in a same atlas
			List<AtlasRegion> regions = atlas.getRegions();	
			boolean hasFourChannels = atlas.hasFourChannels();

			Map<Integer, ArrayList<AtlasRegion>> fileAtlasRegionMap = new HashMap<Integer, ArrayList<AtlasRegion>>();

			for (AtlasRegion region : regions) {
				ArrayList<AtlasRegion> fileRegions = fileAtlasRegionMap.get(region.file);
				if(fileRegions == null){
					fileRegions = new ArrayList<AtlasRegion>();
					fileAtlasRegionMap.put(region.file, fileRegions);
				}
				fileRegions.add(region);
			}

			Set<Entry<Integer, ArrayList<AtlasRegion>>> entrySet = fileAtlasRegionMap.entrySet();
			Iterator<Entry<Integer, ArrayList<AtlasRegion>>> it = entrySet.iterator();
			int file=0;
			while(it.hasNext()){
				Entry<Integer, ArrayList<AtlasRegion>> entry = it.next();
				ArrayList<AtlasRegion> levelRegions = entry.getValue();

				Map<Integer, Dimension> fileDimensions = atlas.getFileDimensions();
				Dimension fileDimension = fileDimensions.get(file);

				int atlasWidth = Math.min(getMinCoveredPOT(fileDimension.width), pow2MaxImageWidth);
				int atlasHeight = Math.min(getMinCoveredPOT(fileDimension.height),pow2MaxImageHeight);
				int imageType = hasFourChannels ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

				BufferedImage atlasImage = new BufferedImage(atlasWidth, atlasHeight, imageType);

				SimpleTexture atlasTexture = new SimpleTexture(SimpleTexture.BASE_LEVEL, TextureInfo.chooseTextureFormat(atlasImage), atlasWidth, atlasHeight);
				String atlasName =  "textureAtlas_" + (idCounter++)+"."+ (hasFourChannels ? "png" : "jpg"); 

				TextureInfo atlasTextureInfo = new TextureInfo(null);
				atlasTextureInfo.setIsTextureAtlas(true);
				atlasTextureInfo.setTexture(atlasTexture);

				Graphics2D graphics2D = atlasImage.createGraphics();

				for (AtlasRegion region : levelRegions) {

					TextureInfo regionTextureInfo = atlasTextureTextureInfoMap.get(region.getTexImageName());
					SimpleTexture regionTexture = regionTextureInfo.getTexture();

					AffineTransform xform = new AffineTransform();
					xform.setToTranslation(region.x, region.y);
					if (region.isRotated) {
						xform.rotate(Math.PI*0.5);
						xform.scale(1.0, -1.0);
					}

					if(region.imageScale != 1f){
						xform.scale(region.imageScale, region.imageScale);
					}

					if(hasFourChannels){
						if(DEBUG == 1) {
							System.out.println("draw into "+atlasName+" from "+region.getTexImageName()+" level "+region.file+" region "+region.x+" "+region.y+" "+region.width+" "+region.height);
						}
					}
					graphics2D.drawImage(regionTexture.getBufferedImage(), xform, null);

					atlasTextureTextureInfoMap.remove(region.getTexImageName());
					regionTextureInfo.setTexture(null);


					HashMap<String, Point2f[]> textureCoordinateMap = regionTextureInfo.getRingIdTextureCoordinateMap();
					if(textureCoordinateMap != null){
						Set<Entry<String, Point2f[]>> entrySet0 = textureCoordinateMap.entrySet();
						for(Entry<String, Point2f[]> entry0 : entrySet0){
							String ringId = entry0.getKey();

							ringIdTextureInfoMap.put(ringId, atlasTextureInfo);

							Point2f[] textureCoordinates = entry0.getValue();
							if(textureCoordinates != null){
								for(Point2f textureCoordinate : textureCoordinates){
									float x = textureCoordinate.x;
									float y = textureCoordinate.y;

									while(x<0.0){
										x += 1.0;
									}
									while(y<0.0){
										y += 1.0;
									}

									if (region.isRotated) {
										textureCoordinate.x = ((float)region.x + (1f-y)*(float)region.width)/(float)atlasWidth;
										textureCoordinate.y = ((float)(atlasHeight-region.y-region.height) + (1f-x)*(float)region.height)/(float)atlasHeight;	
									}
									else{
										textureCoordinate.x = ((float)region.x + x*(float)region.width)/(float)atlasWidth; 
										textureCoordinate.y = ((float)(atlasHeight-region.y-region.height) + y*(float)region.height)/(float)atlasHeight;
									}
								}
							}

						}
					}
				}

				graphics2D.dispose();

				if (atlasHeight != 0 && atlasWidth != 0) {

					int ic_format = TextureInfo.chooseImageComponentFormat(atlasImage);
					ImageComponent2D base_image_component = new ImageComponent2D(ic_format, atlasImage);
					base_image_component.setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
					atlasTexture.setImage(0, base_image_component);
					//atlasTextureImage.setUri(atlasName);
					atlasTexture.setUserData(atlasName);
					atlasTextureInfo.setTargetUri(atlasName);

					atlasTextureTextureInfoMap.put(atlasTexture , atlasTextureInfo);
				}
				file++;
			}

		}


		Map<SimpleTexture, TextureInfo> modifiedTextureTextureMap = atlasTextureTextureInfoMap;			
		Set<Entry<SimpleTexture, TextureInfo>> es3 = modifiedTextureTextureMap.entrySet();
		Iterator<Entry<SimpleTexture, TextureInfo>> it3 = es3.iterator();
		while(it3.hasNext()){
			Entry<SimpleTexture, TextureInfo> entry3 = it3.next();
			SimpleTexture atlasImageUri = entry3.getKey();
			TextureInfo atlasTextureInfo = entry3.getValue();
			SimpleTexture atlasTextureImage = atlasTextureInfo.getTexture();
			if(atlasTextureImage != null){
				appearanceInfo.getTextureTextureInfoMap().put(atlasImageUri, atlasTextureInfo);
			}
		}


		Set<Entry<SimpleTexture, Boolean>> es20 = acceptedTextureAtlasTextures.entrySet();
		Iterator<Entry<SimpleTexture, Boolean>> it20 = es20.iterator();
		while(it20.hasNext()){
			Entry<SimpleTexture, Boolean> e20 = it20.next();
			SimpleTexture textureUrl = e20.getKey();
			Boolean isAccepted = e20.getValue();
			//log.debug"textureUrl "+textureUrl+" isAccepted "+isAccepted);
			if(!isAccepted){
				float imageScale = textureScaleMap.get(textureUrl);
				//log.debug"textureUrl "+textureUrl+" imageScale "+imageScale);

				//String mappedUrl = textureUrlMapping.get(textureUrl);

				HashMap<SimpleTexture, TextureInfo> textureTextureInfoMap = appearanceInfo.getTextureTextureInfoMap();
				TextureInfo textureInfo = textureTextureInfoMap.get(textureUrl);

				SimpleTexture texture = textureInfo.getTexture();

				int orgWidth = texture.getWidth();
				int orgHeight = texture.getHeight();
				int targetWidth = Math.max(1, (int)(orgWidth*imageScale));
				int targetHeight = Math.max(1, (int)(orgHeight*imageScale));

				if(targetWidth < orgWidth || targetHeight < orgHeight){


					boolean hasFourChannels = texture.getChannels() == 4;

					int imageType = hasFourChannels ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
					BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, imageType);


					SimpleTexture scaledTexture = new SimpleTexture(SimpleTexture.BASE_LEVEL, TextureInfo.chooseTextureFormat(scaledImage), targetWidth, targetHeight);
					String scaledTextureImageName =  texture.getName(); 
					scaledTexture.setName(scaledTextureImageName);
					scaledTexture.setUserData(texture.getUserData());

					Graphics2D graphics2D = scaledImage.createGraphics();

					AffineTransform xform = new AffineTransform();
					//xform.setToTranslation(0, 0);
					xform.scale(imageScale, imageScale);

					graphics2D.drawImage(texture.getBufferedImage(), xform, null);
					graphics2D.dispose();


					int ic_format = TextureInfo.chooseImageComponentFormat(scaledImage);
					ImageComponent2D base_image_component = new ImageComponent2D(ic_format, scaledImage);
					base_image_component.setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
					scaledTexture.setImage(0, base_image_component);

					textureInfo.setTexture(scaledTexture);
				}
			}
		}
	}




	private String getNewTexImageName(String prevURI, int channel) {
		return prevURI.substring(0, prevURI.lastIndexOf('.')) + (channel == 3 ? ".jpg" : ".png");
	}



	private int getMinCoveredPOT(int len){
		return len;
	}


	private TextureAtlas pack(Packer packer) {
		packer.setBinSize(pow2MaxImageWidth, pow2MaxImageHeight);
		TextureAtlas textureAtlas = packer.pack();
		return textureAtlas;
	}



	private boolean checkValidTexCoordinates(Point2f[] coordinates){
		if (coordinates==null || coordinates.length == 0){
			return false;
		}

		for (int i=0; i<coordinates.length; i++){
			float value = coordinates[i].x;
			if (value < -0.01 || value > 1.01)
				return false;

			value = coordinates[i].y;
			if (value < -0.01 || value > 1.01)
				return false;
		}

		return true;
	}



}
