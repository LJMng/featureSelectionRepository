package featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position;

import java.util.Arrays;

import featureSelection.basic.model.optimization.AttributeEncoding;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import lombok.Data;

/**
 * A position for attribute value reflected by particle position coding. (attributes in this
 * class are real values instead of attribute indexes)
 * 
 * @author Benjamin_L
 */
@Data
public class BytePosition4RealAttributeReflected implements AttributeEncoding<byte[]> {
	private int[] attributes;
	private byte[] encodedValues;
	
	public BytePosition4RealAttributeReflected(Position<byte[]> position, int[] attributeSource) {
		attributes = new int[position.getAttributes().length];
		for (int i=0; i<attributes.length; i++)	attributes[i] = attributeSource[position.getAttributes()[i]];
		encodedValues = position.encodedValues();
	}

	@Override
	public byte[] encodedValues() {
		return encodedValues;
	}

	@Override
	public String encodedValuesToString() {
		return Arrays.toString(encodedValues);
	}

	@Override
	public int encodedValuesLength() {
		return encodedValues.length;
	}

	@Override
	public Class<byte[]> encodedTypeClass() {
		return byte[].class;
	}
}
