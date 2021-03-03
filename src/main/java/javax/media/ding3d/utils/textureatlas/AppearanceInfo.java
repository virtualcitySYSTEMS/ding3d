package javax.media.ding3d.utils.textureatlas;



import java.util.ArrayList;
import java.util.HashMap;

import javax.media.ding3d.Material;
import javax.media.ding3d.Texture;

public class AppearanceInfo {


	//maps Ding3D Material keys to Ding3D Materials
	private HashMap<String, Material> materialMap = new HashMap<String, Material>();

	// maps GML MultiSurface Ids to Ding3D Material
	private HashMap<String, Material> multiSurfaceMaterialMap = new HashMap<String, Material>();

	//maps GML polygon IDs to  TextureImage
	private HashMap<String, TextureInfo> ringIdTextureInfoMap = new HashMap<String, TextureInfo>();

	//maps texture URLs to TextureImage
	private HashMap<SimpleTexture, TextureInfo> textureTextureInfoMap = new HashMap<SimpleTexture, TextureInfo>();


	private ArrayList<TextureInfo> allTextureInfos = new ArrayList<TextureInfo>();



	public ArrayList<TextureInfo> getAllTextureInfos() {
		return allTextureInfos;
	}

	public HashMap<String, Material> getMaterialMap() {
		return materialMap;
	}

	public HashMap<String, Material> getMultiSurfaceMaterialMap() {
		return multiSurfaceMaterialMap;
	}

	public HashMap<String, TextureInfo> getRingIdTextureInfoMap() {
		return ringIdTextureInfoMap;
	}

	public HashMap<SimpleTexture, TextureInfo> getTextureTextureInfoMap() {
		return textureTextureInfoMap;
	}


	public void clearTextureImages(){
		for(TextureInfo textureInfo : allTextureInfos){
			textureInfo.setTexture(null);
		}
	}




}
