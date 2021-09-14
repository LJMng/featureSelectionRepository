package featureSelection.repository.algorithm.alg.toleranceClassPositiveRegionIncremental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.CombTClassesResult4VariantObjs;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.EquivalenceClass;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.MostSignificantAttributeResult;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.ReductCandidateResult4VariantObjects;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesGroupArray;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesGroupMap;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ToleranceClassPositiveRegionIncremental;
import lombok.extern.slf4j.Slf4j;

/**
 * Algorithm repository of ToleranceClass & PositiveRegion (AttributeReduction), which bases on the
 * paper <a href="https://www.sciencedirect.com/science/article/abs/pii/S0031320314002234">
 * "Incremental feature selection based on rough set in dynamic incomplete data"</a> by Wenhao Shu, 
 * Hong Shen.
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ToleranceClassPositiveRegionAlgorithm {
	
	public static class Basic {
		
		/**
		 * Extract {@link Instance} in the given <code>Instances</code> that is not
		 * missing value at <code>attribute</code>
		 * <p>
		 * If elements in <code>Instances</code> is not instance of
		 * {@link IncompleteInstance}, they are also extracted.
		 * <p>
		 * Only if elements in <code>Instances</code> is instance of
		 * {@link IncompleteInstance} and {@link IncompleteInstance#isValueMissing(int)}
		 * returns true will be excluded from the return result.
		 * 
		 * @see IncompleteInstance#isValueMissing(int)
		 * 
		 * @param instances
		 * 		A {@link Instance} {@link Collection} for filtering.
		 * @param attribute
		 * 		The attribute of {@link Instance} to check.
		 * @return Filtered {@link Instance} {@link List}.
		 */
		public static List<Instance> extractOnesNotMissingOfAttribute(
			Collection<Instance> instances, int attribute
		){
			return instances.stream().filter(ins->
					(ins instanceof IncompleteInstance ?
						!((IncompleteInstance) ins).isValueMissing(attribute):
						true
					)
				).collect(Collectors.toList());
		}
		
		/**
		 * For every attribute in <code>attributes</code>, extract {@link Instance} in the given
		 * <code>Instances</code> that is not missing value.
		 * 
		 * @see #extractOnesNotMissing(Collection, int)
		 * 
		 * @param instances
		 * 		A {@link Instance} {@link Collection} for filtering.
		 * @param attributeSizeOfInstance
		 * 		Attributes of {@link Instance}.
		 * @return {@link Collection} array.
		 */
		public static InstancesGroupArray extractOnesNotMissing(
				Collection<Instance> instances, int attributeSizeOfInstance
		){
			InstancesGroupArray completes = new InstancesGroupArray(attributeSizeOfInstance);
			
			for (int i=0; i<attributeSizeOfInstance; i++) {
				int attr = i+1;
				// let the object set U<sub>i</sub> = { x in U | b<sub>i</sub>(x) != *};
				//	// The object set contains no missing values.
				List<Instance> fullValueIns = extractOnesNotMissingOfAttribute(instances, attr);
				if (!fullValueIns.isEmpty())
					completes.set(attr, equivalenceClass(fullValueIns, attr));
			}
			
			return completes;
		}//*/
		
		/**
		 * For every attribute in <code>attributes</code>, extract {@link Instance} in the given
		 * <code>Instances</code> that is not missing value.
		 * 
		 * @see #extractOnesNotMissingOfAttribute(Collection, int)
		 * 
		 * @param instances
		 * 		A {@link Instance} {@link Collection} for filtering.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return {@link Collection} array.
		 */
		public static InstancesGroupMap extractOnesNotMissing(
				Collection<Instance> instances, IntegerIterator attributes
		){
			final InstancesGroupMap completes = new InstancesGroupMap(attributes.size());
			
			attributes.reset();
			for (int i=0; i<attributes.size(); i++) {
				int attr = attributes.next();
				// let the object set U<sub>i</sub> = { x in U | b<sub>i</sub>(x) != *};
				//	// The object set contains no missing values.
				List<Instance> fullValueIns = extractOnesNotMissingOfAttribute(instances, attr);
				if (!fullValueIns.isEmpty())
					completes.set(attr, equivalenceClass(fullValueIns, attr));
			}
			
			return completes;
		}
	
		public static InstancesGroupArray attributeGroups(
				Collection<Instance> instances, int attributeSizeOfInstance
		) {
			final InstancesGroupArray group = new InstancesGroupArray(attributeSizeOfInstance);
			final List<Instance> instanceList =
					instances instanceof ArrayList?
							(List<Instance>) instances:
							new ArrayList<>(instances);
			
			for (int i=0; i<attributeSizeOfInstance; i++) {
				int attr = i+1;
				group.set(attr, equivalenceClass(instanceList, attr));
			}
			
			return group;
		}
		
		public static InstancesGroupMap attributeGroups(
				Collection<Instance> instances, IntegerIterator attributes
		) {
			final InstancesGroupMap group = new InstancesGroupMap(attributes.size());
			final List<Instance> instanceList =
					instances instanceof ArrayList?
							(List<Instance>) instances:
							new ArrayList<>(instances);
			
			attributes.reset();
			for (int i=0; i<attributes.size(); i++) {
				int attr = attributes.next();
				group.set(attr, equivalenceClass(instanceList, attr));
			}
			
			return group;
		}
		
		/**
		 * Get the equivalent classes partitioned by the given <code>attribute</code>.
		 * <p>
		 * <code>Instances</code> is sorted first and then collected.
		 * 
		 * @see Collections#sort(List, java.util.Comparator)
		 * 
		 * @param Instances
		 * 		A {@link List} of {@link Instance} to be partitioned.
		 * @param attribute
		 * 		An attribute of {@link Instance} to be used to partition.(starts from 1)
		 * @return A {@link Collection} of {@link Instance} {@link Collection}.
		 */
		public static Collection<EquivalenceClass> equivalenceClass(
			List<Instance> Instances, int attribute
		){
			// sort the objects in U<sub>i</su> by feature values;
			Collections.sort(
				Instances,
				(ins1, ins2)->(ins1.getAttributeValue(attribute) - ins2.getAttributeValue(attribute))
			);
			// let U<sub>i</sub> = {x'[1], x'[2], ..., x'[n]}, z=1, s=1, U<sub>is</sub> = {x'[1]}
			// for j=2 to n' do
			//	if b<sub>i</sub>(x[z]')=b<sub>i</sub>(x[j]') then
			//		U<sub>is</sub>.add( {x'[j]} ).
			//	else 
			//		s=s+1, U<sub>is</sub>={x'[j]}, z=j
			// for the above actions, is to partition U<sub>i</sub> based on their attribute values.
			int attrValuePointer;
			Instance ins;
			EquivalenceClass equClass;
			Collection<EquivalenceClass> equClasses = new LinkedList<>();
			Iterator<Instance> insIterator = Instances.iterator();
			
			ins = insIterator.next();
			attrValuePointer = ins.getAttributeValue(attribute);
			equClasses.add(equClass=new EquivalenceClass(attrValuePointer));
			equClass.add(ins);
			while (insIterator.hasNext()) {
				// next instance
				ins = insIterator.next();
				// check if a new equivalent class is required.
				if (attrValuePointer!=ins.getAttributeValue(attribute)) {
					attrValuePointer = ins.getAttributeValue(attribute);
					equClasses.add(equClass=new EquivalenceClass(attrValuePointer));
				}
				// add instance into equivalent class.
				equClass.add(ins);
			}
			return equClasses;
		}
	
	}

	public static class VariantObjects {
		
		/**
		 * Tolerance classes maintaining for variant objects. Maintain tolerance classes for
		 * <code>invariances</code> and <code>variances</code> by combining tolerance classes
		 * into one map and add additional tolerance classes.
		 * 
		 * @see CombTClassesResult4VariantObjs
		 * 
		 * @param invariances
		 * 		In-variance {@link Instance}s.
		 * @param variances
		 * 		Variance {@link Instance}s.
		 * @param attributes
		 * 		All attributes of {@link Instance}.
		 * @return Wrapped in {@link CombTClassesResult4VariantObjs}.
		 */
		public static <InstancesCollector> CombTClassesResult4VariantObjs toleranceClasses(
			Collection<Instance> invariances, Collection<Instance> variances,
			IntegerIterator attributes,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4AttributesOfInvariances,
			InstancesCollector completeData4AttributesOfVariances
		) {
			// Get tolerance classes of invariances: (U'-Ux')/TR(B)
			Map<Instance, Collection<Instance>>	tolerancesOfInvariances =
					toleranceClassObtainer.obtain(
						invariances, invariances, attributes, 
						completeData4AttributesOfInvariances
					);
		
			// Get tolerance classes of variances: Ux'/TR(B)
			Map<Instance, Collection<Instance>>	tolerancesOfVariances =
					toleranceClassObtainer.obtain(
						variances, variances, attributes, 
						completeData4AttributesOfVariances
					);
			
			// A hash map to contain tolerance classes of U': U'/TR(B)
			Map<Instance, Collection<Instance>> combined =
				new HashMap<>(invariances.size()+variances.size());
			// U'/TR(B) contains (U'-Ux')/TR(B)
			combined.putAll(tolerancesOfInvariances);
			// U'/TR(B) contains Ux'/TR(B)
			combined.putAll(tolerancesOfVariances);
			
			// Get the additional tolerance classes when combining (U'-Ux') and Ux'
			//	for "ones to be added" in U'/TR(B) comparing to (U'-Ux')/TR(B) and
			//	Ux'/TR(B).
			Map<Instance, Collection<Instance>> additionalTolerances =
				toleranceClassObtainer.obtain(
					invariances, variances, attributes, 
					completeData4AttributesOfVariances
				);
			
			// Record the universe instances being updated.
			//	Additional ones in (U'-Ux')/TR(B) are updated.
			Collection<Instance> invariancesUpdated = additionalTolerances.keySet();
			//	Additional ones in Ux'/TR(B) are updated.
			Collection<Instance> variancesUpdated = new HashSet<>(variances.size());
			
			for (Entry<Instance, Collection<Instance>> extraInvariancesEntry:
				additionalTolerances.entrySet()
			) {
				// Maintain U'/TR(B): Additional ones of tolerance classes for ones in variances.
				combined.get(extraInvariancesEntry.getKey()).addAll(extraInvariancesEntry.getValue());
				// Maintain U'/TR(B): Additional ones of tolerance classes for ones in in-variances.
				for (Instance toleranceOfVariance: extraInvariancesEntry.getValue())
					combined.get(toleranceOfVariance).add(extraInvariancesEntry.getKey());
				
				//	Additional ones in Ux'/TR(B) are updated.
				variancesUpdated.addAll(extraInvariancesEntry.getValue());
			}
			
			return new CombTClassesResult4VariantObjs(
					invariancesUpdated, variancesUpdated, 
					tolerancesOfInvariances, tolerancesOfVariances, combined
				);
		}
	
		/**
		 * Sort the given attributes outside of <code>previousReduct</code> in descending sequence of
		 * sig<sub>2</sub>:
		 * <pre>sig<sub>2</sub>(a, B, D) = POS<sub>B∪{a}</sub>(D)-POS<sub>B</sub>(D)</pre>
		 * 
		 * @param instances
		 * 		{@link Instance}s for sig<sub>2</sub> calculation.
		 * @param previousReduct
		 * 		Previous reduct.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @param toleranceClassObtainer
		 *      A {@link ToleranceClassObtainer}.
		 * @param calculation
		 * 		{@link FeatureImportance4ToleranceClassPositiveRegionIncremental} instance.
		 * @param completeData4PreviousReductOfInstances
		 * 		{@link InstancesCollector} that contains complete data of previous reduct of
		 * 	    instances.
		 * @return Sorted attributes in {@link Integer} {@link Collection}.
		 */
		public static ReductCandidateResult4VariantObjects descendingSequenceSortedAttributes(
			Collection<Instance> instances,
			Collection<Integer> previousReduct, int[] attributes,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4PreviousReductOfInstances,
			FeatureImportance4ToleranceClassPositiveRegionIncremental calculation
		){
			// ---------------------------------------------------------------------------------------------
			// | sig<sub>2</sub>(a, B, D) = POS<sub>B∪{a}</sub>(D)-POS<sub>B</sub>(D) 					   |
			// | 																	 					   |
			// | Because POS<sub>B</sub>(D) is fixed, comparisons of sig<sub>2</sub> between features	   |
			// | could be made based on POS<sub>B∪{a}</sub>(D).											   |
			// ---------------------------------------------------------------------------------------------
			// Collect reduct candidates: C-B
			final Collection<Integer> previousReductHash = 
				previousReduct instanceof Set? previousReduct: new HashSet<>(previousReduct);
			List<Integer> reductCandidate =
					Arrays.stream(attributes)
						.filter(attr->!previousReductHash.contains(attr))
						.boxed()
						.collect(Collectors.toList());
			// Calculate POS<sub>B∪{a}</sub>(D) for every attribute in reduct candidates(C-B).
			int[] partitionAttributes = new int[previousReduct.size()+1];
			int i=0;	for (int attr: previousReduct)	partitionAttributes[i++]=attr;
			
			final Map<Integer, Integer> posOfCandidates = new HashMap<>(reductCandidate.size());
			for (int candidate: reductCandidate) {
				partitionAttributes[i] = candidate;
				int pos = calculation.calculate(
								toleranceClassObtainer.obtain(
									instances, instances,
									new IntegerArrayIterator(partitionAttributes),
									completeData4PreviousReductOfInstances
								).entrySet()
							).getResult();
				posOfCandidates.put(candidate, pos);
			}
			// Sort reduct candidates in descending sequence.
			Collections.sort(reductCandidate, (attr1, attr2)->-(posOfCandidates.get(attr1)-posOfCandidates.get(attr2)));
			return new ReductCandidateResult4VariantObjects(previousReductHash, reductCandidate);
		}
		
		/**
		 * Search for the most significant attribute for the current reduct.
		 * 
		 * @param instances
		 * 		{@link Instance}s.
		 * @param invariances
		 * 		In-variant {@link Instance}.(i.e. ones stay in-variant)
		 * @param changedFromInstances
		 * 		Variant {@link Instance} changed from.
		 * @param changedToInstances
		 * 		Variant {@link Instance} changed to.
		 * @param newReduct
		 * 		Current reduct.
		 * @param reductCandidates
		 * 		Attributes of {@link Instance} outside <code>newReduct</code>.
		 * @param completeData4AttributesOfInstances
		 * 		{@link EquivalenceClass} {@link Collection} array that contains complete data
		 * 		(in <code>Instances</code>) for every attributes.
		 * @param completeData4AttributesOfInvariances
		 * 		{@link EquivalenceClass} {@link Collection} array that contains complete data
		 * 		(in <code>invariances</code>) for every attributes.
		 * @param completeData4AttributesOfChangedToInstances
		 * 		{@link EquivalenceClass} {@link Collection} array that contains complete data
		 * 		(in <code>changedToInstances</code>) for every attributes.
		 * @param calculation
		 * 		{@link FeatureImportance4ToleranceClassPositiveRegionIncremental} instance.
		 * @return {@link MostSignificantAttributeResult} instance.
		 */
		public static MostSignificantAttributeResult mostSiginificantAttribute(
			Collection<Instance> instances, Collection<Instance> invariances,
			Collection<Instance> changedFromInstances, Collection<Instance> changedToInstances,
			Collection<Integer> newReduct, Collection<Integer> reductCandidates,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4AttributesOfInstances,
			InstancesCollector completeData4AttributesOfInvariances,
			InstancesCollector completeData4AttributesOfChangedToInstances,
			FeatureImportance4ToleranceClassPositiveRegionIncremental calculation
		) {
			int sigAttr = -1, sig = 0;
			// for h=1 to |C-B| do
			for (int attr: reductCandidates) {
				if (newReduct.contains(attr))	continue;
				// let B<-B∪{c'[h]} and compute POS<sub>B</sub><sup>U'</sup> by Equ.(4)
				newReduct.add(attr);
				// -----------------------------------------------------------------------------------------
				// | POS<sub>B</sub><sup>U'</sup>(D) = POS<sub>B</sub><sup>U</sup>(D)					   |
				// | 									∪ POS<sub>B</sub><sup>Ux'</sup>(D)				   |
				// | 									∪ {xp | |S<sub>B</sub>(xp)/IND(D)|==1 }			   |
				// | 									- Ux											   |
				// | 									- {xi | |S'<sub>B</sub>(xi)/IND(D)|!=1 }		   |
				// | 									- {u'k | |S'<sub>B</sub>(u'k)/IND(D)|!=1 }		   |
				// | (1<=p<=r, 1<=i<=e, 1<=k<=e')														   |
				// -----------------------------------------------------------------------------------------
				IntegerIterator newReductIterator = new IntegerCollectionIterator(newReduct);
				final Collection<Instance> positiveRegion = new HashSet<>(instances.size());
				// -----------------------------------------------------------------------------------------
				// | POS<sub>B</sub><sup>U</sup>(D)														   |
				// -----------------------------------------------------------------------------------------
				positiveRegion.addAll(
					calculation.calculate(
						toleranceClassObtainer.obtain(
							instances, instances, newReductIterator,
							completeData4AttributesOfInstances
						).entrySet()
					).getPositiveRegionInstances()
				);
				// -----------------------------------------------------------------------------------------
				// | POS<sub>B</sub><sup>Ux'</sup>(D)													   |
				// -----------------------------------------------------------------------------------------
				positiveRegion.addAll(
					calculation.calculate(
						toleranceClassObtainer.obtain(
							changedToInstances, changedToInstances, newReductIterator,
							completeData4AttributesOfChangedToInstances
						).entrySet()
					).getPositiveRegionInstances()
				);
				// -----------------------------------------------------------------------------------------
				// | {xp | |S<sub>B</sub>(xp)/IND(D)|==1 }												   |
				// | i.e. Unchanged tolerance classes of in-variances which are consist.				   |
				// -----------------------------------------------------------------------------------------
				CombTClassesResult4VariantObjs combinedToleranceClassResult =
					toleranceClasses(
						invariances, changedToInstances, newReductIterator,
						toleranceClassObtainer,
						completeData4AttributesOfInvariances,
						completeData4AttributesOfChangedToInstances
					);
				Collection<Instance> consistInvariancesWhoseTolerancesUnchanged =
					combinedToleranceClassResult.getTolerancesOfInvariances().entrySet().stream()
						.filter(
							// select unchanged ones.
							entry->!combinedToleranceClassResult.getInvariancesUpdated().contains(entry.getKey())
						).filter(entry->{
							// select consist ones.
							if (entry.getValue().size()<=1)	return true;
							Iterator<Instance> iterator = entry.getValue().iterator();
							int dec = iterator.next().getAttributeValue(0);
							while (iterator.hasNext()) {
								if (iterator.next().getAttributeValue(0)!=dec)
									return false;
							}
							return true;
						}).map(Entry::getKey)
						.collect(Collectors.toList());
				positiveRegion.addAll(consistInvariancesWhoseTolerancesUnchanged);
				// -----------------------------------------------------------------------------------------
				// | Ux																					   |
				// -----------------------------------------------------------------------------------------
				positiveRegion.removeAll(changedFromInstances);
				// -----------------------------------------------------------------------------------------
				// | {xi | |S'<sub>B</sub>(xi)/IND(D)|!=1 }												   |
				// | i.e. Updated tolerance classes of in-variances which are in-consist.				   |
				// -----------------------------------------------------------------------------------------
				Collection<Instance> inconsistInvariancesWhoseTolerancesUpdated =
					combinedToleranceClassResult.getTolerancesOfInvariances().entrySet().stream()
						.filter(
							// select updated ones
							entry->combinedToleranceClassResult.getInvariancesUpdated().contains(entry.getKey())
						).filter(entry->{
							// select in-consist ones.
							if (entry.getValue().isEmpty())	return false;
							Iterator<Instance> iterator = entry.getValue().iterator();
							int dec = iterator.next().getAttributeValue(0);
							while (iterator.hasNext())
								if (iterator.next().getAttributeValue(0)!=dec)	return true;
							return false;
						}).map(Entry::getKey)
						.collect(Collectors.toList());
				positiveRegion.removeAll(inconsistInvariancesWhoseTolerancesUpdated);
				// -----------------------------------------------------------------------------------------
				// | {u'k | |S'<sub>B</sub>(u'k)/IND(D)|!=1 }											   |
				// -----------------------------------------------------------------------------------------
				Collection<Instance> inconsistVariancesWhoseTolerancesUpdated =
					combinedToleranceClassResult.getTolerancesOfVariances().entrySet().stream()
						.filter(
							// select updated ones.
							entry->combinedToleranceClassResult.getVariancesUpdated().contains(entry.getKey())
						).filter(entry->{
							// select in-consist ones.
							Iterator<Instance> iterator = entry.getValue().iterator();
							int dec = iterator.next().getAttributeValue(0);
							while (iterator.hasNext()) {
								if (iterator.next().getAttributeValue(0)!=dec)	return true;
							}
							return false;
						}).map(Entry::getKey)
						.collect(Collectors.toList());
				positiveRegion.removeAll(inconsistVariancesWhoseTolerancesUpdated);

				if (sigAttr==-1 || positiveRegion.size()>sig) {
					sigAttr = attr;
					sig = positiveRegion.size();
				}
				
				newReduct.remove(attr);
			}
			return new MostSignificantAttributeResult(sigAttr, sig);
		}
	}

	
	/**
	 * Search for the most significant attribute for the current reduct.
	 * 
	 * @param instances
	 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
	 * @param reduct
	 * 		Current reduct.
	 * @param attributes
	 * 		Attributes of {@link Instance}: <strong>P</strong>
	 * @param completeData4Attributes
	 * 		{@link InstancesCollector} that contains complete data
	 * 		(in <strong><code>instances</code></strong>) for every attributes(in  
	 * 		<code>attributes</code>).
	 * @param calculation
	 * 		{@link FeatureImportance4ToleranceClassPositiveRegionIncremental} instance.
	 * @return {@link MostSignificantAttributeResult}.
	 */
	public static MostSignificantAttributeResult mostSignificantAttribute(
			Collection<Instance> instances,
			Collection<Integer> reduct, int[] attributes, 
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4Attributes,
			FeatureImportance4ToleranceClassPositiveRegionIncremental calculation
	) {
		Iterator<Integer> reductIterator = reduct.iterator();
		int[] partitionAttributes = new int[reduct.size()+1];
		for (int i=0; i<reduct.size(); i++)	partitionAttributes[i] = reductIterator.next();
		
		int sigAttr=-1;
		Integer sigValue=null, sig;
		for(int attr: attributes) {
			if (reduct.contains(attr))	continue;
			partitionAttributes[partitionAttributes.length-1] = attr;
			
			Map<Instance, Collection<Instance>> tolerances =
				toleranceClassObtainer.obtain(
					instances, instances,
					new IntegerArrayIterator(partitionAttributes), 
					completeData4Attributes
				);
			
			sig = calculation.calculate(tolerances.entrySet()).getResult();
			if (sigAttr==-1 || calculation.value1IsBetter(sig, sigValue, 0)) {
				sigValue = sig;
				sigAttr = attr;
			}
		}
		return new MostSignificantAttributeResult(sigAttr, sigValue==null? 0: sigValue.intValue());
	}

	
	/**
	 * Execute inspection by removing attributes from <code>reduct</code> one by one and check if
	 * the result of {@link FeatureImportance4ToleranceClassPositiveRegionIncremental#calculate(Collection)}
	 * changed: 
	 * <ul>
	 * 	<li>changed: is reduct, stayed.</li>
	 * 	<li>un-changed: is not reduct, removed.</li>
	 * </ul>
	 * <p>
	 * <strong>Notice: </strong>
	 * Elements in <code>reduct</code> will be removed if necessary.
	 * 
	 * @param instances
	 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
	 * @param reduct
	 * 		Current reduct.
	 * @param reductPos
	 * 		The reduct positive region.
	 * @param completeData4Reduct
	 * 		{@link InstancesCollector} that contains complete data
	 * 		(in <strong><code>instances</code></strong>) for every attributes(in  
	 * 		<code>reduct</code>).
	 * @param calculation
	 * 		{@link FeatureImportance4ToleranceClassPositiveRegionIncremental} instance.
	 */
	public static void inspection(
		Collection<Instance> instances, Collection<Integer> reduct,
		int reductPos, 
		ToleranceClassObtainer toleranceClassObtainer,
		InstancesCollector completeData4Reduct,
		FeatureImportance4ToleranceClassPositiveRegionIncremental calculation
	){
		Integer[] reductCopy = reduct.toArray(new Integer[reduct.size()]);
		for (int attr: reductCopy) {
			reduct.remove(attr);
			int pos = calculation.calculate(
						toleranceClassObtainer.obtain(
							instances, instances,
							new IntegerCollectionIterator(reduct), 
							completeData4Reduct
						).entrySet()
					).getResult();
			if (calculation.value1IsBetter(reductPos, pos, 0))
				reduct.add(attr);
		}
	}
}