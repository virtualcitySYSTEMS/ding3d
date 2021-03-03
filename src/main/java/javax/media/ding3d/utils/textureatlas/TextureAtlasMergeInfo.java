package javax.media.ding3d.utils.textureatlas;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.media.ding3d.Geometry;
import javax.media.ding3d.vecmath.Point3f;


public class TextureAtlasMergeInfo {

	//collects info from CityGML Appearances
	AppearanceInfo appearanceInfo = new AppearanceInfo();
	
	private ArrayList<Geometry> sourceGeometries = new ArrayList<Geometry>();
	
	
	//maps Ding3D Appearance keys to Ding3D Appearances
	private HashMap<String, javax.media.ding3d.Appearance> appearanceMap = new HashMap<String, javax.media.ding3d.Appearance>();

	

//	private Map<javax.media.ding3d.Appearance, GeometryArrayBuilder> appearanceGeometryArrayBuilderMap = new HashMap<javax.media.ding3d.Appearance, GeometryArrayBuilder>();

	private HashMap<String, Point3f[]> ringIdVerticesMap = new HashMap<String, Point3f[]>(); 

	public AppearanceInfo getAppearanceInfo() {
		return appearanceInfo;
	}

//	public Map<javax.media.ding3d.Appearance, GeometryArrayBuilder> getAppearanceGeometryArrayBuilderMap() {
//		return appearanceGeometryArrayBuilderMap;
//	}


	public HashMap<String, javax.media.ding3d.Appearance> getAppearanceMap() {
		return appearanceMap;
	}

	public HashMap<String, Point3f[]> getRingIdVerticesMap() {
		return ringIdVerticesMap;
	}
	
	

}
