package featureSelection.repository.entity.alg.activeSampleSelection.samplePair.family;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePairSelectionResult;
import lombok.Data;

/**
 * An entity for Sample Pair family with ME, KP info.
 * <p>
 * This entity is built based on the article:
 * <a href="https://ieeexplore.ieee.org/document/7492272">"Active Sample Selection Based Incremental
 * Algorithm for Attribute Reduction With Rough Sets"</a> by Yanyan Yang, Degang Chen, Hui Wang
 * &
 * <a href="https://ieeexplore.ieee.org/document/6308684/">"Sample Pair Selection for Attribute
 * Reduction with Rough Set"</a> by Degang Chen, Suyun Zhao, Lei Zhang, Yongping Yang, Xiao Zhang.
 * <p>
 * For <strong>ME</strong>, it is a collection of attributes corresponding to the sample pairs in KP.
 * In the form of ME = { c<sub>1</sub>={a<sub>1</sub>}, c<sub>2</sub>={a<sub>2</sub>, a<sub>3</sub>}, ... }.
 * <p>
 * For <strong>KP</strong>, it is a collection of sample pair collection, in the form of KP = { 
 * p<sub>1</sub>={(x<sub>1</sub>, x<sub>2</sub>)},  p<sub>2</sub>={(x<sub>1</sub>, x<sub>3</sub>), 
 * (x<sub>1</sub>, x<sub>4</sub>)}, etc...}.
 * 
 * @author Benjamin_L
 */
@Data
public class SamplePairFamily {
	/**
	 * A {@link Map} for ME-KP with ME as keys and KP as values.
	 */
	private Map<IntArrayKey, Collection<SamplePair>> samplePairFamilyMap;

	public SamplePairFamily(Map<IntArrayKey, Collection<SamplePair>> samplePairFamilyMap) {
		this.samplePairFamilyMap = samplePairFamilyMap;
	}
	
	/**
	 * Transfer {@link SamplePairSelectionResult} into a structure which is grouped by attributes:
	 * <pre>{ a<sub>1</sub>: [sample pair <sub>1</sub>, ...], ... }</pre>
	 * 
	 * @param selectionResult
	 * 		A {@link SamplePairSelectionResult} instance.
	 * @return {@link SamplePair} Collection grouped by attributes in a Map.
	 */
	public static Map<IntArrayKey, Collection<SamplePair>> collectNCombineIdenticalMinimalElements(
			SamplePairSelectionResult selectionResult
	){
		Map<IntArrayKey, Collection<SamplePair>> selectionMap = new HashMap<>(selectionResult.getSelectedSamplePairs().size());

		Collection<SamplePair> samplePairs = selectionResult.getSelectedSamplePairs();
		Collection<Collection<Integer>> selectionAttributes = selectionResult.getSelectionAttributes();
		Iterator<SamplePair> samplePairIterator = samplePairs.iterator();
		Iterator<Collection<Integer>> selectionAttributeIterator = selectionAttributes.iterator();

		SamplePair samplePair;
		IntArrayKey selectionAttributeKey;
		Collection<Integer> selectionAttribute;
		while (samplePairIterator.hasNext()) {
			samplePair = samplePairIterator.next();
			selectionAttribute = selectionAttributeIterator.next();
			
			selectionAttributeKey = new IntArrayKey(selectionAttribute.stream().mapToInt(v->v).toArray());
			Collection<SamplePair> sp = selectionMap.get(selectionAttributeKey);
			if (sp==null)	selectionMap.put(selectionAttributeKey, sp=new LinkedList<>());
			sp.add(samplePair);
		}
		
		return selectionMap;
	}

	public Collection<IntArrayKey> getMinimalElements() {
		return samplePairFamilyMap.keySet();
	}
	
	public Collection<Collection<SamplePair>> getCorrespondentSamplePairs(){
		return samplePairFamilyMap.values();
	}

	public Map<IntArrayKey, Collection<SamplePair>> copySamplePairFamilyMap(){
		final Map<IntArrayKey, Collection<SamplePair>> copy = new HashMap<>(samplePairFamilyMap.size());
		samplePairFamilyMap.entrySet().stream().forEach(entry->{
			copy.put(entry.getKey(), new LinkedList<>(entry.getValue()));
		});
		return copy;
	}
}