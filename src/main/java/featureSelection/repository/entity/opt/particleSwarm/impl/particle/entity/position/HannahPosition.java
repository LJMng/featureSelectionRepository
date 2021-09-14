package featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Implementation of {@link Position} based on the paper
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0169260713003477">
 * "Supervised hybrid feature selection based on PSO and rough sets for medical diagnosis"</a> 
 * by H.Hannah.
 * <p>
 * <strong>Notice: </strong> {@link #attributeIndexes} in <code>this</code> are only indexes(
 * starts from 0) for the {@link Position} byte values. To use it properly for a real attribute
 * array of {@link Instance}, assuming <code>attrIndex</code> is a value in
 * {@link #attributeIndexes}, <code>array</code> is the real attribute array, please use
 * <code>array[attrIndex]</code> to get the attribute value.
 * 
 * @author Benjamin_L
 */
public class HannahPosition implements Position<byte[]> {
	private int[] attributeIndexes;
	private byte[] position;
	/**
	 * Construct instance without any action.
	 */
	public HannahPosition() {}
	/**
	 * Construct instance by setting {@link #position}.
	 * 
	 * @param position
	 * 		A {@link byte[]} encoded value as the position.
	 */
	public HannahPosition(byte[] position) {
		this.position = position;
	}
	/**
	 * Construct instance by setting {@link #attributeIndexes} and {@link #position}.
	 * 
	 * @param attributeIndexes
	 * 		Attribute indexes of attribute values of {@link Instance}.
	 * @param positionSize
	 * 		The length of full Position encoded value(i.e. The length of attributes of {@link Instance}).
	 */
	public HannahPosition(int[] attributeIndexes, int positionSize) {
		this.attributeIndexes = attributeIndexes;
		position = new byte[positionSize];	for (int i: attributeIndexes)	position[i] = 1;
	}

	/**
	 * <strong>Notice: </strong>The return value is only indexes(starts from 0) for the
	 * {@link Position} byte values. To use it properply for a real attribute array of
	 * {@link Instance}, assuming <code>attrIndex</code> is a value in
	 * {@link #attributeIndexes}, <code>array</code> is the real attribute array, please
	 * use <code>array[attrIndex]</code> to get the correct attribute value.
	 * 
	 * @return {@link int[]}.
	 */
	@Override
	public int[] getAttributes() {
		if (attributeIndexes==null) {
			Collection<Integer> set = new LinkedList<>();
			for (int i=0; i<position.length; i++)	if (position[i]==(byte)1)	set.add(i);
			attributeIndexes = ArrayCollectionUtils.getIntArrayByCollection(set);
		}
		return attributeIndexes;
	}

	/**
	 * Set {@link #attributeIndexes} and update {@link #position} based on the given attribute
	 * indexes.
	 */
	@Override
	public HannahPosition setAttributes(int[] attributeIndexes) {
		this.attributeIndexes = attributeIndexes;
		position = new byte[position.length];	for (int i: attributeIndexes)	position[i] = 1;
		return this;
	}

	/**
	 * Refresh {@link #attributeIndexes} by setting <code>null</code>. When calling
	 * {@link #getAttributes()}, {@link #attributeIndexes} will be reset based on
	 * {@link Position},
	 */
	public void refreshAttributes() {
		attributeIndexes = null;
	}
	
	@Override
	public byte[] encodedValues() {
		return position;
	}
	
	@Override
	public String encodedValuesToString() {
		return Arrays.toString(position);
	}
	
	@Override
	public int encodedValuesLength() {
		return position==null?0:position.length;
	}
	
	@Override
	public Class<byte[]> encodedTypeClass() {
		return byte[].class;
	}

	/**
	 * Set {@link #position} and {@link #refreshAttributes()}.
	 */
	@Override
	public HannahPosition setEncodedValues(byte[] position) {
		// Set position.
		this.position = position;
		refreshAttributes();
		return this;
	}
	
	@Override
	public HannahPosition clone() {
		int[] attributeIndexes = getAttributes();
		return new HannahPosition(
				Arrays.copyOf(attributeIndexes, attributeIndexes.length), 
				position.length
			);
	}
	
	@Override
	public String toString() {
		return "HannahPosition [position=" + Arrays.toString(position) + ", attributeIndexes=" + 
				Arrays.toString(attributeIndexes) + "]";
	}
}
