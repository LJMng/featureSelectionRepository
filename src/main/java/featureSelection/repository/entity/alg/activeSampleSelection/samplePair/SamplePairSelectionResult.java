package featureSelection.repository.entity.alg.activeSampleSelection.samplePair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import lombok.Data;

/**
 * An entity for sample pair selection result wrapped in {@link Map}:
 * <ul>
 * 	<li><strong>Sample Pairs selected</strong> <i>as keys</i>
 * 		<p>Selected sample pairs.
 * 	</li>
 * 	<li><strong>Correspondent attributes</strong> <i>as values</i>
 * 		<p>Correspondent attribute collections of the selected sample pairs.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Data
@ReturnWrapper
public class SamplePairSelectionResult {
	private Map<SamplePair, Collection<Integer>> samplePairSelectionMap;
	
	public SamplePairSelectionResult(Collection<SamplePair> samplePairsSelected,
									Collection<Collection<Integer>> selectionAttributes
	) {
		setSamplePairSelectionMap(samplePairsSelected, selectionAttributes);
	}
	public SamplePairSelectionResult(Map<SamplePair, Collection<Integer>> samplePairSelectionMap) {
		this.samplePairSelectionMap = samplePairSelectionMap;
	}

	private void setSamplePairSelectionMap(Collection<SamplePair> samplePairsSelected,
											Collection<Collection<Integer>> selectionAttributes
	) {
		samplePairSelectionMap = new HashMap<>(samplePairsSelected.size());
		
		Iterator<SamplePair> samplePairIterator = samplePairsSelected.iterator();
		Iterator<Collection<Integer>> selectionAttributesIterator = selectionAttributes.iterator();
		
		while (samplePairIterator.hasNext()) {
			samplePairSelectionMap.put(samplePairIterator.next(), selectionAttributesIterator.next());
		}
	}

	public Collection<SamplePair> getSelectedSamplePairs(){
		return samplePairSelectionMap.keySet();
	}
	
	public Collection<Collection<Integer>> getSelectionAttributes(){
		return samplePairSelectionMap.values();
	}

	/**
	 * Transform Sample Pair Selection result into Minimal Elements {@link Map} form with attributes(
	 * <strong>minimal elements</strong>) as <i>keys</i> and the correspondent <strong>{@link SamplePair}s
	 * </strong> as <i>values</i>.
	 * 
	 * @return Sample Pair selection result in {@link Map}.
	 */
	public Map<IntArrayKey, Collection<SamplePair>> getSamplePairFamilyMap(){
		Map<IntArrayKey, Collection<SamplePair>> selectionMap = new HashMap<>();

		Collection<SamplePair> samplePairs = getSelectedSamplePairs();
		Collection<Collection<Integer>> selectionAttributes = getSelectionAttributes();
		Iterator<SamplePair> samplePairIterator = samplePairs.iterator();
		Iterator<Collection<Integer>> selectionAttributeIterator = selectionAttributes.iterator();

		SamplePair samplePair;
		IntArrayKey selectionAttributeKey;
		Collection<Integer> selectionAttribute;
		while (samplePairIterator.hasNext()) {
			samplePair = samplePairIterator.next();
			selectionAttribute = selectionAttributeIterator.next();
			
			selectionAttributeKey = new IntArrayKey(selectionAttribute.stream().sorted().mapToInt(v->v).toArray());
			Collection<SamplePair> sp = selectionMap.get(selectionAttributeKey);
			if (sp==null)	selectionMap.put(selectionAttributeKey, sp=new LinkedList<>());
			sp.add(samplePair);
		}
		
		return selectionMap;
	}
}