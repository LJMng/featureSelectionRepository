package featureSelection.repository.entity.opt.artificialFishSwarm.impl.position;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import common.utils.ArrayCollectionUtils;
import common.utils.ArrayUtils;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import lombok.Getter;

/**
 * An artificial fish position proposed by Yumin Chen, learn more in his article 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0950705115000337">
 * "Finding rough set reducts with fish swarm algorithm"</a>, Knowledge-Based Systems 81 (2015)
 * 22–29.
 * <p>
 * "We represent the fish’s position by a binary bit string of length N, where N is the total
 * number of features. Every bit represents a feature, and in particular, the value ‘1’ means
 * the corresponding feature is selected while ‘0’ not selected. Each position is a feature
 * subset."
 * <p>
 * hashCode():	<code>Arrays.hashCode(position)</code>
 * <p>
 * equals():	<code>Arrays.equals(position, ((ByteArrayPosition) obj).position())</code>
 * 
 * @author Benjamin_L
 */
public class ByteArrayPosition implements Position<byte[]> {
	@Getter private byte[] position;
	private int[] attributes;
	
	public ByteArrayPosition(byte[] position) {
		setPosition(position);
	}
	public ByteArrayPosition(int[] attributes, int positionLength){
		this.attributes = attributes;
		position = new byte[positionLength];	for (int attr : attributes)	position[attr-1] = 1;
	}

	@Override
	public void setPosition(byte[] p) {
		this.position = p;
		attributes = null;
	}

	@Override
	public int[] getAttributes() {
		if (attributes==null) {
			List<Integer> attrSet = new LinkedList<>();
			for (int i=0; i<position.length; i++) {
				if (position[i]==(byte) 1)		attrSet.add(i+1);
			}
			attributes = ArrayCollectionUtils.getIntArrayByCollection(attrSet);
		}
		return attributes;
	}
	
	@Override
	public boolean containsAttribute(int attribute) {
		return position[attribute-1]==(byte) 1;
	}
	
	@Override
	public Position<byte[]> addAttributeInPosition(int attribute) {
		attributes = null;
		position[attribute-1] = (byte) 1;
		return this;
	}
	@Override
	public Position<byte[]> removeAttributeInPosition(int attribute) {
		attributes = null;
		position[attribute-1] = (byte) 0;
		return this;
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
		return position.length;
	}
	
	@Override
	public Class<byte[]> encodedTypeClass() {
		return byte[].class;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(position);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ByteArrayPosition && 
				position.length==((ByteArrayPosition) obj).getPosition().length?
					Arrays.equals(position, ((ByteArrayPosition) obj).getPosition()) : 
					false;
	}
	
	@Override
	public ByteArrayPosition clone() {
		byte[] code = Arrays.copyOf(position, position.length);
		return new ByteArrayPosition(code);
	}
	
	@Override
	public String toString() {
		return "ByteArrayPosition [attributes=" + (attributes==null?null: ArrayUtils.intArrayToString(
				attributes, 10)) + ", position="+(position==null?null:ArrayUtils.byteArrayToString(
				position, 10))  + "]";
	}
}