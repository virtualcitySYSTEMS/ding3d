package javax.media.ding3d.utils.textureatlas;


import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.HashMap;
import java.util.Map;

import javax.media.ding3d.ImageComponent;
import javax.media.ding3d.Texture;
import javax.media.ding3d.vecmath.Point2f;

public class TextureInfo {


	private SimpleTexture texture;
	private String sourceUri;
	private String targetUri;
	private HashMap<String, Point2f[]> ringIdTextureCoordinateMap = new HashMap<String, Point2f[]>();
	private boolean isTextureAtlas;


	private TextureInfo(){

	}





	private static int texCounter = 0;

	public TextureInfo(String sourceUri) {
		this.sourceUri = sourceUri;
		
		if(sourceUri != null){
	
			String suffix =	sourceUri.substring(sourceUri.lastIndexOf('.') + 1).toLowerCase();
			String targetImageUri = "tex"+(texCounter++)+"."+suffix;
			setTargetUri(targetImageUri);	
		}
	}



	public SimpleTexture getTexture() {
		return texture;
	}




	public TextureInfo copy(){
		TextureInfo copy = new TextureInfo();
		copy.setSourceUri(sourceUri);
		copy.setTargetUri(targetUri);
		copy.setTexture(texture);


		copy.getRingIdTextureCoordinateMap().putAll(ringIdTextureCoordinateMap);


		return copy;
	}


	public void setTexture(SimpleTexture texture) {
		this.texture = texture;
	}


	public String getSourceUri() {
		return sourceUri;
	}


	public void setSourceUri(String sourceUri) {
		this.sourceUri = sourceUri;
	}


	public String getTargetUri() {
		return targetUri;
	}


	public void setTargetUri(String targetUri) {
		this.targetUri = targetUri;
	}


	public HashMap<String, Point2f[]> getRingIdTextureCoordinateMap() {
		return ringIdTextureCoordinateMap;
	}

	public boolean isTextureAtlas() {
		return isTextureAtlas;
	}


	public void setIsTextureAtlas(boolean isTextureAtlas) {
		this.isTextureAtlas = isTextureAtlas;
	}

	public static int chooseTextureFormat(BufferedImage image) {

		int textureFormat = Texture.RGBA;
		switch (image.getType()) {
		case BufferedImage.TYPE_4BYTE_ABGR :
		case BufferedImage.TYPE_INT_ARGB :
			textureFormat = Texture.RGBA;
			break;
		case BufferedImage.TYPE_3BYTE_BGR :
		case BufferedImage.TYPE_INT_BGR:
		case BufferedImage.TYPE_INT_RGB:
			textureFormat = Texture.RGB;
			break;
		case BufferedImage.TYPE_CUSTOM:
			if (is4ByteRGBAOr3ByteRGB(image)) {
				SampleModel sm = image.getSampleModel();
				if (sm.getNumBands() == 3) {
					textureFormat = Texture.RGB;
				}
				else {
					textureFormat = Texture.RGBA;
				}
			}
			break;
		default :
			textureFormat = Texture.RGBA;
			break;
		}

		return textureFormat;
	}




	public static int chooseImageComponentFormat(BufferedImage image) {
		int imageComponentFormat = ImageComponent.FORMAT_RGBA;
		switch (image.getType()) {
		case BufferedImage.TYPE_4BYTE_ABGR :
		case BufferedImage.TYPE_INT_ARGB :
			imageComponentFormat = ImageComponent.FORMAT_RGBA;
			break;
		case BufferedImage.TYPE_3BYTE_BGR :
		case BufferedImage.TYPE_INT_BGR:
		case BufferedImage.TYPE_INT_RGB:
			imageComponentFormat = ImageComponent.FORMAT_RGB;
			break;
		case BufferedImage.TYPE_CUSTOM:
			if (is4ByteRGBAOr3ByteRGB(image)) {
				SampleModel sm = image.getSampleModel();
				if (sm.getNumBands() == 3) {
					imageComponentFormat = ImageComponent.FORMAT_RGB;
				}
				else {
					imageComponentFormat = ImageComponent.FORMAT_RGBA;
				}
			}
			break;
		default :
			// System.err.println("Unoptimized Image Type "+image.getType());
			imageComponentFormat = ImageComponent.FORMAT_RGBA;
			break;
		}

		return imageComponentFormat;
	}



	private static int getImageType(RenderedImage ri) {
		int imageType = BufferedImage.TYPE_CUSTOM;
		int i;

		if (ri instanceof BufferedImage) {
			return ((BufferedImage)ri).getType();
		}
		ColorModel cm = ri.getColorModel();
		ColorSpace cs = cm.getColorSpace();
		SampleModel sm = ri.getSampleModel();
		int csType = cs.getType();
		boolean isAlphaPre = cm.isAlphaPremultiplied();
		if ( csType != ColorSpace.TYPE_RGB) {
			if (csType == ColorSpace.TYPE_GRAY &&
					cm instanceof ComponentColorModel) {
				if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
					imageType = BufferedImage.TYPE_BYTE_GRAY;
				} else if (sm.getDataType() == DataBuffer.TYPE_USHORT) {
					imageType = BufferedImage.TYPE_USHORT_GRAY;
				}
			}
		}
		// RGB , only interested in BYTE ABGR and BGR for now
		// all others will be copied to a buffered image
		else {
			int numBands = sm.getNumBands();
			if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
				if (cm instanceof ComponentColorModel &&
						sm instanceof PixelInterleavedSampleModel) {
					PixelInterleavedSampleModel csm =
							(PixelInterleavedSampleModel) sm;
					int[] offs = csm.getBandOffsets();
					ComponentColorModel ccm = (ComponentColorModel)cm;
					int[] nBits = ccm.getComponentSize();
					boolean is8Bit = true;
					for (i=0; i < numBands; i++) {
						if (nBits[i] != 8) {
							is8Bit = false;
							break;
						}
					}
					if (is8Bit &&
							offs[0] == numBands-1 &&
							offs[1] == numBands-2 &&
							offs[2] == numBands-3) {
						if (numBands == 3) {
							imageType = BufferedImage.TYPE_3BYTE_BGR;
						}
						else if (offs[3] == 0) {
							imageType = (isAlphaPre
									? BufferedImage.TYPE_4BYTE_ABGR_PRE
											: BufferedImage.TYPE_4BYTE_ABGR);
						}
					}
				}
			}
		}
		return imageType;
	}

	private static boolean is4ByteRGBAOr3ByteRGB(RenderedImage ri) {
		boolean value = false;
		int i;
		int biType = getImageType(ri);
		if (biType != BufferedImage.TYPE_CUSTOM)
			return false;
		ColorModel cm = ri.getColorModel();
		ColorSpace cs = cm.getColorSpace();
		SampleModel sm = ri.getSampleModel();
		boolean isAlphaPre = cm.isAlphaPremultiplied();
		int csType = cs.getType();
		if ( csType == ColorSpace.TYPE_RGB) {
			int numBands = sm.getNumBands();
			if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
				if (cm instanceof ComponentColorModel &&
						sm instanceof PixelInterleavedSampleModel) {
					PixelInterleavedSampleModel csm =
							(PixelInterleavedSampleModel) sm;
					int[] offs = csm.getBandOffsets();
					ComponentColorModel ccm = (ComponentColorModel)cm;
					int[] nBits = ccm.getComponentSize();
					boolean is8Bit = true;
					for (i=0; i < numBands; i++) {
						if (nBits[i] != 8) {
							is8Bit = false;
							break;
						}
					}
					if (is8Bit &&
							offs[0] == 0 &&
							offs[1] == 1 &&
							offs[2] == 2) {
						if (numBands == 3) {
							value = true;
						}
						else if (offs[3] == 3 && !isAlphaPre) {
							value = true;
						}
					}
				}
			}
		}
		return value;
	}

}
