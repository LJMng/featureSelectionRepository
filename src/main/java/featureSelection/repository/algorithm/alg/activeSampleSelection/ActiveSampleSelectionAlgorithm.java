package featureSelection.repository.algorithm.alg.activeSampleSelection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.activeSampleSelection.AttrDiscernibilityResult;
import featureSelection.repository.entity.alg.activeSampleSelection.DiscernIntersectionResult;
import featureSelection.repository.entity.alg.activeSampleSelection.EquivalenceClass;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePairAttributeInfo;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePairSelectionResult;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.family.SamplePairFamily;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ActiveSampleSelection;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.function.Function;


/**
 * Algorithm repository of <strong>Active Sample Selection</strong>, which bases on papers:
 * <ul>
 *     <li><a href="https://ieeexplore.ieee.org/document/7492272">"Active Sample Selection
 *     		Based Incremental Algorithm for Attribute Reduction With Rough Sets"</a> by
 *     		Yanyan Yang, Degang Chen, Hui Wang</li>
 *     <li><a href="https://ieeexplore.ieee.org/document/6308684/">"Sample Pair Selection
 *     		for Attribute Reduction with Rough Set"</a>by Degang Chen, Suyun Zhao, Lei Zhang,
 *     		Yongping Yang, Xiao Zhang.</li>
 * </ul>
 * <p>
 * Please check the <i>Notice</i> of {@link Basic.Incremental.MinimalElementsUpdating} if use
 * the <i>Active Sample Selection Based Incremental Algorithm</i>
 * 
 * @author Benjamin_L
 */
@RoughSet
public class ActiveSampleSelectionAlgorithm {

	/**
	 * Basic Algorithms for {@link ActiveSampleSelectionAlgorithm}. Including some mathematical
	 * utilities and sample pair utilities as well as other basic implementations.
	 * 
	 * @author Benjamin_L
	 */
	public static class Basic {
		/**
		 * Some mathematical utilities for <strong>Active Sample Pair Selection</strong>.
		 * 
		 * @author Benjamin_L
		 */
		public static class Mathematicals {
			/**
			 * Calculate the <code>intersection</code> of two <code>discerns</code> that
			 * contain attributes.
			 * <p>
			 * {@link Collection#stream()}.filter() is used to calculate the intersection of
			 * two sample pairs.
			 * 
			 * @see List#retainAll(Collection)
			 * 
			 * @param samplePair1
			 * 		A {@link SamplePair} {@link Collection}.
			 * @param samplePair2
			 * 		Another {@link SamplePair} {@link Collection}.
			 * @return A {@link SamplePair} {@link Collection} as intersection.
			 */
			public final static Collection<SamplePair> intersectionOf(
					Collection<SamplePair> samplePair1, Collection<SamplePair> samplePair2
			) {
				if (samplePair1.isEmpty() || samplePair2.isEmpty())
					return Collections.emptyList();
				
				if (samplePair1 instanceof List) {
					final Collection<SamplePair> sm, lg;
					if (samplePair1.size()>samplePair2.size()) {
						lg = new HashSet<>(samplePair1);
						sm = samplePair2;
					}else {
						lg = new HashSet<>(samplePair2);
						sm = samplePair1;
					}
					return sm.stream().filter(sp->lg.contains(sp)).collect(Collectors.toList());//*/
				}else {
					final Collection<SamplePair> sm, lg;
					if (samplePair1.size()>samplePair2.size()) {
						lg = samplePair1;
						sm = samplePair2;
					}else {
						lg = samplePair2;
						sm = samplePair1;
					}
					final Collection<SamplePair> hashOfLgDiscern = new HashSet<>(lg);
					return sm.stream().filter(sp->hashOfLgDiscern.contains(sp)).collect(Collectors.toSet());
				}
			}
		
			/**
			 * Calculate the <code>intersection</code> of <code>discernLocalAttributes</code> and 
			 * <code>samplePair</code>.
			 * <p>
			 * Pair = ∩{DIS({b}): (x<sub>i<sub>0</sub></sub>, x<sub>j<sub>0</sub></sub>)∈
			 * DIS({b})} and C<sub>ij</sub> = ∪{b∈A: (x<sub>i<sub>0</sub></sub>,
			 * x<sub>j<sub>0</sub></sub>)∈DIS({b})}
			 * <p>
			 * Basically, this method loops over <code>discernibilityMatrix</code> and collects
			 * attributes whose correspondent discernibility matrix contains the given 
			 * <code>samplePair</code>. Meanwhile, the intersection of sample pairs that in those
			 * attributes' discernibility matrix is acquired.
			 * 
			 * @see #intersectionOfAllDiscernsContainsSamplePair(Map, Collection[], SamplePair, int[])
			 * 
			 * @param discernibilityMatrix
			 * 		{@link Integer} {@link Collection} array as discerns.
			 * @param samplePair
			 * 		A {@link SamplePair}.
			 * @param attributes
			 * 		{@link Instance} attributes.
			 * @return {@link DiscernIntersectionResult} which contains <code>Pair</code> and 
			 * 		<code>C<sub>ij</sub></code>.
			 */
			@Deprecated
			public static DiscernIntersectionResult intersectionOfAllDiscernsContainsSamplePair(
					Collection<SamplePair>[] discernibilityMatrix, SamplePair samplePair,
					int[] attributes
			) {
				int dis1Index = 0;
				Collection<Integer> filteredAttributes = new LinkedList<>();
				// for k=1 to |C|
				Collection<SamplePair> intersection = null;
				for (int k=0; k<discernibilityMatrix.length; k++) {
					// if (i, j) in DIS({a[k]})
					Collection<SamplePair> hashSet = new HashSet<>(discernibilityMatrix[k]);
					if (hashSet.contains(samplePair)) {
						// DIS[1]=DIS({a[k]})
						dis1Index = k;
						intersection = hashSet;
						// c(i, j).add(a[k])
						filteredAttributes.add(attributes[k]);
						break;
					}
				}
				if (intersection!=null) {
					// For p=dis1Index+1 to |C|
					for (int p=dis1Index+1; p<discernibilityMatrix.length; p++) {
						// if (x[i], x[j]) in DIS({a[p]})
						Collection<SamplePair> hashSet = new HashSet<>(discernibilityMatrix[p]);
						if (hashSet.contains(samplePair)) {
							// c(i, j).add(a[p])
							filteredAttributes.add(attributes[p]);
							// IN = intersectionOf(DIS1, DIS2)
							intersection = Mathematicals.intersectionOf(
											intersection, discernibilityMatrix[p]
										);
						}
					}
				}
				return new DiscernIntersectionResult(intersection, filteredAttributes);//*/
			}
			/**
			 * <strong>(Improved version)</strong> 
			 * <p>
			 * Calculate the <code>intersection</code> of <code>discernLocalAttributes</code> and 
			 * <code>samplePair</code>.
			 * <p>
			 * Pair = ∩{DIS({b}): (x<sub>i<sub>0</sub></sub>, x<sub>j<sub>0</sub></sub>)∈
			 * DIS({b})} and C<sub>ij</sub> = ∪{b∈A: (x<sub>i<sub>0</sub></sub>,
			 * x<sub>j<sub>0</sub></sub>)∈DIS({b})}
			 * <p>
			 * Basically, this method loops over <code>discernibilityMatrix</code> and collects
			 * attributes whose correspondent discernibility matrix contains the given 
			 * <code>samplePair</code>. Meanwhile, the intersection of sample pairs that in those
			 * attributes' discernibility matrix is acquired.
			 * <p>
			 * Comparing to {@link #intersectionOfAllDiscernsContainsSamplePair(Collection[], SamplePair, int[])},
			 * this method is improved by using <code>discernibilityMatrixMap</code> to get the 
			 * discernible attributes for two {@link Instance}, which skipped the collecting of
			 * the discernible attributes.
			 * 
			 * @param discernibilityMatrixMap
			 * 		A {@link Map} with {@link Instance} as keys and {@link Map}s whose keys are
			 * 		another {@link Instance} and values are the corresponding discernible
			 * 		attribute {@link Collection}s as values.
			 * @param discernibilityMatrix
			 * 		{@link Integer} {@link Collection} array as discerns.
			 * @param samplePair
			 * 		A {@link SamplePair}.
			 * @param attributes
			 * 		{@link Instance} attributes.
			 * @return {@link DiscernIntersectionResult} which contains <code>Pair</code> and 
			 * 		<code>C<sub>ij</sub></code>.
			 */
			public static DiscernIntersectionResult intersectionOfAllDiscernsContainsSamplePair(
					Map<Instance, Map<Instance, Collection<Integer>>> discernibilityMatrixMap,
					Collection<SamplePair>[] discernibilityMatrix, SamplePair samplePair,
					int[] attributes
			) {
				Map<Instance, Collection<Integer>> subDiscernibilityMatrixMap =
						discernibilityMatrixMap.get(samplePair.getPair()[0]);
				if (subDiscernibilityMatrixMap!=null) {
					Collection<Integer> samplePairDiscernAttributeIndexes = 
							subDiscernibilityMatrixMap.get(samplePair.getPair()[1]);
					if (samplePairDiscernAttributeIndexes!=null) {
						Collection<Integer> filteredAttributes = new HashSet<>(samplePairDiscernAttributeIndexes.size());
						Collection<SamplePair> intersection = null;
						// Loop over sample pair collections in discernibilityMatrix bases on 
						//	samplePairDiscernAttributeIndexes and calculate the intersection
						//	of them.
						for (int attrIndex: samplePairDiscernAttributeIndexes) {
							// c(i, j).add(a[p])
							filteredAttributes.add(attributes[attrIndex]);
							// IN = intersectionOf(DIS1, DIS2)
							if (intersection==null) {
								intersection = discernibilityMatrix[attrIndex];
							}else if (!intersection.isEmpty()) {
								intersection = Mathematicals.intersectionOf(
												intersection, discernibilityMatrix[attrIndex]
											);
							}
						}
						return new DiscernIntersectionResult(intersection, filteredAttributes);
					}
				}
				return new DiscernIntersectionResult(new ArrayList<>(0), new ArrayList<>(0));
			}
			
			/**
			 * Calculate the <code>intersection</code> of <code>intArray</code> and
			 * <code>intValuescode>.
			 * 
			 * @param intArray
			 * 		An int array.
			 * @param intValues
			 * 		An int value {@link Collection}.
			 * @return The intersection of the two int values.
			 */
			public static Collection<Integer> intersectionOf(
					int[] intArray, Collection<Integer> intValues
			){
				Collection<Integer> intArraySet = Arrays.stream(intArray).boxed().collect(Collectors.toSet());
				Collection<Integer> intersection = new LinkedList<>(intValues);
				intersection.retainAll(intArraySet);
				return intersection;
			}
		}
			
		/**
		 * Some utilities for <strong>Sample pairs</strong>.
		 * 
		 * @author Benjamin_L
		 */
		public static class SamplePairs {
			/**
			 * Collect all {@link SamplePair} in the given <code>attrDiscernibilities</code> and
			 * return.
			 * 
			 * @param attrDiscernibilities
			 * 		{@link SamplePair} {@link Collection} array.
			 * @param hashCapacity
			 * 		The capacity value for {@link HashSet} for the output result. <code>16</code>
			 * 		as default capacity in {@link HashSet}.
			 * @return {@link SamplePair} {@link HashSet}.
			 */
			public static Collection<SamplePair> distinct(
					Collection<SamplePair>[] attrDiscernibilities, int hashCapacity
			){
				Collection<SamplePair> allAttrDiscernibility = new HashSet<>(hashCapacity);
				// Calculate repeated sample pair in DIS({A}).
				for (Collection<SamplePair> pair: attrDiscernibilities) {
					if (pair!=null)	allAttrDiscernibility.addAll(pair);
				}
				return allAttrDiscernibility;
			}
			
			/**
			 * Count the frequency of attributes for each sample pair.
			 * <p>
			 * Comparing to {@link #countFrequencyUsingStream(Collection[])}, this method is
			 * faster based on the tested results on Datasets: <i>UJlindoorLoc</i>,
			 * <i>ticdata2000</i>, <i>breast-cancer-wisconsin.data</i>
			 * 
			 * @param attrDiscernibilities
			 * 		{@link SamplePair} {@link Collection} array.
			 * @param hashCapacity
			 * 		The capacity value for {@link HashSet} for the output result. <code>16</code>
			 * 		as default capacity in {@link HashSet}.
			 * @return {@link Map} with {@link SamplePair}s as keys and frequencies in
			 * 		{@link Integer} as values.
			 */
			public static Map<SamplePair, Integer> countFrequency(
					Collection<SamplePair>[] attrDiscernibilities, int hashCapacity
			){
				Map<SamplePair, Integer> counter = new HashMap<>(hashCapacity);
				Integer countValue;
				for (Collection<SamplePair> samplePairs: attrDiscernibilities) {
					if (samplePairs==null)	continue;
					for (SamplePair sp: samplePairs) {
						countValue = counter.get(sp);
						if (countValue==null)	counter.put(sp, 1);
						else					counter.put(sp, countValue+1);
					}
				}
				return counter;
			}
			/**
			 * Count the frequency of attributes for each sample pair.
			 * 
			 * @see #countFrequency(Collection[], int)
			 * 
			 * @param attrDiscernibilities
			 * 		{@link SamplePair} {@link Collection} array.
			 * @return {@link Map} with {@link SamplePair}s as keys and frequencies in
			 * 		{@link Integer} as values.
			 */
			public static Map<SamplePair, Integer> countFrequencyUsingStream(
					Collection<SamplePair>[] attrDiscernibilities
			){
				Map<SamplePair, Integer> counter = 
					Arrays.stream(attrDiscernibilities)
						.filter(collection->collection!=null)
						.map(sps->sps.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())))
						.flatMap(m->m.entrySet().stream())
						.collect(Collectors.toMap(Entry::getKey, entry->entry.getValue().intValue(), Integer::sum));//*/
				return counter;
			}
			
			/**
			 * Collect attributes for each sample pair as well as to count the frequency of
			 * attributes.
			 * 
			 * @see #countFrequency(Collection[], int)
			 * 
			 * @param attrDiscernibilities
			 * 		{@link SamplePair} {@link Collection} array.
			 * @param hashCapacity
			 * 		The capacity value for {@link HashSet} for the output result. <code>16</code>
			 * 		as default capacity in {@link HashSet}.
			 * @param attributes
			 * 		Attributes of {@link Instance}.
			 * @return {@link Map} with {@link SamplePair}s as keys and
			 * 		{@link SamplePairAttributeInfo} as values.
			 */
			public static Map<SamplePair, SamplePairAttributeInfo> collectAttributes(
					Collection<SamplePair>[] attrDiscernibilities, int hashCapacity,
					int[] attributes
			){
				// Initiate a map for SamplePairAttributeInfo collecting.
				Map<SamplePair, SamplePairAttributeInfo> collector = new HashMap<>(hashCapacity);
				SamplePairAttributeInfo samplePairAttrInfo;
				for (int a=0; a<attributes.length; a++) {
					if (attrDiscernibilities[a]==null)	continue;
					for (SamplePair sp: attrDiscernibilities[a]) {
						samplePairAttrInfo = collector.get(sp);
						if (samplePairAttrInfo==null)	collector.put(sp, samplePairAttrInfo = new SamplePairAttributeInfo());
						samplePairAttrInfo.add(a);
					}
				}
				return collector;
			}
	
			/**
			 * Copy Sample Pair Family Map. With the same {@link IntArrayKey} instances as keys and
			 * deep-copied {@link SamplePair} collections as values.
			 * 
			 * @param minimalElementMap
			 * 		The Sample Pair Family map to be copied.
			 * @return Copied Sample Pair Family Map.
			 */
			public static Map<IntArrayKey, Collection<SamplePair>> copySamplePairFamilyMap(
					Map<IntArrayKey, Collection<SamplePair>> minimalElementMap
			){
				Map<IntArrayKey, Collection<SamplePair>> copy = new HashMap<>(minimalElementMap.size());
				for (Entry<IntArrayKey, Collection<SamplePair>> entry: minimalElementMap.entrySet())
					copy.put(
						new IntArrayKey(Arrays.stream(entry.getKey().getKey()).sorted().toArray()), 
						new LinkedList<>(entry.getValue())
					);
				return copy;
			}
		}
		
		/**
		 * Get {@link EquivalenceClass}es of {@link Instance}s.
		 * 
		 * @param instances
		 * 		An {@link Instance} {@link Collection}.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return {@link EquivalenceClass}es.
		 */
		public static Map<IntArrayKey, EquivalenceClass> equivalenceClasses(
				Collection<Instance> instances, IntegerIterator attributes
		){
			// Initiate a hash
			int[] attrValue;
			IntArrayKey key;
			EquivalenceClass equClass;
			Map<IntArrayKey, EquivalenceClass> equClasses = new HashMap<>(instances.size());
			for (Instance ins: instances) {
				// key = C(x)
				attrValue = Instance.attributeValuesOf(ins, attributes);
				key = new IntArrayKey(attrValue);
				// if key is not in hash.
				equClass = equClasses.get(key);
				if (equClass==null) {
					// create equClass
					equClasses.put(key, equClass=new EquivalenceClass());
					// equClass.v = x
					// equClass.dec = D(x)
					equClass.setDecision(ins.getAttributeValue(0));
				// if key in hash
				}else {
					// if equClass.dec!=D(x)
					//	equClass.dec = '/'
					if (equClass.getDecision()!=null && 
						!equClass.getDecision().equals(ins.getAttributeValue(0))
					) {
						equClass.setDecision(null);
					}
				}
				equClass.addUniverse(ins);
			}
			return equClasses;
		}
		
		/**
		 * Calculate the discernibility of each universe instances: DIS({a}).
		 * <p>
		 * For each (x[i], x[j]) in DIS({a}), x[i] and x[j] is {@link Instance} instance as a pair
		 * of sample in {@link SamplePair} that using attribute 'a', the two {@link Instance} can
		 * be discerned/distinguished. And the two {@link Instance}s meet one of the following
		 * conditions:
		 * <ul>
		 * 	<li>x<sub>1</sub>∈POS(C, D) && x<sub>2</sub>∉POS(C, D)</li>
		 * 	<li>x<sub>1</sub>∉POS(C, D) && x<sub>2</sub>∈POS(C, D)</li>
		 * 	<li>x<sub>1</sub>∉POS(C, D) && x<sub>2</sub>∉POS(C, D) &&
		 * 		d(x<sub>1</sub>)!=d(x<sub>2</sub>)
		 * 	</li>
		 * </ul>
		 * 
		 * @param equClasses
		 * 		{@link EquivalenceClass}es: <strong>U/C</strong>
		 * @param insSize
		 * 		The size of {@link Instance}: <strong>|U|</strong>
		 * @param attributes
		 * 		Attributes of {@link Instance} in <code>int[]</code>. Attributes should be the exact
		 * 		one used in the partitioning of {@link Instance}(<strong>U</strong>) (i.e
		 * 		calculating U/C) with <code>int[]</code> in it have the same sequence and values.
		 * @return DIS({a}): {@link AttrDiscernibilityResult}.
		 */
		@SuppressWarnings("unchecked")
		public static AttrDiscernibilityResult attributeDiscernibility(
				Map<IntArrayKey, EquivalenceClass> equClasses, int insSize,
				int[] attributes
		) {
			// create a discernibility matrix for each attributes.
			Collection<SamplePair>[] discernSamplePair = new Collection[attributes.length];
			Map<Instance, Map<Instance, Collection<Integer>>> discernibilityMatrix =
					new HashMap<>(insSize);
			// Loop over equivalence class [1].
			IntArrayKey key1, key2;
			SamplePair samplePair;
			EquivalenceClass equClass1, equClass2;
			Map<Instance, Collection<Integer>> innerMapOfDisMatrix;
			Entry<IntArrayKey, EquivalenceClass>[] entriesOfEquClasses =
					equClasses.entrySet()
							.toArray(new Entry[equClasses.size()]);
			for (int i=0; i<entriesOfEquClasses.length; i++) {
				// key =  C(x[i]).
				key1 = entriesOfEquClasses[i].getKey();
				// e1 = equClasses.get(key).
				equClass1 = entriesOfEquClasses[i].getValue();
				// Loop over equivalent class [2].
				for (int j=i+1; j<entriesOfEquClasses.length; j++) {
					// key =  C(x[i]).
					key2 = entriesOfEquClasses[j].getKey();
					// e2 = equClasses.get(key).
					equClass2 = entriesOfEquClasses[j].getValue();
					// if	e1.dec!='/' && e2.dec=='/' or 
					//		e1.dec=='/' && e2.dec!='/' or 
					//		e1.dec!='/' && e2.dec!='/' && e1.dec!=e2.dec 
					if ((equClass1.getDecision()!=null && equClass2.getDecision()==null) ||
						(equClass1.getDecision()==null && equClass2.getDecision()!=null) ||
						(equClass1.getDecision()!=null && equClass2.getDecision()!=null  &&
						 Integer.compare(equClass1.getDecision(), equClass2.getDecision())!=0)
					) {
						// Loop over a in C.
						for (int a=0; a<attributes.length; a++) {
							// if a(e1[i])!=a(e2[j])
							//	M(i,j)=M(i,j) ∪ {a}
							//	c(i,j)=c(i,j) ∪ {a}.
							//	DIS({a}) = DIS({a}) U (i,j)
							if (key1.getKey()[a]!=key2.getKey()[a]) {
								Collection<Integer> dicsernAttributeIndexes;
								Instance insWithSmallerId, insWithLargerId;
								for (Instance ins1: equClass1.getUniverses()) {
									for (Instance ins2: equClass2.getUniverses()) {
										samplePair = new SamplePair(ins1, ins2);
										if (discernSamplePair[a]==null)	discernSamplePair[a] = new LinkedList<>();
										discernSamplePair[a].add(samplePair);
										
										// Update discern matrix.
										insWithSmallerId = samplePair.getPair()[0];
										insWithLargerId = samplePair.getPair()[1];
										
										innerMapOfDisMatrix = discernibilityMatrix.get(insWithSmallerId);
										if (innerMapOfDisMatrix==null) {
											discernibilityMatrix.put(
												insWithSmallerId, 
												innerMapOfDisMatrix=new HashMap<>(
													insSize
												)
											);
										}
										dicsernAttributeIndexes = innerMapOfDisMatrix.get(insWithLargerId);
										if (dicsernAttributeIndexes==null)
											innerMapOfDisMatrix.put(insWithLargerId, dicsernAttributeIndexes=new LinkedList<>());
										dicsernAttributeIndexes.add(a);
									}
								}
							}
						}
					}
				}
			}
			return new AttrDiscernibilityResult(
						discernSamplePair,
						Arrays.stream(discernSamplePair).map(ele->ele==null?0: ele.size()).reduce(Integer::sum).orElse(0),
						discernibilityMatrix
					);
		}
		
		/**
		 * Implementation of finding a sample pair selection by Heuristic search.
		 * <p>
		 * Implementation of <i>Algorithm 2</i> in the paper 
		 * <a href="https://ieeexplore.ieee.org/document/6308684/">
		 * "Sample Pair Selection for Attribute Reduction with Rough Set"</a> 
		 * by Degang Chen, Suyun Zhao, Lei Zhang, Yongping Yang, Xiao Zhang.
		 * <p>
		 * First, discernibility matrix is calculated using {@link #attributeDiscernibility(Map,
		 * int, int[])} to get <code>DIS({a})</code> for every attributes and <code>DIS({A})
		 * </code> with all sample pairs.
		 * <p>
		 * Second, sample pairs are sorted in <code>DIS({A})</code> in an ascending order based on
		 * the frequency of the sample pairs. {@link SamplePairs#countFrequency(Collection[], int)}
		 * is called to count the frequency of each sample pairs.
		 * <p>
		 * Finally, loop over to select sample pairs and the correspondent discerned attributes as 
		 * selected sample pair(<code>SELECTION</code>) and minimal elements(<code>M*(A, U)</code>).
		 * 
		 * @param instances
		 * 		{@link Instance} array: <strong>U</strong>
		 * @param equClasses
		 * 		{@link EquivalenceClass} {@link Map} with equivalent values in {@link IntArrayKey}
		 * 		as keys: <strong>U/C</strong>
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return {@link SamplePairSelectionResult}.
		 */
		public static SamplePairSelectionResult aSamplePairSelection(
				Instance[] instances, Map<IntArrayKey, EquivalenceClass> equClasses,
				int[] attributes
		) {
			// DIS({a}), DIS(A)= discernibility(DT, H)
			AttrDiscernibilityResult attrDiscernibilityResult = 
					attributeDiscernibility(equClasses, instances.length, attributes);
			List<SamplePair> allAttrDiscernibility = 
					new ArrayList<>(
						SamplePairs.distinct(
							attrDiscernibilityResult.getDiscernibilities(), 
							attrDiscernibilityResult.getSamplePairNumber()
						)
					);
			// Sort (i,j) in DIS(A) in an ascending order.
			Map<SamplePair, Integer> samplePairSequency = 
					Collections.unmodifiableMap(
						SamplePairs.countFrequency(
							attrDiscernibilityResult.getDiscernibilities(), 
							attrDiscernibilityResult.getSamplePairNumber()
						)
					);
			Collections.sort(allAttrDiscernibility, (d1, d2) -> {
				int cmp;
				cmp = Integer.compare(samplePairSequency.get(d1), samplePairSequency.get(d2));
				if (cmp!=0)	return cmp;
				cmp = Integer.compare(d1.getPair()[0].getNum(), d2.getPair()[0].getNum());
				if (cmp!=0)	return cmp;
				return Integer.compare(d1.getPair()[1].getNum(), d2.getPair()[1].getNum());
			});
			
			// SELECTION = null, M*(A, U)=null
			Collection<SamplePair> selection = new LinkedList<>();
			Collection<Collection<Integer>> attributesOfSelection = new LinkedList<>();
			// Loop until DIS(A) is null
			DiscernIntersectionResult discernIntersectionResult;
			while(!allAttrDiscernibility.isEmpty()) {
				// get (i[0], j[0]) in DIS(A)
				// Pair, C(i,j) =  intersection(DIS({a}), (i[0], j[0]))
				discernIntersectionResult = 
						Mathematicals.intersectionOfAllDiscernsContainsSamplePair(
							attrDiscernibilityResult.getDiscernibilityMatrix(),
							attrDiscernibilityResult.getDiscernibilities(),
							allAttrDiscernibility.get(0),
							attributes
						);
				// SELECTION.add((i[0], j[0]))
				selection.add(allAttrDiscernibility.get(0));
				// M*(A, U) = M*.add(C(i, j))
				attributesOfSelection.add(discernIntersectionResult.getSelectionAttributes());
				// DIS(A) = DIS(A)-pair.
				if (discernIntersectionResult.getIntersection()!=null)
					allAttrDiscernibility.removeAll(discernIntersectionResult.getIntersection());
			}
			return new SamplePairSelectionResult(selection, attributesOfSelection);
		}
		/**
		 * Implementation of finding a sample pair selection by Heuristic search.
		 * <p>
		 * Implementation of <i>Algorithm 2</i> in the paper 
		 * <a href="https://ieeexplore.ieee.org/document/6308684/">
		 * "Sample Pair Selection for Attribute Reduction with Rough Set"</a> 
		 * by Degang Chen, Suyun Zhao, Lei Zhang, Yongping Yang, Xiao Zhang.
		 * <p>
		 * First, discernibility matrix is calculated using {@link #attributeDiscernibility(Map,
		 * int, int[])} to get <code>DIS({a})</code> for every attributes and <code>DIS({A})</code>
		 * with all sample pairs.
		 * <p>
		 * Second, sample pairs are sorted in <code>DIS({A})</code> in an ascending order based on
		 * the frequency of the sample pairs. {@link SamplePairs#countFrequency(Collection[], int)}
		 * is called to count the frequency of each sample pairs.
		 * <p>
		 * Finally, loop over to select sample pairs and the correspondent discerned attributes as 
		 * selected sample pair(<code>SELECTION</code>) and minimal elements(<code>M*(A, U)</code>).
		 * 
		 * @param instances
		 * 		{@link Instance} {@link List}. <strong>{@link ArrayList}</strong> is recommended
		 * 		for the using of index in this method which should cost least time in 
		 * 		{@link #attributeDiscernibility(Map, int, int[])}: <strong>U</strong>
		 * @param equClasses
		 * 		{@link EquivalenceClass} {@link Map} with equivalence values in {@link IntArrayKey}
		 * 		as keys: <strong>U/C</strong>
		 * @param attributes
		 * 		Attributes of {@link Instance}
		 * @return {@link SamplePairSelectionResult}.
		 */
		public static SamplePairSelectionResult aSamplePairSelection(
				List<Instance> instances, Map<IntArrayKey, EquivalenceClass> equClasses,
				int[] attributes
		) {
			// DIS({a}), DIS(A)= discernibility(DT, H)
			AttrDiscernibilityResult attrDiscernibilityResult = 
					attributeDiscernibility(equClasses, instances.size(), attributes);
			List<SamplePair> allAttrDiscernibility = 
					new ArrayList<>(
						SamplePairs.distinct(
							attrDiscernibilityResult.getDiscernibilities(), 
							attrDiscernibilityResult.getSamplePairNumber()
						)
					);
			// Sort (i,j) in DIS(A) in an ascending order.
			Map<SamplePair, Integer> samplePairFrequency = 
					Collections.unmodifiableMap(
						SamplePairs.countFrequency(
							attrDiscernibilityResult.getDiscernibilities(), 
							attrDiscernibilityResult.getSamplePairNumber()
						)
					);
			Collections.sort(allAttrDiscernibility, (d1, d2) -> {
				// Sort base on frequency
				int cmp;
				cmp = Integer.compare(samplePairFrequency.get(d1), samplePairFrequency.get(d2));
				// Sort base on Instance 1 ID.
				if (cmp!=0)	return cmp;
				cmp = Integer.compare(d1.getPair()[0].getNum(), d2.getPair()[0].getNum());
				// Sort base on Instance 2 ID.
				if (cmp!=0)	return cmp;
				return Integer.compare(d1.getPair()[1].getNum(), d2.getPair()[1].getNum());
			});
			
			// SELECTION = null, M*(A, U)=null
			Collection<SamplePair> selection = new LinkedList<>();
			Collection<Collection<Integer>> attributesOfSelection = new LinkedList<>();
			// Loop until DIS(A) is null
			DiscernIntersectionResult discernIntersectionResult;
			while(!allAttrDiscernibility.isEmpty()) {
				// get (i[0], j[0]) in DIS(A)
				// Pair, C(i,j) = intersection(DIS({a}), (i[0], j[0]))
				discernIntersectionResult = 
						Mathematicals.intersectionOfAllDiscernsContainsSamplePair(
							attrDiscernibilityResult.getDiscernibilityMatrix(),
							attrDiscernibilityResult.getDiscernibilities(),
							allAttrDiscernibility.get(0),
							attributes
						);
				// SELECTION.add((i[0], j[0]))
				selection.add(allAttrDiscernibility.get(0));
				// M*(A, U) = M*.add(C(i, j))
				attributesOfSelection.add(discernIntersectionResult.getSelectionAttributes());
				// DIS(A) = DIS(A)-pair.
				if (discernIntersectionResult.getIntersection()!=null)
					allAttrDiscernibility.removeAll(discernIntersectionResult.getIntersection());
			}
			return new SamplePairSelectionResult(selection, attributesOfSelection);
		}
		
		/**
		 * Building a sample pair family set bases on the given Minimal Elements.
		 * 
		 * @param instances
		 * 		{@link Instance} array: <strong>U</strong>
		 * @param equClasses
		 * 		{@link EquivalenceClass}es of the {@link Instance}s: <strong>U/C</strong>
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @param minimalElements
		 * 		Minimal elements of {@link Instance}: <strong>ME</strong>
		 * @return {@link SamplePairFamily}
		 */
		public static SamplePairFamily samplePairFamilySet(
			Collection<Instance> instances, Map<IntArrayKey, EquivalenceClass> equClasses,
			int[] attributes, Collection<IntArrayKey> minimalElements
		) {
			// DIS({a})
			AttrDiscernibilityResult attrDiscernibilityResult = 
					attributeDiscernibility(equClasses, instances.size(), attributes);
			// Get sample pair discern attributes
			Map<SamplePair, SamplePairAttributeInfo> samplePairAttrInfo = 
					SamplePairs.collectAttributes(
							attrDiscernibilityResult.getDiscernibilities(),
							16,
							attributes
					);
			// Collect family.
			Map<IntArrayKey, Collection<SamplePair>> family = new HashMap<>();
			samplePairAttrInfo.entrySet().stream().forEach(entry->{
				// key: minimal element
				IntArrayKey key = new IntArrayKey(
						entry.getValue().getAttributeIndexes().stream().sorted()
							.mapToInt(index->attributes[index]).toArray()
				);
				// fill correspondent sample pairs
				if (minimalElements.contains(key)) {
					Collection<SamplePair> samplePairs = family.get(key);
					if (samplePairs==null)	family.put(key, samplePairs=new LinkedList<>());
					samplePairs.add(entry.getKey());
				}
			});
			return new SamplePairFamily(family);
		}
		
		/**
		 * Basic support for Active Sample Selection based Attribute Reduction(for streaming
		 * data).
		 * 
		 * @author Benjamin_L
		 */
		public static class Incremental {
			/**
			 * Check if the given <code>new Universe instance</code> is useful for the
			 * <code>Equivalence Classes</code>
			 * 
			 * @param equClasses
			 * 		{@link EquivalenceClass} {@link Collection} without the <code>new Universe
			 * 		instance </code>: <strong>U/C</strong>.
			 * @param newInstance
			 * 		The new {@link Instance} that is gonna be added into the
			 * 		<code>equClasses</code> and to check: <strong>x</strong>.
			 * @return <code>true</code> if the <code>new Universe</code> make a difference if
			 * 		adding into the <code>Equivalent Classes</code>. / <code>False</code> if it
			 * 		doesn't and can be absorbed.
			 */
			public static boolean insIsUseful(
					Map<IntArrayKey, EquivalenceClass> equClasses, Instance newInstance
			) {
				// key = C(x)
				IntArrayKey key = new IntArrayKey(newInstance.getConditionAttributeValues());
				// Search in U/C.
				EquivalenceClass equClass = equClasses.get(key);
				//	if exists
				if (equClass!=null) {
					// if h.dec=='/'
					if (equClass.getDecision()==null) {
						return false;
					}else {
						// if D(x)==h.dec
						if (equClass.getDecision()==newInstance.getAttributeValue(0)) {
							return false;
						}
					}
				}
				//	else doesn't exist
				return true;
			}
			
			/**
			 * Get {@link SamplePair}s that meets criteria as w<sub>1</sub>:
			 * <p>
			 * <pre>w<sub>1</sub>={ (x[i], x[j]): x[i] in ([x]<sub>C</sub>-{x}), x[j] in (U-
			 * POS<sub>U</sub>(C,D)} }</pre>
			 * <p>
			 * where <code>[x]<sub>C</sub></code> is the equivalence class(in U/C) that share
			 * the same condition description as x;
			 * 
			 * @param instances
			 * 		An {@link Instance} {@link Collection} of the equivalent class(in U/C) that
			 * 		share the same condition description as x: <strong>[x]<sub>C</sub>-{x}</strong>.
			 * @param equClasses
			 * 		Equivalent classes of the <code>universes</code>: <strong>U/C</strong>
			 * @return w<sub>1</sub> in an unmodifiable {@link SamplePair} {@link HashSet}.
			 */
			public static Collection<SamplePair> w1(
				Collection<Instance> instances, Collection<EquivalenceClass> equClasses
			) {
				// w1 = null
				final Collection<SamplePair> w1 = new LinkedList<>();
				// Loop x[i] in X
				for (Instance instance: instances) {
					// Loop h in equClasses
					for (EquivalenceClass equClass: equClasses) {
						// if h.dec=='/': U - POSc(D)
						if (equClass.getDecision()==null) {
							// for any x[j] in h
							//	w1.add(x[j]).
							for (Instance innerInstance: equClass.getUniverses())
								w1.add(new SamplePair(instance, innerInstance));
						}
					}
				}
				return new HashSet<>(w1);
			}

			/**
			 * Get {@link Instance} with conflicted decision values or in equivalence classes
			 * with multiple label values:
			 * w<sub>2</sub>={x[i]: x[i] in U - POS<sub>U∪{x}</sub>(C, D) or x[i] in
			 * POS<sub>U∪{x}</sub>(C, D), d(x)!=d(x[i])}
			 * 
			 * @param instance
			 * 		The {@link Instance} to examine: <strong>x[i] &isin; U</strong>
			 * @param equClasses
			 * 		Equivalent classes of the <code>universes</code>: <strong>U/C</strong>
			 * @return w<sub>2</sub> in {@link Instance} {@link Collection}.
			 */
			public static Collection<Instance> w2(
					Instance instance, Map<IntArrayKey, EquivalenceClass> equClasses
			) {
				// w2 = null
				final Collection<Instance> w2 = new LinkedList<>();
				// Loop over x in equivalent class
				for (EquivalenceClass equClass: equClasses.values()) {
					// if h.dec=='/' or h.dec!='/' && D(x) != h.dec
					if (equClass.getDecision()==null || 
						(equClass.getDecision()!=null &&
						 equClass.getDecision()!=instance.getAttributeValue(0))
					) {
						// Loop over x[j] in h
						// 	w2 = w2 U j
						w2.addAll(equClass.getUniverses());
					}
				}
				return w2;
			}
		
			/**
			 * Get the combined two Sample Pairs with Sample Pair sets(KP) as values and the 
			 * correspondent Minimal Element set(ME).
			 * <p>
			 * No modification will be made in the two sample pair families of the given arguments.
			 * Return result is a new {@link Map} with new {@link LinkedList} as values for
			 * containing {@link SamplePair}s.
			 * <p>
			 * Combination bases on Minimal Element sets(ME) as keys to combine Sample Pairs.
			 * 
			 * @param samplePairFamily1
			 * 		A Sample Pair Family in Map with Minimal Element set(ME) as keys and Sample
			 * 		Pair sets(KP) as values: <strong>{ ME: KP }</strong>
			 * @param samplePairFamily2
			 * 		Another Sample Pair Family in Map: <strong>{ ME: KP }</strong>
			 * @return Combined Sample Pair Family(ME+KP).
			 */
			public static Map<IntArrayKey, Collection<SamplePair>> combineSamplePairFamilies(
					Map<IntArrayKey, Collection<SamplePair>> samplePairFamily1, 
					Map<IntArrayKey, Collection<SamplePair>> samplePairFamily2
			){
				final Map<IntArrayKey, Collection<SamplePair>> more, less;
				if (samplePairFamily1.size()>samplePairFamily2.size()) {
					more = samplePairFamily1;
					less = samplePairFamily2;
				}else {
					more = samplePairFamily2;
					less = samplePairFamily1;
				}
				final Map<IntArrayKey, Collection<SamplePair>> comb = new HashMap<>(
						samplePairFamily1.size()+samplePairFamily2.size()
				);

				more.entrySet().stream().forEach(entry->
						comb.put(entry.getKey(), new LinkedList<>(entry.getValue()))
				);

				less.entrySet().stream().forEach(entry->{
					Collection<SamplePair> samplePairs = comb.get(entry.getKey());
					if (samplePairs==null)	comb.put(entry.getKey(), new LinkedList<>(entry.getValue()));
					else					samplePairs.addAll(entry.getValue());
				});
				return comb;
			}
		
			/**
			 * Minimal Elements updating was implemented based on the paper 
			 * <a href="https://ieeexplore.ieee.org/document/7492272">
			 * "Active Sample Selection Based Incremental Algorithm for Attribute Reduction With
			 * Rough Sets"</a> by Yanyan Yang, Degang Chen, Hui Wang.
			 * <p>
			 * <strong>NOTICE:</strong>
			 * <p>
			 * However, in program testing, a problem in this implementation concerns with the
			 * positive region of the reduct has encountered and <strong>remains unsolved</strong>,
			 * here are some details:
			 * <p>
			 * When testing Sample Pair Selection based Attribute Reduction(for <strong>static
			 * data</strong>) and Active Sample Pair based Attribute Reduction(for <strong>incoming
			 * data</strong>) on the dataset <a href="http://archive.ics.uci.edu/ml/datasets/SPECT+
			 * Heart"><strong>SPECT.train </strong></a> (available on UCI datset repository), the
			 * dataset was split into <i>5</i> parts(the number of instance that each part consists
			 * of is <strong>[54, 54, 53, 53, 53]) </strong> and the <u>order/sequence</u> of the
			 * instances remained unchanged.
			 * <p>
			 * The <strong>1st part</strong> was inputed into the <u>Sample Pair Selection based
			 * Attribute Reduction</u> to generate the <strong>initial Minimal Elements</strong>
			 * (marked as M*) and to search for a reduct based on <i>M*</i>. For this part, the
			 * <strong>core</strong> it found was [13, 22](i.e. a<sub>13</sub>, a<sub>22</sub>)
			 * and the correspondent sample pair selections were c<sub>1</sub>=[13],
			 * k<sub>1</sub>=[(x<sub>9</sub>, x<sub>11</sub>)],
			 * c<sub>2</sub>=[22], k<sub>2</sub>=[(x<sub>11</sub>, x<sub>27</sub>)]. The <strong>
			 * reduct</strong> it found was [1, 3, 11, 13, 16, 19, 22](both reduct before inspection
			 * and after inspection, i.e. no redundant attributes in the reduct).
			 * |POS<sub>U</sub>(C, D)|=|POS<sub>U</sub>(reduct, D)|=45.
			 * <p>
			 * However, based on the reduct and <i>M*</i>, when inputing a new instance
			 * x<sub>64</sub> from the <strong>2nd part</strong> of the split data into the
			 * <u>Active Sample Pair Selection based Attribute Reduction</u>, the <i>ME'</i>(i.e.
			 * updated Minimal elements) it calculated which doesn't contains {c<sub>i</sub>=[22],
			 * kp<sub>i</sub>=[(x<sub>11</sub>, x<sub>27</sub>)]} for the minimal element [22] had
			 * been removed because the consistency of [x]<sub>C</sub> which only contains
			 * x<sub>27</sub> and x<sub>64</sub> had changed into in-consistent (where
			 * d(x<sub>27</sub>)=0 and d(x<sub>64</sub>)=1). With no further minimal elements
			 * contains a<sub>22</sub>, {x<sub>6</sub>(d=1), x<sub>8</sub>(d=1), x<sub>53</sub>(d=0)}
			 * and {x<sub>17</sub>(d=0), x<sub>40</sub>(d=1)} could no longer be discerned using
			 * <i>ME'</i> in reduct inspection.
			 * <p>
			 * In the attribute reduction algorithm, the reduct before inspection using <i>ME'</i>
			 * was [1, 3, 11, 13, 16, 19, 22](where POS<sub>U∪{x}</sub>(C, D)=POS<sub>U∪{x}</sub>
			 * (reduct, D)=44), and the one after inspection was [1, 3, 11, 13, 16, 19] where 22
			 * was removed(where POS<sub>U∪{x}</sub>(reduct, D)=39).
			 * <p>
			 * For convenience, look at x<sub>17</sub> and x<sub>40</sub>(both in POS<sub>U∪{x}</sub>
			 * (C, D)). Attributes that discern the two instances are [2, 7, 10, 12, 21, 22], which
			 * means a<sub>22</sub> should not be removed in the inspection.
			 * <p>
			 * The same problem occurs when performing the above executions on the dataset
			 * <a href="http://archive.ics.uci.edu/ml/datasets/UJIIndoorLoc"><strong>UJIIndoorLoc
			 * </strong> </a> (available on UCI dataset repository) (with 1st column deleted and
			 * MDLP discretization performed) which was split into 6 parts.
			 * <p>
			 * Anyway, to respect the originality of the paper, no changes were made when
			 * implementing the two algorithms.
			 * <p>
			 * <strong>Statement: </strong>
			 * Still, the problem may also was caused by my misunderstanding of the two papers and
			 * the pseudo-codes or me implementing with bugs. Please feel free to correct me if it
			 * was.
			 * 
			 * @author Benjamin_L
			 */
			public static class MinimalElementsUpdating {
				/**
				 * A class for {@link MinimalElementsUpdating#execute(Map, Instance, Map, IntegerIterator)}.
				 * 
				 * @author Benjamin_L
				 */
				private static class PreviousMinimalElements{					
					/**
					 * Update the given <code>familyPlus</code>(i.e. <strong>M*</strong>) for the
					 * {@link EquivalenceClass} where the new instance(i.e. <code>newInstance</code>)
					 * is has changed <strong>from Consistent into in-Consistent</strong>.
					 * <p>
					 * <strong>Case 3</strong> in <a href="https://ieeexplore.ieee.org/document/
					 * 7492272">"Active Sample Selection Based Incremental Algorithm for Attribute
					 * Reduction With Rough Sets"</a> by Yanyan Yang, Degang Chen, Hui Wang
					 * 
					 * @param originalFamily
					 * 		A Sample Pair Family set in {@link Map} with minimal elements as keys
					 * 		and the correspondent sample pairs as values: <strong>M*</strong>.
					 * @param newInstance
					 * 		The new {@link Instance} added: <strong>x</strong>
					 * @param equClass
					 * 		A {@link EquivalenceClass} that with the same condition attribute values
					 * 		as <code>newInstance</code>: <strong>[x]<sub>C</sub>-{x}</strong>
					 * @param equClasses
					 * 		{@link EquivalenceClass}es of the previous {@link Instance}s:
					 * 		<strong>U/C</strong>
					 * @param attributes
					 * 		Attributes of {@link Instance}. (Starts from 1): <strong>C</strong>
					 * @return A Sample Pair Family mark as "<strong>M</strong>" in
					 * 		<i>algorithm 2</i> in the paper.
					 */
					private static Map<SamplePair, Collection<Integer>> 
						updateFamily4ChangedEquivalentClass(
							Map<IntArrayKey, Collection<SamplePair>> originalFamily,
							Instance newInstance, EquivalenceClass equClass,
							Map<IntArrayKey, EquivalenceClass> equClasses, 
							IntegerIterator attributes
					){
						// w1 = calculateW1
						Collection<SamplePair> w1 = 
								Incremental
									.w1(equClass.getUniverses(), equClasses.values());
						// Loop over p[i] in K*
						Entry<IntArrayKey, Collection<SamplePair>> originalFamilyItem;
						Iterator<Entry<IntArrayKey, Collection<SamplePair>>> originalFamilyIterator =
								originalFamily.entrySet().iterator();
						while (originalFamilyIterator.hasNext()) {
							originalFamilyItem = originalFamilyIterator.next();
							//	p[i] = p[i] - w1
							originalFamilyItem.getValue().removeAll(w1);
							
							//	M* = M* - { c[i] in M*: p[i] is empty }
							if (originalFamilyItem.getValue().isEmpty()) {
								originalFamilyIterator.remove();
							}
						}
						// Initiate M
						Map<SamplePair, Collection<Integer>> m = new HashMap<>();
						// Loop over h in equClasses(U/C)
						//	compute c(x, x[i]) = {a in C, a(x)!=a(x[i])}
						//	where x[i] in POS<sub>U ∪ {x}</sub>(C, D)
						IntArrayKey newInstanceKey = 
							new IntArrayKey(newInstance.getConditionAttributeValues());
						for (EquivalenceClass h: equClasses.values()) {
							// Skip the Equivalent Class that contains {x}, for its consistency is changed
							//	into in-consistent in case 3.
							if (new IntArrayKey(
									h.getUniverses().iterator().next().getConditionAttributeValues()
								).equals(newInstanceKey)
							) {
								continue;
							}
							
							// If h.dec != '/': POS<sub>U ∪ {x}</sub>(C, D)
							if (h.getDecision()!=null) {
								// Loop over a in C
								attributes.reset();
								for (int i=0; i<attributes.size(); i++) {
									int attr = attributes.next();
									// if a(x)!=a(h)
									if (newInstance.getAttributeValue(attr)!=h.getAttributeValue(attr)) {
										// Loop x[j] in h
										for (Instance universe: h.getUniverses()) {
											// add the sample pair (x, x[j]) and the correspondent attribute a into M.
											SamplePair samplePair = new SamplePair(newInstance, universe);
											Collection<Integer> discernAttributes = m.get(samplePair);
											if (discernAttributes==null)	m.put(samplePair, discernAttributes=new HashSet<>(attributes.size()-i));
											discernAttributes.add(attr);
										}
									}
								}
							}
						}
						return m;
					}
					
					/**
					 * Update the given <code>familyPlus</code>(i.e. <strong>M*</strong>) for the
					 * <code>newInstance</code> who serves as an {@link EquivalenceClass}.
					 * <p>
					 * <strong>Case 4</strong> in <a href="https://ieeexplore.ieee.org/document/
					 * 7492272">"Active Sample Selection Based Incremental Algorithm for Attribute
					 * Reduction With Rough Sets"</a> by Yanyan Yang, Degang Chen, Hui Wang.
					 *
					 * @param newInstance
					 * 		The new {@link Instance} added: <strong>x</strong>
					 * @param equClasses
					 * 		{@link EquivalenceClass}es of the previous {@link Instance}s:
					 * 		<strong>U/C</strong>
					 * @param attributes
					 * 		Attributes of {@link Instance}. (Starts from 1): <strong>C</strong>
					 * @return A Sample Pair Family mark as "<strong>M</strong>" in <i>algorithm
					 * 		4.2</i> in the paper.
					 */
					private static Map<SamplePair, Collection<Integer>> 
						updateFamily4NewlyEquivalentClass(
							Instance newInstance, Map<IntArrayKey, EquivalenceClass> equClasses,
							IntegerIterator attributes
					){
						// Initiate M
						Map<SamplePair, Collection<Integer>> m = new HashMap<>();
						// Create an h and add into U/C.
						// calculate w2.
						Collection<Instance> w2 =
							Incremental
								.w2(newInstance, equClasses);
						// Loop over x[i] in w2
						for (Instance ins: w2) {
							// Loop over a in C
							attributes.reset();
							for (int i=0; i<attributes.size(); i++) {
								int attr = attributes.next();
								// if a(x[i]) != a(x)
								if (ins.getAttributeValue(attr)!=newInstance.getAttributeValue(attr)) {
									// M = M U c(|U|+1, i)=a
									SamplePair samplePair = new SamplePair(ins, newInstance);
									Collection<Integer> discernAttributes = m.get(samplePair);
									if (discernAttributes==null)	m.put(samplePair, discernAttributes = new HashSet<>(attributes.size()-i));
									discernAttributes.add(attr); 
								}
							}
						}
						return m;
					}
				}
				
				/**
				 * Transform a newly sample pair family from the given <code>m</code>:
				 * <ul>
				 * 	<li>1. Loop until <code>m</code> is empty;</li>
				 * 	<li>2. Select the minimal element with min. attributes(<code>c(x,
				 * 			x[i<sub>0</sub>]) </code>);</li>
				 * 	<li>3. Update <i>M**</i> and <i>KP**</i>, delete elements in <code>m</code>
				 * 			(<i>M</i>)</li>
				 * </ul>
				 * 
				 * @param m
				 * 		A Sample Pair Family in {@link Map}: <strong>M</strong>
				 * @return A Sample Pair Family marked as "<strong>M**</strong>" & "<strong>KP**
				 * 		</strong>"in the paper.
				 */
				private static Map<IntArrayKey, Collection<SamplePair>> 
					transformMinimalElementsFromPreviousToNew(
							Map<SamplePair, Collection<Integer>> m
				) {
					Map<IntArrayKey, Collection<SamplePair>> mTransformed = new HashMap<>();
					m.entrySet().forEach(entry->{
						IntArrayKey key = new IntArrayKey(
								entry.getValue().stream().sorted().mapToInt(v->v).toArray()
						);
						Collection<SamplePair> samplePairs = mTransformed.get(key);
						if (samplePairs==null)	mTransformed.put(key, samplePairs=new LinkedList<>());
						samplePairs.add(entry.getKey());
					});
					// M**=null, KP**=null
					Map<IntArrayKey, Collection<SamplePair>> familyPlusPlus = new HashMap<>();
					// while(M!=null)
					Collection<SamplePair> samplePairs;
					while (!mTransformed.isEmpty()) {
						// c(x, x[i[0]]) = min{|c(x, x[i])|: c(x, x[i]) in M}
						Entry<IntArrayKey, Collection<SamplePair>> minEntry =
								mTransformed.entrySet().stream()
											.min((entry1, entry2)->entry1.getKey().getKey().length-entry2.getKey().getKey().length)
											.get();
						// M** = M** U c(x, x[i[0]]), p** = (x, x[i[0]])
						samplePairs = familyPlusPlus.get(minEntry.getKey());
						if (samplePairs==null)	familyPlusPlus.put(minEntry.getKey(), samplePairs=new LinkedList<>());
						// Loop over c(x, x[i]) in M
						Entry<IntArrayKey, Collection<SamplePair>> mEntry;
						Iterator<Entry<IntArrayKey, Collection<SamplePair>>> mEntryIterator = mTransformed.entrySet().iterator();
						while (mEntryIterator.hasNext()) {
							mEntry = mEntryIterator.next();
							// if c(x, x[i[0]]) all in c(x, x[i])
							boolean fit = mEntry==minEntry;
							if (!fit) {
								final Collection<Integer> mEntryKeySet = 
										Arrays.stream(mEntry.getKey().getKey())
												.boxed()
												.collect(Collectors.toSet());
								fit = Arrays.stream(minEntry.getKey().key())
											.allMatch(c->mEntryKeySet.contains(c));
							}
							if (fit) {
								// if c(x, x[i[0]]) == c(x, x[i])
								if (minEntry==mEntry) {
									// p** = p** U {(x, x[i])}
									samplePairs.addAll(mEntry.getValue());
								}
								// c(x, x[i]) = null
								mEntryIterator.remove();
							}
						}
					}
					return familyPlusPlus;
				}
				
				private static Map<IntArrayKey, Collection<SamplePair>>
					finalizeUpdatedMinimalElements(
						Map<IntArrayKey, Collection<SamplePair>> originalFaimly,		// M*
						Map<IntArrayKey, Collection<SamplePair>> family4NewInstance		// M**
				) {
					// ME'=M* U M**, KP'=K* U KP**
					//	M*, K*: familyPlus; M**, K**: familyPlusPlus
					Map<IntArrayKey, Collection<SamplePair>> updatedFamily = 
						Incremental
							.combineSamplePairFamilies(originalFaimly, family4NewInstance);
					// Loop over c[i]** in M**
					for (IntArrayKey minimalElePlusPlus: family4NewInstance.keySet()) {
						Collection<Integer> mePlusPlus = 
								Arrays.stream(minimalElePlusPlus.getKey())
										.boxed()
										.collect(Collectors.toSet());
						//	Loop over c[j] in M*
						for (IntArrayKey minimalEle: originalFaimly.keySet()) {
							Collection<Integer> me = 
								Arrays.stream(minimalEle.getKey())
										.boxed()
										.collect(Collectors.toSet());
							// if c[i]** all in c[j]
							if (me.size()>mePlusPlus.size() && me.containsAll(mePlusPlus)) {
								// ME' = ME' - c[j] and KP' = KP' - p[j].
								updatedFamily.remove(minimalEle);
								continue;
							}
							// if c[j] all in c[i]**
							if (mePlusPlus.size()>me.size() && mePlusPlus.containsAll(me)) {
								// ME' = ME' - c[i]** and KP' = KP' - p[i]**.
								updatedFamily.remove(minimalElePlusPlus);
								continue;
							}
							// if c[j] == c[i]**
							//	ME' = ME' - c[j] and KP' = KP' - p[i]**.
							//	p[j] = p[j] ∪ p[i]**
							// meaning put all elements in p[i]** and p[j] into KP'.
						}
					}
					return updatedFamily;
				}
				
				/**
				 * Get updated Minimal Elements and correspondent Sample Pairs selection for
				 * incremental feature selection.
				 * <p>
				 * <strong>Notice</strong>: implementation of Algorithm 4.2 in 
				 * <a href="https://ieeexplore.ieee.org/document/7492272">
				 * "Active Sample Selection Based Incremental Algorithm for Attribute Reduction
				 * With Rough Sets"</a>
				 * 
				 * @param samplePairFamily
				 * 		Previous {@link SamplePairFamily} before adding new universe instance in
				 * 		{@link Map}. With minimum elements in {@link IntArrayKey} as keys and
				 * 		correspondent SamplePalr {@link Collection}s as values: <strong>{ ME: KP }
				 * 		</strong>
				 * @param newInstance
				 * 		The new {@link Instance} to be added: <strong>x</strong>
				 * @param equClasses
				 * 		{@link EquivalenceClass}es of previous {@link Instance} {@link Collection}:
				 * 		<strong>U/C</strong>
				 * @param attributes
				 * 		Attributes of {@link Instance}.
				 * @return {@link SamplePair} collection map.
				 */
				public static Map<IntArrayKey, Collection<SamplePair>> execute(
						Map<IntArrayKey, Collection<SamplePair>> samplePairFamily, 
						Instance newInstance, Map<IntArrayKey, EquivalenceClass> equClasses,
						IntegerIterator attributes
				) {
					// M*=ME, K*=KP.
					Map<IntArrayKey, Collection<SamplePair>> familyPlus = 
						SamplePairs
							.copySamplePairFamilyMap(samplePairFamily);//*/
					// Load M
					Map<SamplePair, Collection<Integer>> m = null;
					//	Search [x]c-{x'} in U/C
					IntArrayKey equClassKey = new IntArrayKey(
							newInstance.getConditionAttributeValues()
					);
					EquivalenceClass equClass = equClasses.get(equClassKey);
					// if [x]c-{x'} exists in U/C.
					if (equClass!=null) {
						// if |d([x]c-{x'})|==1 && |d([x]c)|>1 (i.e. d([x]c-{x'})!=d(x)): Case 3
						if (equClass.getDecision()!=null && 
							equClass.getDecision()!=newInstance.getAttributeValue(0)
						) {
							m = PreviousMinimalElements
									.updateFamily4ChangedEquivalentClass(
										familyPlus, newInstance, equClass, 
										equClasses, attributes
									);
						}else {
							throw new RuntimeException("Unimplemented situation.");
						}
					// Case 4
					}else {
						m = PreviousMinimalElements
								.updateFamily4NewlyEquivalentClass(
									newInstance, equClasses, attributes
								);
					}
					// Load M**, KP**
					Map<IntArrayKey, Collection<SamplePair>> familyPlusPlus = 
							MinimalElementsUpdating
								.transformMinimalElementsFromPreviousToNew(m);
					
					// ME'=M* U M**, KP'=K* U KP**
					//	M*, K*: familyPlus; M**, K**: familyPlusPlus
					Map<IntArrayKey, Collection<SamplePair>> finalFamily = 
							MinimalElementsUpdating
								.finalizeUpdatedMinimalElements(familyPlus, familyPlusPlus);
					return finalFamily;
				}
			}
		
			/**
			 * Get the equivalence class where the new universe is from the {@link Instance}
			 * Collection.
			 * 
			 * @param instances
			 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
			 * @param attributes
			 * 		Attributes of {@link Instance} to partition for equivalence classes.
			 * @param newIns
			 * 		The new {@link Instance}: <strong>x</strong>
			 * @return The {@link EquivalenceClass} where the new universe is.
			 */
			public static EquivalenceClass equivalenceClassWithUniverse(
					Collection<Instance> instances, IntegerIterator attributes,
					Instance newIns
			) {
				// key = red(x)
				int[] redValuesOfNewU = new int[attributes.size()];
				attributes.reset();
				for (int i=0; i<redValuesOfNewU.length; i++)
					redValuesOfNewU[i] = newIns.getAttributeValue(attributes.next());
				// dec = d(x)
				EquivalenceClass equClass = new EquivalenceClass();
				equClass.addUniverse(newIns);
				equClass.setDecision(newIns.getAttributeValue(0));
				// Loop x[i] in U
				int[] redValueOfU;
				for (Instance universe: instances) {
					// if red(x[i])=key
					redValueOfU = new int[attributes.size()];
					attributes.reset();
					for (int i=0; i<redValueOfU.length; i++)
						redValueOfU[i] = universe.getAttributeValue(attributes.next());
					if (Arrays.equals(redValuesOfNewU, redValueOfU)) {
						// X = X U x[i]
						equClass.addUniverse(universe);
						// if X.dec=='/', continue
						// else if X.dec!=D(x[i]), X.dec='/'
						if (equClass.getDecision()!=null &&
							equClass.getDecision()!=universe.getAttributeValue(0)
						) {
							equClass.setDecision(null);
						}
					}
				}
				return equClass;
			}
		
			/**
			 * Check if two equivalent classes are equals. Meaning the size of universe instances
			 * they contain should be equal, and the elements they contain should be the same ones.
			 * 
			 * @param equClass1
			 * 		An Equivalent Class.
			 * @param equClass2
			 * 		Another Equivalent Class.
			 * @return <code>true</code> if the two equivalent classes are equal.
			 */
			public static boolean equivalenceClassesEquals(
					Collection<Instance> equClass1, Collection<Instance> equClass2
			) {
				// If two equivalent classes are equal, their size must be the same.
				if (equClass1.size()==equClass2.size()) {
					// If two equivalent classes are equal, they must contain the same elements.
					Collection<Instance> universeSet = equClass2 instanceof HashSet?
														equClass2: new HashSet<>(equClass2);
					return universeSet.containsAll(equClass1);
				}else {
					return false;
				}
			}
		}
	}
	
	/**
	 * Separate Core from the given minimal elements in the given {@link SamplePairSelectionResult}.
	 * <p>
	 * For Core, the size of minimal element is 1. For the rest of the minimal elements, they are 
	 * sorted bases on the size of attributes in the minimal element.
	 * 
	 * @param samplePairSelection
	 * 		{@link SamplePairSelectionResult} instance.
	 * @param attributeLength
	 * 		The length of attributes in {@link Instance}.
	 * @return Core and the rest of the minimal elements in a {@link Collection} array. For core,
	 * 		the result is wrap in an {@link Integer} {@link Collection}. For the rest of the
	 * 		minimal elements, they are in a {@link List} of {@link Integer} {@link Collection}s.
	 */
	public static Collection<?>[] separateCoreNOtherAttributes(
			SamplePairSelectionResult samplePairSelection, int attributeLength
	){
		// Core: C, where |C.attributes| = 1
		Collection<Integer> core = core(samplePairSelection, attributeLength);
		// M* - C', where |C'.attributes|> 1
		List<Collection<Integer>> seperatedSamplePairSelection = 
			samplePairSelection.getSamplePairSelectionMap().values().stream()
							.filter(collection->collection.size()>1)
							.sorted((c1, c2)->-(c1.size()-c2.size()))
							.collect(Collectors.toList());
		return new Collection[] {core, seperatedSamplePairSelection};
	}
		
	/**
	 * Get the core based on selected attributes from {@link SamplePairSelectionResult}.
	 * 
	 * @param samplePairSelection
	 * 		{@link SamplePairSelectionResult}
	 * @param hashCapacity
	 * 		Capacity for core attributes in an {@link Integer} {@link Collection}.
	 * @return Attributes as core.
	 */
	public static Collection<Integer> core(
			SamplePairSelectionResult samplePairSelection, int hashCapacity
	){
		Collection<Integer> core = new HashSet<>(hashCapacity);
		for (Collection<Integer> collection: samplePairSelection.getSamplePairSelectionMap().values()) {
			if (collection.size()==1)	core.add(collection.iterator().next());
		}
		return core;
	}
	
	/**
	 * Inspections for the 2 algorithms.
	 * 
	 * @author Benjamin_L
	 */
	public static class Inspection {
		/**
		 * Execute inspection bases on the paper <a href="https://ieeexplore.ieee.org/document/
		 * 7492272">"Active Sample Selection Based Incremental Algorithm for Attribute Reduction
		 * With Rough Sets"</a> by Yanyan Yang, Degang Chen, Hui Wang.
		 * <p>
		 * Inspection uses previous reduct(i.e. <code>a</code> and <code>previousReduct</code>),
		 * attributes added into the reduct(i.e. <code>newReduct</code>) and up-to-date minimal
		 * elements.
		 * <p>
		 * <strong>Notice</strong>: <code>a</code> and <code>previousReduct</code> should be
		 * <strong>2 different instances</strong> pointing at <strong>2 different
		 * {@link Collection}s</strong> respectively if <code>newReduct</code> is <code>null</code>.
		 * 
		 * @param a
		 * 		A {@link Collection} of {@link Instance}'s attributes as the previous reduct.
		 * @param previousReduct
		 * 		A {@link Collection} of {@link Instance}'s attributes as the previous reduct.
		 * @param newReduct
		 * 		A {@link Collection} of {@link Instance}'s attributes that added to the reduct.
		 * 		<code>null</code> if using <code>previousReduct</code> only in the calculation of
		 * 		the intersection between <code>previousReduct</code> and elements in
		 * 		<code>minimalElements</code>. Otherwise, it will be mixed in with
		 * 		<code>previousReduct</code> in the intersection calculations.
		 * @param minimalElements
		 * 		Up-to-date minimal elements.
		 * @return Inspected reduct: <strong>(<code>previousReduct</code> - <i>redundancy</i>) ∪ 
		 * 			<code>newReduct</code></strong>.
		 */
		public static Collection<Integer> execute(
				int[] a, Collection<Integer> previousReduct, Collection<Integer> newReduct,
				Collection<IntArrayKey> minimalElements
		) {
			// red = red<sub>x</sub> if B(<code>newReduct</code>) is null else red<sub>x</sub> ∪ B.
			Collection<Integer> reduct;
			if (newReduct==null || newReduct.isEmpty()) {
				reduct = previousReduct;
			}else {
				reduct = new HashSet<>(previousReduct.size()+newReduct.size());
				reduct.addAll(previousReduct);
				reduct.addAll(newReduct);
			}
			// Loop over <code>a</code> and check redundant attributes.
			LoopA:
			for (int attr: a) {
				// Get red - {a}
				if (!reduct.remove(attr))
					throw new RuntimeException("Fail to remove attribute "+attr+" in reduct: "+reduct);
				// Loop over minimal elements in ME
				//	Check if red - {a} ∩ c' is null, where c' is a minimal element in ME'.
				//	If all of them(c') is NOT null: remove {a} 
				for (IntArrayKey me: minimalElements) {
					if (Basic.Mathematicals.intersectionOf(me.getKey(), reduct).isEmpty()) {
						// do not remove {a}
						reduct.add(attr);
						continue LoopA;
					}
				}
				// remove {a}
			}
			return reduct;
		}
		
		/**
		 * Execute using {@link FeatureImportance4ActiveSampleSelection} for positive region
		 * calculations.
		 * 
		 * @param <Sig>
		 * 		Type of feature subset significance.
		 * @param universes
		 * 		{@link Instance} {@link Collection}.
		 * @param reduct
		 * 		The reduct to be inspected.
		 * @param calculation
		 * 		{@link FeatureImportance4ActiveSampleSelection} instance.
		 * @param sigDeviation
		 * 		Acceptable deviation when calculating significance of attributes. Consider equal
		 * 		when the difference between two sig is less than the given deviation value.
		 * @return inspected reduct.
		 */
		public static <Sig> Collection<Integer> execute(
				Collection<Instance> universes, IntegerIterator reduct,
				FeatureImportance4ActiveSampleSelection<Sig> calculation, Sig sigDeviation
		){
			Sig globalPos = calculation.calculate(Basic.equivalenceClasses(universes, reduct).values())
										.getResult();
			
			LinkedList<Integer> examQueue = new LinkedList<>();
			reduct.reset();	for (int i=0; i<reduct.size(); i++)	examQueue.add(reduct.next());
			// Go through attributes in reduct.
			int originalSize = reduct.size(), examAttr;
			Sig examSig;
			for (int i=0; i<originalSize; i++) {
				examAttr = examQueue.getFirst();
				// if pos(reduct-{a}) has intersection with every element in M*
				//	a is redundant: reduct = reduct - {a}
				examSig = calculation.calculate(Basic.equivalenceClasses(universes, new IntegerCollectionIterator(examQueue)).values())
									.getResult();
				if (calculation.value1IsBetter(globalPos, examSig, sigDeviation))
					examQueue.addLast(examAttr);
			}
			return examQueue;
		}//*/
	}

}