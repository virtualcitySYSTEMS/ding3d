package javax.media.ding3d;

public class BatchedQuadArray extends QuadArray{

	short[] batchIds;

	public BatchedQuadArray(int vertexCount, int vertexFormat) {
		super(vertexCount, vertexFormat);
	}

	public short[] getBatchIds() {
		return batchIds;
	}

	public void setBatchIds(short[] batchIds) {
		this.batchIds = batchIds;
	}


}
