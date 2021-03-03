package javax.media.ding3d.utils.textureatlas;


import javax.media.ding3d.vecmath.Point3d;


public class LoaderOptions {
	

	
	

	private float targetTexelSize = 0.01f;
	private float imageCompressionQuality = 0.85f;
	private int maxImageWidth;
	private int maxImageHeight;
	private Point3d origin;
	private boolean separateMaterials = false;
	private boolean computeNormals = false;
	private boolean batched = false;
	
	



	
	public boolean isBatched() {
		return batched;
	}

	public void setBatched(boolean batched) {
		this.batched = batched;
	}


	public boolean isComputeNormals() {
		return computeNormals;
	}


	public void setComputeNormals(boolean computeNormals) {
		this.computeNormals = computeNormals;
	}


	public boolean isSeparateMaterials() {
		return separateMaterials;
	}


	public void setSeparateMaterials(boolean separateMaterials) {
		this.separateMaterials = separateMaterials;
	}






	public Point3d getOrigin() {
		return origin;
	}


	public void setOrigin(Point3d origin) {
		this.origin = origin;
	}


	public int getAtlasMaxWidth() {
		return maxImageWidth;
	}


	public void setAtlasMaxWidth(int maxImageWidth) {
		this.maxImageWidth = maxImageWidth;
	}


	public int getMaxImageHeight() {
		return maxImageHeight;
	}


	public void setMaxImageHeight(int maxImageHeight) {
		this.maxImageHeight = maxImageHeight;
	}




	public float getImageCompressionQuality() {
		return imageCompressionQuality;
	}


	public void setImageCompressionQuality(float imageCompressionQuality) {
		this.imageCompressionQuality = imageCompressionQuality;
		if(this.imageCompressionQuality < 0f ) this.imageCompressionQuality = 0f;
		if(this.imageCompressionQuality > 1f ) this.imageCompressionQuality = 1f;
	}


	public float getTargetTexelSize() {
		return targetTexelSize;
	}


	public void setTargetTexelSize(float targetTexelSize) {
		this.targetTexelSize = targetTexelSize;
	}




	
}
