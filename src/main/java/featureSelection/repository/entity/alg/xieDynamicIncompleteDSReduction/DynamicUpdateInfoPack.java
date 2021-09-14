package featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.attributeRelatedUpdate.AlteredAttributeValues;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.objectRelatedUpdate.AlteredInstanceItem;
import lombok.Data;

/**
 * An package entity to contain info. of updates for dynamic(and previous info.: {@link #previousInfo}),
 * please make sure {@link #alteredObjects} and {@link #alteredAttributeValues} are <strong>identical
 * </strong> to each other if it is the case of {@link DynamicUpdateStrategy#BOTH_RELATED}.
 * 
 * @author Benjamin_L
 */
@Data
public class DynamicUpdateInfoPack {
	private PreviousInfoPack previousInfo;
	private DynamicUpdateStrategy updateStrategy;
	private Map<Instance, Map<Integer, Integer>> alteredSettings;
	
	private Map<Instance, AlteredInstanceItem> alteredObjects;
	private AlteredAttributeValues alteredAttributeValues;
	
	private List<Instance> alteredAttrValAppliedInstances;
	private List<Instance> alteredObjectAppliedInstances;
	private List<Instance> alteredMixAppliedInstances;
	
	private Map<Instance, Instance> previousNLatestInstanceMap;
	
	public DynamicUpdateInfoPack(DynamicUpdateStrategy updateStrategy, PreviousInfoPack previousInfo) {
		this.updateStrategy = updateStrategy;
		this.previousInfo = previousInfo;
		this.alteredSettings = new HashMap<>();
		this.previousNLatestInstanceMap = new HashMap<>();
	}

	/* -------------------------------------------------------------------------------------------------- */
	
	public void alter(Instance instance, int attribute, int newValue) {
		Map<Integer, Integer> newAttributeValues = alteredSettings.get(instance);
		if (newAttributeValues==null)
			alteredSettings.put(instance, newAttributeValues=new HashMap<>(instance.getAttributeValues().length-1));
		newAttributeValues.put(attribute, newValue);
	}

	/**
	 * Collect all altered attributes.
	 * 
	 * @return Attributes whose values had been altered.(starts from 1)
	 */
	public Collection<Integer> countAlteredAttributes(){
		return alteredSettings.values()
							.stream()
							.flatMap(map->map.keySet().stream())
							.collect(Collectors.toSet());
	}
	
	/**
	 * Get if any attribute has been altered in {@link Instance}s of
	 * {@link PreviousInfoPack#getInstances()}.
	 * <p>
	 * (From the prospect of attribute)
	 * 
	 * @param attribute
	 * 		The attribute to check.
	 * @return true if the given attribute's values have been altered.
	 */
	public boolean hasAnyAlter(int attribute) {
		return alteredSettings.values().stream().anyMatch(map->map.containsKey(attribute));
	}
	
	/**
	 * Collect all altered {@link Instance}.
	 * 
	 * @return {@link Instance} whose values had been altered.
	 */
	public Collection<Instance> countAlteredInstances(){
		return Collections.unmodifiableCollection(alteredSettings.keySet());
	}
	
	/**
	 * Get if any attribute value has been altered in the given {@link Instance} in
	 * {@link PreviousInfoPack#getInstances()}.
	 * <p>
	 * (From the prospect of instance)
	 * 
	 * @param ins
	 * 		The {@link Instance} to check.
	 * @return true if the given {@link Instance}'s attribute values have been altered.
	 */
	public boolean hasAnyAlter(Instance ins) {
		return alteredSettings.containsKey(ins);
	}
	
	/* -------------------------------------------------------------------------------------------------- */
	
	// Attribute-related
	
	public void setSelectednUnalteredAttribute4AlteredAttributeValues(Collection<Integer> selectedUnalteredAttributes) {
		if (DynamicUpdateStrategy.ATTRIBUTE_RELATED.equals(updateStrategy)) {
			getAlteredAttributeValues(); // initiate alteredAttributeValues first.
			alteredAttributeValues.setSelectedUnalteredAttributes(selectedUnalteredAttributes);
		}else {
			throw new IllegalStateException("Can not call this method with strategy: "+updateStrategy.name());
		}
	}

	public AlteredAttributeValues getAlteredAttributeValues() {
		if (DynamicUpdateStrategy.ATTRIBUTE_RELATED.equals(updateStrategy)) {
			if (alteredAttributeValues==null) {
				Collection<Integer> alteredAttrs = null;
				for (Map<Integer, Integer> newAttrValues: alteredSettings.values()) {
					if (alteredAttrs==null) {
						alteredAttrs = new HashSet<>(newAttrValues.keySet());
					}else {
						if (alteredAttrs.size()!=newAttrValues.size() ||
							!alteredAttrs.containsAll(newAttrValues.keySet())
						) {
							alteredAttrs.addAll(newAttrValues.keySet());
						}
					}
				}
				if (alteredAttrs==null || alteredAttrs.isEmpty()) {
					alteredAttributeValues = null;
				}else {
					alteredAttributeValues = new AlteredAttributeValues(alteredAttrs.size());
					for (int alteredAttr: alteredAttrs) {
						Map<Instance, Integer> alteredValues =
							previousInfo.getInstances().stream()
										.filter(ins->alteredSettings.containsKey(ins) && alteredSettings.get(ins).containsKey(alteredAttr))
										.collect(Collectors.toMap(
											ins->ins, 
											ins->alteredSettings.get(ins).get(alteredAttr)
										));
						
						alteredAttributeValues.alterAttributeValue(alteredAttr, alteredValues);
					}
				}
			}
			
			return alteredAttributeValues;
		}else {
			return null;
		}
	}
	
	/**
	 * Get the {@link Instance}s with {@link #alteredAttributeValues} applied.
	 * <p>
	 * <strong>Notice:</strong>
	 * <ul>
	 * 	<li>Lazy copying {@link PreviousInfoPack#getInstances()} for {@link #alteredAttrValAppliedInstances}</li>
	 * </ul>
	 * 
	 * @see #alteredAttributeValues
	 * @see AlteredAttributeValues#applyAlterAttributeValues(AlteredAttributeValues, List)
	 * 
	 * @return {@link #alteredAttrValAppliedInstances}.
	 */
	public List<Instance> getAlteredAttrValAppliedInstances(){
		if (DynamicUpdateStrategy.ATTRIBUTE_RELATED.equals(updateStrategy)) {
			// Apply attribute value altering.
			if (this.alteredAttrValAppliedInstances==null && alteredAttributeValues!=null) {
				List<Instance> copyIns = new ArrayList<>(previousInfo.getInstances().size());
				for (Instance ins: previousInfo.getInstances()) {
					Instance deepCopy = deepCopyOfOriginalInstance(ins);
					previousNLatestInstanceMap.put(ins, deepCopy);
					copyIns.add(deepCopy);
				}
				
				AlteredAttributeValues.applyAlterAttributeValues(alteredAttributeValues, copyIns);
				this.alteredAttrValAppliedInstances = copyIns;
			}

			return this.alteredAttrValAppliedInstances;
		}else {
			return null;
		}
		
	}

	/* -------------------------------------------------------------------------------------------------- */

	// Object-related
	
	public Map<Instance, AlteredInstanceItem> getAlteredObjects(){
		if (DynamicUpdateStrategy.OBJECT_RELATED.equals(this.updateStrategy)) {
			if (alteredObjects==null) {
				alteredObjects = new HashMap<>();
				for (Instance ins: previousInfo.getInstances()) {
					Map<Integer, Integer> alteredSetting = alteredSettings.get(ins);
					if (alteredSetting==null || alteredSetting.isEmpty())	continue;
					
					for (Map.Entry<Integer, Integer> newAttrValue: alteredSetting.entrySet()) {
						AlteredInstanceItem alterItem = new AlteredInstanceItem(ins);
						alteredObjects.put(ins, alterItem);
						alterItem.alterAttributeValue(newAttrValue.getKey(), newAttrValue.getValue());
					}
				}
			}
			return alteredObjects;
		}else {
			return null;
		}
	}
	
	/**
	 * Get the {@link Instance}s with {@link #alteredObjects} applied.
	 * <p>
	 * <strong>Notice:</strong>
	 * <ul>
	 * 	<li>Lazy copying {@link PreviousInfoPack#getInstances()} for
	 * 	    {@link #alteredObjectAppliedInstances}</li>
	 * </ul>
	 * 
	 * @see #alteredObjects
	 * 
	 * @return {@link #alteredObjectAppliedInstances}.
	 */
	public List<Instance> getAlteredObjectAppliedInstances(){
		if (DynamicUpdateStrategy.OBJECT_RELATED.equals(this.updateStrategy)) {
			getAlteredObjects(); // initiate if needed.
			if (alteredObjectAppliedInstances==null) {
				if (alteredObjects==null)	return null;
				
				List<Instance> appliedInstances = new ArrayList<>(previousInfo.getInstances().size());
				
				for (Instance oriIns: previousInfo.getInstances()) {
					AlteredInstanceItem alter = alteredObjects.get(oriIns);
					// if need to update the instance.
					if (alter!=null) {
						appliedInstances.add(alter.getAlteredInstance());
						previousNLatestInstanceMap.put(oriIns, alter.getAlteredInstance());
					// else return deep copy.
					}else {
						appliedInstances.add(deepCopyOfOriginalInstance(oriIns));
					}
				}
				
				this.alteredObjectAppliedInstances = appliedInstances;
			}
			return this.alteredObjectAppliedInstances;
		}else {
			return null;
		}
	}

	/* -------------------------------------------------------------------------------------------------- */

	// Both-related
	
	/**
	 * Get the {@link Instance}s with {@link #alteredAttributeValues} and {@link #alteredObjects}
	 * applied.
	 * <p>
	 * <strong>Notice:</strong>
	 * <ul>
	 * 	<li>Lazy copying {@link PreviousInfoPack#getInstances()} for {@link #alteredMixAppliedInstances}</li>
	 * </ul>
	 * 
	 * {@link #alteredMixAppliedInstances}
	 * 
	 * @return {@link #alteredMixAppliedInstances}.
	 */
	public List<Instance> getAlteredMixAppliedInstances(){
		if (DynamicUpdateStrategy.BOTH_RELATED.equals(this.updateStrategy)) {
			if (alteredMixAppliedInstances==null) {
				List<Instance> result = new ArrayList<>(previousInfo.getInstances().size());
				for (Instance ins: previousInfo.getInstances()) {
					Instance alteredIns = deepCopyOfOriginalInstance(ins);

					Map<Integer, Integer> alterSetting = alteredSettings.get(ins);
					if (alterSetting!=null && !alterSetting.isEmpty()) {
						for (Map.Entry<Integer, Integer> newAttrValue: alterSetting.entrySet())
							alteredIns.setAttributeValueOf(newAttrValue.getKey(), newAttrValue.getValue());
					}
					
					result.add(alteredIns);
					previousNLatestInstanceMap.put(ins, alteredIns);
				}
				alteredMixAppliedInstances = result;
			}
			return alteredMixAppliedInstances;
		}else {
			return null;
		}
	}
	
	/* -------------------------------------------------------------------------------------------------- */
	
	public Collection<Instance> getLatestInstances(){
		switch(updateStrategy) {
			case OBJECT_RELATED:
				return getAlteredObjectAppliedInstances();
			case ATTRIBUTE_RELATED:
				return getAlteredAttrValAppliedInstances();
			case BOTH_RELATED:
				return getAlteredMixAppliedInstances();
			default:
				return null;
		}
	}//*/

	public Instance getLatestInstanceOf(Instance previous) {
		return previousNLatestInstanceMap.getOrDefault(previous, previous);
	}
	
	/* -------------------------------------------------------------------------------------------------- */
	
	private Instance deepCopyOfOriginalInstance(Instance ins) {
		return ins instanceof IncompleteInstance ?
				new IncompleteInstance(
					Arrays.copyOf(ins.getAttributeValues(), ins.getAttributeValues().length),
					ins.getNum()
				):
				new Instance(
					Arrays.copyOf(ins.getAttributeValues(), ins.getAttributeValues().length),
					ins.getNum()
				);
	}

}