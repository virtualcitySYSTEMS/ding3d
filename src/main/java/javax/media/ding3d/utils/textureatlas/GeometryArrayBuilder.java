package javax.media.ding3d.utils.textureatlas;


import java.util.ArrayList;

import javax.media.ding3d.BatchedTriangleArray;
import javax.media.ding3d.GeometryArray;
import javax.media.ding3d.TriangleArray;
import javax.media.ding3d.utils.geometry.GeometryInfo;
import javax.media.ding3d.utils.geometry.Triangulator;
import javax.media.ding3d.vecmath.Point2f;
import javax.media.ding3d.vecmath.Point3f;
import javax.media.ding3d.vecmath.Vector3f;


public class GeometryArrayBuilder {

/*

	private static String TAG = GeometryArrayBuilder.class.getSimpleName();

	//final int MAX_VERTEX_COUNT = Short.MAX_VALUE;
	//final int MAX_VERTEX_COUNT_TRI = Short.MAX_VALUE;;
	final int MAX_VERTEX_COUNT = 60000;
	final int MAX_VERTEX_COUNT_TRI = 63000;

	private ArrayList<Point3f> coordinates = new ArrayList<Point3f>(); 
	private ArrayList<Point2f> textureCoordinates = new ArrayList<Point2f>(); 
	private ArrayList<Vector3f> normals = new ArrayList<Vector3f>(); 
	private ArrayList<Integer> stripCounts = new ArrayList<Integer>(); 
	private ArrayList<Integer> contourCounts = new ArrayList<Integer>();

	private ArrayList<Short> ids = new ArrayList<Short>();

	public static final int PRIMITIVE_TYPE_TRIANGLES = 0;
	public static final int PRIMITIVE_TYPE_POLYGONS = 1;

	private int primitiveType = PRIMITIVE_TYPE_POLYGONS;
	private boolean batched = false;


	public GeometryArrayBuilder(int primitiveType, boolean batched){
		this.primitiveType = primitiveType;
		this.batched = batched;
	}



	public int getPrimitiveType() {
		return primitiveType;
	}



	public ArrayList<Short> getIds() {
		return ids;
	}

	public ArrayList<Point3f> getCoordinates() {
		return coordinates;
	}
	public ArrayList<Vector3f> getNormals() {
		return normals;
	}
	public ArrayList<Integer> getStripCounts() {
		return stripCounts;
	}
	public ArrayList<Integer> getContourCounts() {
		return contourCounts;
	} 

	public ArrayList<Point2f> getTextureCoordinates() {
		return textureCoordinates;
	}


	public GeometryArray[] getGeometryArray(boolean triangulate, boolean doubleSided){

		ArrayList<GeometryArray> geometries = new ArrayList<GeometryArray>(); 

		if(coordinates.size()>0){

			GeometryArray geometry = null;
			boolean isTextured = false;

			if(primitiveType == PRIMITIVE_TYPE_TRIANGLES){

				Point3f[] coordinatesArray = coordinates.toArray(new Point3f[0]);
				int vertexCount = coordinatesArray.length;
				int vertexFormat = GeometryArray.COORDINATES;


				Vector3f[] normalsArray = null;
				if(normals.size() == vertexCount){
					normalsArray = normals.toArray(new Vector3f[0]);
					vertexFormat |= GeometryArray.NORMALS;
				}
				else{
					System.err.println(TAG+".getGeometryArray number of normals must be equal to number of coordinates: "+normals.size()+" != "+vertexCount);
				}

				Point2f[] textureCoordinatesArray = null;
				if(textureCoordinates != null && textureCoordinates.size()>0){
					if(textureCoordinates.size() == vertexCount){
						textureCoordinatesArray = textureCoordinates.toArray(new Point2f[0]);
						isTextured = true;
						vertexFormat |= GeometryArray.TEXTURE_COORDINATE_2;
					}
					else{
						System.err.println(TAG+".getGeometryArray number of textureCoordinates must be equal to number of coordinates: "+textureCoordinates.size()+" != "+vertexCount);
					}
				}

				if(this.batched){
					BatchedTriangleArray batchedTriangleArray = new BatchedTriangleArray(vertexCount, vertexFormat);
					batchedTriangleArray.setCoordinates(0, coordinatesArray);
					if(normalsArray != null){
						batchedTriangleArray.setNormals(0, normalsArray);
					}
					if(textureCoordinatesArray != null){
						batchedTriangleArray.setTextureCoordinates(0, textureCoordinatesArray);
					}

					if(ids.size() == vertexCount){
						short[] idsArray = new short[vertexCount];
						for(int b=0; b<vertexCount; b++){
							idsArray[b] = ids.get(b);
						}
						batchedTriangleArray.setBatchIds(idsArray);
					}
					else{
						System.err.println(TAG+".getGeometryArray number of ids must be equal to number of coordinates: "+ids.size()+" != "+vertexCount);
					}
					geometry = batchedTriangleArray;
				}
				else{
					TriangleArray triangleArray = new TriangleArray(vertexCount, vertexFormat);
					triangleArray.setCoordinates(0, coordinatesArray);
					if(normalsArray != null){
						triangleArray.setNormals(0, normalsArray);
					}
					if(textureCoordinatesArray != null){
						triangleArray.setTextureCoordinates(0, textureCoordinatesArray);
					}

					geometry = triangleArray;
				}
			}
			else if(primitiveType == PRIMITIVE_TYPE_POLYGONS){
				GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);

				Point3f[] coordinatesArray = coordinates.toArray(new Point3f[0]);
				int vertexCount = coordinatesArray.length;

				short[] idsArray = new short[vertexCount];
				for(int b=0; b<vertexCount; b++){
					idsArray[b] = ids.get(b);
				}


				int numStripCounts = stripCounts.size();
				int[] stripCountsArray = new int[numStripCounts];
				for(int i=0; i<numStripCounts; i++){
					stripCountsArray[i] = stripCounts.get(i);
				}

				int numContourCounts = contourCounts.size();
				int[] contourCountsArray = new int[numContourCounts];
				for(int i=0; i<numContourCounts; i++){
					contourCountsArray[i] = contourCounts.get(i);
				}

				gi.setCoordinates(coordinatesArray);

				Vector3f[] normalsArray = normals.toArray(new Vector3f[0]);
				if(normalsArray.length == vertexCount){
					gi.setNormals(normalsArray);
				}

				gi.setStripCounts(stripCountsArray);	
				gi.setContourCounts(contourCountsArray);


				if(textureCoordinates != null && textureCoordinates.size()>0){
					if(textureCoordinates.size() == vertexCount){
						Point2f[] textureCoordinatesArray = textureCoordinates.toArray(new Point2f[0]);
						gi.setTextureCoordinates(textureCoordinatesArray);
						isTextured = true;
					}
					else{
						System.err.println(TAG+".getGeometryArray number of textureCoordinates must be equal to number of coordinates: "+textureCoordinates.size()+" != "+vertexCount);
					}
				}

				if(triangulate){
					Triangulator triangulator = new Triangulator();
					try{
						triangulator.triangulate(gi);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}

				geometry = gi.getGeometryArray();
			}



			int geometryVertexCount = geometry.getVertexCount();
			if(geometryVertexCount > MAX_VERTEX_COUNT){
				if(geometry instanceof BatchedTriangleArray){
					BatchedTriangleArray batchedTriangleArray = (BatchedTriangleArray)geometry;

					int vertexOffset = 0;
					while(vertexOffset < geometryVertexCount){

						int target_vertexCount = Math.min(MAX_VERTEX_COUNT_TRI, geometryVertexCount-vertexOffset);																					

						int vertexFormat = TriangleArray.COORDINATES|TriangleArray.NORMALS;
						if(isTextured){
							vertexFormat |= TriangleArray.TEXTURE_COORDINATE_2;
						}

						BatchedTriangleArray targetGeometry = new BatchedTriangleArray(target_vertexCount, vertexFormat);

						float[] target_coordinates = new float[target_vertexCount*3];
						batchedTriangleArray.getCoordinates(vertexOffset, target_coordinates);
						targetGeometry.setCoordinates(0, target_coordinates);

						float[] target_normals = new float[target_vertexCount*3];
						batchedTriangleArray.getNormals(vertexOffset, target_normals);
						targetGeometry.setNormals(0, target_normals);

						short[] target_batchIds = new short[target_vertexCount];
						short[] batchedTriangleArrayBatchIds = batchedTriangleArray.getBatchIds();
						int batchedTriangleArrayBatchIdsLength = batchedTriangleArrayBatchIds.length;
						for(int i=0; i<target_vertexCount; i++){
							target_batchIds[i] = batchedTriangleArrayBatchIds[vertexOffset+i];
						}
						targetGeometry.setBatchIds(target_batchIds);

						if(isTextured){
							float[] target_textureCoordinates = new float[target_vertexCount*2];
							batchedTriangleArray.getTextureCoordinates(vertexOffset, target_textureCoordinates);
							targetGeometry.setTextureCoordinates(0, target_textureCoordinates);
						}
						geometries.add(targetGeometry);

						vertexOffset += MAX_VERTEX_COUNT_TRI;
					}
				}
				else 


					if(geometry instanceof TriangleArray){
						TriangleArray triangleArray = (TriangleArray)geometry;

						int vertexOffset = 0;
						while(vertexOffset < geometryVertexCount){

							int target_vertexCount = Math.min(MAX_VERTEX_COUNT_TRI, geometryVertexCount-vertexOffset);																					

							int vertexFormat = TriangleArray.COORDINATES|TriangleArray.NORMALS;
							if(isTextured){
								vertexFormat |= TriangleArray.TEXTURE_COORDINATE_2;
							}

							TriangleArray targetGeometry = new TriangleArray(target_vertexCount, vertexFormat);

							float[] target_coordinates = new float[target_vertexCount*3];
							triangleArray.getCoordinates(vertexOffset, target_coordinates);
							targetGeometry.setCoordinates(0, target_coordinates);

							float[] target_normals = new float[target_vertexCount*3];
							triangleArray.getNormals(vertexOffset, target_normals);
							targetGeometry.setNormals(0, target_normals);

							if(isTextured){
								float[] target_textureCoordinates = new float[target_vertexCount*2];
								triangleArray.getTextureCoordinates(vertexOffset, target_textureCoordinates);
								targetGeometry.setTextureCoordinates(0, target_textureCoordinates);
							}
							geometries.add(targetGeometry);

							vertexOffset += MAX_VERTEX_COUNT_TRI;
						}
					}


			}
			else{
				geometries.add(geometry);
			}
			//}
			//else{
			//	System.err.println(TAG+".getGeometryArray number of normals must be equal to number of coordinates: "+normalsArray.length+" != "+vertexCount);
			//}
		}

		if(doubleSided){
			ArrayList<GeometryArray> invertedGeometries = new ArrayList<GeometryArray>(); 
			for(GeometryArray geometry : geometries){
				GeometryArray invertedGeometry = invert(geometry);
				if(invertedGeometry != null){
					invertedGeometries.add(invertedGeometry);
				}
			}
			geometries.addAll(invertedGeometries);
		}



		return geometries.toArray(new GeometryArray[0]);
	}



	private GeometryArray invert(GeometryArray geometry) {
		GeometryArray invertedGeometry = null;

		int geometryVertexCount = geometry.getVertexCount();
		int vertexFormat = geometry.getVertexFormat();
		boolean isTextured = ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0);

		if(geometry instanceof BatchedTriangleArray){
			BatchedTriangleArray batchedTriangleArray = (BatchedTriangleArray)geometry;

			BatchedTriangleArray targetGeometry = new BatchedTriangleArray(geometryVertexCount, vertexFormat);
			
			float[] coordinates = new float[geometryVertexCount*3];
			float[] target_coordinates = new float[geometryVertexCount*3];
			batchedTriangleArray.getCoordinates(0, coordinates);
			int c=0;
			for(int i=geometryVertexCount-1; i>=0; i--){
				float x = coordinates[i*3];
				float y = coordinates[i*3+1];
				float z = coordinates[i*3+2];
				target_coordinates[c*3] = x;
				target_coordinates[c*3+1] = y;
				target_coordinates[c*3+2] = z;
				c++;
			}
			targetGeometry.setCoordinates(0, target_coordinates);
			
			
			float[] normals = new float[geometryVertexCount*3];
			float[] target_normals = new float[geometryVertexCount*3];
			batchedTriangleArray.getNormals(0, normals);
			c=0;
			for(int i=geometryVertexCount-1; i>=0; i--){
				float x = normals[i*3];
				float y = normals[i*3+1];
				float z = normals[i*3+2];
				target_normals[c*3] = -x;
				target_normals[c*3+1] = -y;
				target_normals[c*3+2] = -z;
				c++;
			}
			targetGeometry.setNormals(0, target_normals);
			
			

			short[] batchIds = batchedTriangleArray.getBatchIds();
			short[] target_batchIds = new short[geometryVertexCount];
			c=0;
			for(int i=geometryVertexCount-1; i>=0; i--){
				short batchId = batchIds[i];
				target_batchIds[c] = batchId;
				c++;
			}	
			targetGeometry.setBatchIds(target_batchIds);
			

			if(isTextured){
				float[] textureCoordinates = new float[geometryVertexCount*2];
				float[] target_textureCoordinates = new float[geometryVertexCount*2];
				batchedTriangleArray.getTextureCoordinates(0, textureCoordinates);
				c=0;
				for(int i=geometryVertexCount-1; i>=0; i--){
					float x = textureCoordinates[i*2];
					float y = textureCoordinates[i*2+1];
					target_textureCoordinates[c*2] = x;
					target_textureCoordinates[c*2+1] = y;
					c++;
				}
				targetGeometry.setTextureCoordinates(0, target_textureCoordinates);
			}
			invertedGeometry = targetGeometry;
		}
		
		else if(geometry instanceof TriangleArray){
			TriangleArray triangleArray = (TriangleArray)geometry;

			TriangleArray targetGeometry = new TriangleArray(geometryVertexCount, vertexFormat);

			float[] coordinates = new float[geometryVertexCount*3];
			float[] target_coordinates = new float[geometryVertexCount*3];
			triangleArray.getCoordinates(0, coordinates);
			
			int c=0;
			for(int i=geometryVertexCount-1; i>=0; i--){
				float x = coordinates[i*3];
				float y = coordinates[i*3+1];
				float z = coordinates[i*3+2];
				target_coordinates[c*3] = x;
				target_coordinates[c*3+1] = y;
				target_coordinates[c*3+2] = z;
				c++;
			}
			targetGeometry.setCoordinates(0, target_coordinates);
			
			

			float[] normals = new float[geometryVertexCount*3];
			float[] target_normals = new float[geometryVertexCount*3];
			triangleArray.getNormals(0, normals);
			c=0;
			for(int i=geometryVertexCount-1; i>=0; i--){
				float x = normals[i*3];
				float y = normals[i*3+1];
				float z = normals[i*3+2];
				target_normals[c*3] = -x;
				target_normals[c*3+1] = -y;
				target_normals[c*3+2] = -z;
				c++;
			}
			targetGeometry.setNormals(0, target_normals);
			
			

			if(isTextured){
				float[] textureCoordinates = new float[geometryVertexCount*2];
				float[] target_textureCoordinates = new float[geometryVertexCount*2];
				triangleArray.getTextureCoordinates(0, textureCoordinates);
				c=0;
				for(int i=geometryVertexCount-1; i>=0; i--){
					float x = textureCoordinates[i*2];
					float y = textureCoordinates[i*2+1];
					target_textureCoordinates[c*2] = x;
					target_textureCoordinates[c*2+1] = y;
					c++;
				}
				targetGeometry.setTextureCoordinates(0, target_textureCoordinates);
			}
			invertedGeometry = targetGeometry;
		}



		return invertedGeometry;
	}
	*/
}
