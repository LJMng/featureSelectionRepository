package featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.attributeRelatedUpdate;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import lombok.Data;

@Data
public class AlteredAttributeValues {
	// <attribute, <instance, value>>
	private Map<Integer, Map<Instance, Integer>> alterAttributeValues;
	
	/**
	 * Unaltered attributes in <code>previous tolerance attributes</code>. According to the paper,
	 * this value(marked as <strong>P</strong> in the paper, where P⊂B-C<sub>ALT</sub>) usually 
	 * selects a single attribute, which satisfies that the number of 
	 * T<sub>P</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)is minimal.
	 */
	private Collection<Integer> selectedUnalteredAttributes;
	
	public AlteredAttributeValues(int alterAttributeNumber) {
		alterAttributeValues = new HashMap<>(alterAttributeNumber);
	}
	
	/**
	 * Alter an attribute.
	 * 
	 * @param attribute
	 * 		The attribute to be altered. (starts from 1)
	 * @param value
	 * 		The values to alter into.
	 */
	public void alterAttributeValue(int attribute, Map<Instance, Integer> value) {
		alterAttributeValues.put(attribute, value);
	}
	
	public boolean attributeValueIsAltered(int attribute) {
		return alterAttributeValues.containsKey(attribute);
	}
	
	public Map<Integer, Map<Instance, Integer>> shallowCopyAlterAttributeValues(){
		return new HashMap<>(alterAttributeValues);
	}

	public Collection<Integer> getAlteredAttributes(){
		return alterAttributeValues.keySet();
	}
	
	/**
	 * Get the altered attribute value of an {@link Instance}.
	 * 
	 * @param attribute
	 * 		The attribute. (Starts from 1)
	 * @param ins
	 * 		An {@link Instance}.
	 * @return The altered value if it is altered. / Original value if it is not altered.
	 */
	public int getAlteredAttributeValueOf(int attribute, Instance ins) {
		Map<Instance, Integer> map = alterAttributeValues.get(attribute);
		if (map==null) {
			// no altering is made.
			return ins.getAttributeValue(attribute);
		}else {
			// altered value.
			return map.getOrDefault(ins, ins.getAttributeValue(attribute));
		}
	}
	
	/**
	 * Apply {@link #alterAttributeValues} to <code>instances</code>.
	 * <p>
	 * <strong>Notice</strong>: Irreversible action.
	 */
	public static void applyAlterAttributeValues(
		AlteredAttributeValues alteredAttributeValues, List<Instance> instances
	) {
		int i=0;
		for (Instance ins: instances) {
			boolean originalMissingValue = ins instanceof IncompleteInstance;
			boolean extraMissingValue = originalMissingValue;
			
			Map<Integer, Integer> toValues = new HashMap<>(alteredAttributeValues.getAlteredAttributes().size());
			for (int alteredAttr: alteredAttributeValues.getAlteredAttributes()) {
				Integer toValue = alteredAttributeValues.getAlteredAttributeValueOf(alteredAttr, ins);
				
				if (toValue!=null) {
					toValues.put(alteredAttr, toValue);
					if (!extraMissingValue && toValue==IncompleteInstance.MISSING_VALUE)
						extraMissingValue = true;
				}
			}
			for (Map.Entry<Integer, Integer> alterInfo: toValues.entrySet()) {
				ins.setAttributeValueOf(alterInfo.getKey(), alterInfo.getValue());
			}
			
			if (!originalMissingValue && extraMissingValue) {
				instances.set(i, new IncompleteInstance(ins.getAttributeValues(), ins.getNum()));
			}			
			i++;
		}
	}//*/
}