package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.entity;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * An implementation of {@link Harmony}, using binary bits as values (0 or 1), Based on the
 * paper <a href="https://link.springer.com/article/10.1007/s00521-015-1840-0">
 * "A novel hybrid feature selection method based on rough set and improved harmony search"</a> 
 * by H.Hannah.
 * <p>
 * <strong>Notice: </strong> {@link #attributeIndexes} in <code>this</code> are only indexes(
 * starts from 0) for the {@link Harmony} byte values. To use it properly for a real attribute
 * array of {@link Instance}, assuming <code>attrIndex</code> is a value in
 * {@link #attributeIndexes}, <code>array</code> is the real attribute array, please use
 * <code>array[attrIndex]</code> to get the correct attribute value.
 * 
 * @author Benjamin_L
 */
public class BinaryHarmony implements Harmony<byte[]> {
	private byte[] binaries;
	private int[] attributeIndexes;
	
	public BinaryHarmony(byte[] binaries) {
		this.binaries = binaries;
	}
	public BinaryHarmony(int[] attributeIndexes, int harmonyLength) {
		binaries = new byte[harmonyLength];
		for (int i: attributeIndexes)	binaries[i] = 1;
	}
	
	public byte[] encodedValues() {
		return binaries;
	}
	
	@Override
	public String encodedValuesToString() {
		return Arrays.toString(binaries);
	}
	
	public BinaryHarmony setEncodedValues(byte[] binaries) {
		this.binaries = binaries;
		return this;
	}
	
	/**
	 * <strong>Notice: </strong>The return value is only indexes(starts from 0) for the
	 * {@link Position} byte values. To use it properply for a real attribute array of
	 * {@link Instance}, assuming <code>attrIndex</code> is a value in {@link #attributeIndexes},
	 * <code>array</code> is the real attribute array, please use <code>array[attrIndex]</code>
	 * to get the correct attribute value.
	 * 
	 * @return {@link int[]}.
	 */
	public int[] getAttributes() {
		if (attributeIndexes==null) {
			Collection<Integer> binaryList = new LinkedList<>();
			for (int i=0; i<binaries.length; i++)	if (binaries[i] == (byte) 1) binaryList.add(i);
			attributeIndexes = ArrayCollectionUtils.getIntArrayByCollection(binaryList);
		}
		return attributeIndexes;
	}

	
	@Override
	public boolean addAttribute(int attributeIndex) {
		if (binaries[attributeIndex]!= (byte) 1) {
			binaries[attributeIndex] = (byte) 1;
			attributeIndexes = null;
		}
		return true;
	}

	@Override
	public boolean removeAttribute(int attributeIndex) {
		if (binaries[attributeIndex]!= (byte) 0) {
			binaries[attributeIndex] = (byte) 0;
			attributeIndexes = null;
		}
		return true;
	}


	@Override
	public boolean containsAttribute(int attributeIndex) {
		return Byte.compare(binaries[attributeIndex], (byte) 1)==0;
	}

	
	@Override
	public int encodedValuesLength() {
		return binaries.length;
	}

	@Override
	public Class<byte[]> encodedTypeClass() {
		return byte[].class;
	}
	
	
	public BinaryHarmony clone() {
		return new BinaryHarmony(Arrays.copyOf(binaries, binaries.length));
	}
	
	@Override
	public String toString() {
		return "BinaryHarmony [attributes=" + Arrays.toString(attributeIndexes) + ", binaries=" + Arrays.toString(binaries)+ "]";
	}

}