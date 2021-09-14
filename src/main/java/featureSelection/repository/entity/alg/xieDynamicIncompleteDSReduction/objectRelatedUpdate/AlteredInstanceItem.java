package featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.objectRelatedUpdate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import lombok.Data;

@Data
public class AlteredInstanceItem {
	private Instance oldInstance;
	private Map<Integer, Integer> alterAttributeValues;
	private Instance alteredInstance;
	
	public AlteredInstanceItem(Instance oldInstance) {
		this(oldInstance, 16);
	}
	public AlteredInstanceItem(Instance oldInstance, int alterAttributeNumber) {
		this.oldInstance = oldInstance;
		alterAttributeValues = new HashMap<>(alterAttributeNumber);
	}
	
	/**
	 * Alter an attribute of {@link #oldInstance}.
	 * 
	 * @param attribute
	 * 		The attribute to be altered. (starts from 1)
	 * @param value
	 * 		The value to alter into.
	 */
	public void alterAttributeValue(int attribute, int value) {
		alterAttributeValues.put(attribute, value);
	}
	
	public boolean attributeValueIsAltered(int attribute) {
		return alterAttributeValues.containsKey(attribute);
	}
	
	public Instance getAlteredInstance() {
		if (alteredInstance==null) {
			boolean missingValue = oldInstance instanceof IncompleteInstance;
			int[] values = Arrays.copyOf(oldInstance.getAttributeValues(), oldInstance.getAttributeValues().length);
			for (Map.Entry<Integer, Integer> entry: alterAttributeValues.entrySet())
				values[entry.getKey()] = entry.getValue();
			if (missingValue) {
				missingValue = Arrays.stream(values)
									.anyMatch(v->v==IncompleteInstance.MISSING_VALUE);
			}
			alteredInstance = missingValue? 
								new IncompleteInstance(values, oldInstance.getNum()):
								new Instance(values, oldInstance.getNum());
		}
		return alteredInstance;
	}


}