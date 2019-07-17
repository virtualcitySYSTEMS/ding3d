package javax.media.ding3d.utils.textureatlas;


import java.awt.image.BufferedImage;

import javax.media.ding3d.ImageComponent2D;
import javax.media.ding3d.Texture2D;

public class SimpleTexture  extends Texture2D{
	


	public SimpleTexture(int multiLevelMipmap, int textureFormat, int width,	int height) {
		super(multiLevelMipmap, textureFormat, width, height);
	}
	

	public BufferedImage getBufferedImage() {
		BufferedImage bufferedImage = null;
		
		ImageComponent2D image_component = (ImageComponent2D)getImage(0);
		if(image_component != null){
			bufferedImage = image_component.getImage();
		}
		
		return bufferedImage;
	}

	public int getChannels(){
		int channels = 0;
		BufferedImage bufferedImage = getBufferedImage();
		if(bufferedImage != null){
			channels = bufferedImage.getTransparency() == java.awt.Transparency.OPAQUE ? 3 : 4;
		}
		
		return channels;
	}

		
	

	
	
}
