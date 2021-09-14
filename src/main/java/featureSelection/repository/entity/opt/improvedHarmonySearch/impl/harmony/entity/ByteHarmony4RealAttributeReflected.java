package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.entity;

import java.util.Arrays;

import featureSelection.basic.model.optimization.AttributeEncoding;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import lombok.Data;

/**
 * A position for attribute value reflected by particle position coding. (attributes in this
 * class are real values instead of attribute indexes like {@link BinaryHarmony#getAttributes()},
 * etc.)
 * 
 * @author Benjamin_L
 */
@Data
public class ByteHarmony4RealAttributeReflected implements AttributeEncoding<byte[]> {
	private int[] attributes;
	private byte[] encodedValues;
	
	public ByteHarmony4RealAttributeReflected(Harmony<byte[]> harmony, int[] attributeSource) {
		attributes = new int[harmony.getAttributes().length];
		for (int i=0; i<attributes.length; i++)	attributes[i] = attributeSource[harmony.getAttributes()[i]];
		encodedValues = harmony.encodedValues();
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