package featureSelection.repository.algorithm.alg.roughEquivalenceClassBased;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.RoughEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.EquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;
import featureSelection.repository.entity.alg.rec.extension.incrementalDecision.MostSignificantAttributeResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.SignificantAttributeClassPack;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.StatisticResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.Shrink4RECBoundaryClassSetStays;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision.ShrinkInput4RECIncrementalDecisionExtension;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision.ShrinkResult4RECIncrementalDecisionExtension;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision.Shrink4RECBasedDecisionMapExt;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Algorithm repository of <strong>Rough Equivalence Class based extension algorithms(REC)</strong>
 * using <i>Rough Equivalence Classes</i>, including:
 * <ul>
 * 	<li><strong>S-REC</strong>:
 * 		<p><code>Real-time counting</code> algorithm published in paper:
 * 			<a href="https://www.sciencedirect.com/science/article/pii/S0020025520302723">NEC:
 * 			A nested equivalence class-based dependency calculation approach for fast feature
 * 			selection using rough set theory</a>. In the paper, it has been re-named as Nested
 * 			Equivalence Class(NEC).
 * 		<p>Simple (Counting) Rough Equivalence Class based. 2 counting strategies have been
 * 	    	implemented for the counting of positive region size when calculating positive
 * 	    	region/dependency: <i>last counting</i>, <i>real-time counting</i>.
 * 		<p>For <i>last counting</i>, the <code>1-REC</code> number counting is performed after
 * 			the partitioning of Equivalence Classes/Rough Equivalence Classes.
 * 		<p>For <i>real-time counting</i>, the <code>1-REC</code> number counting is performed
 * 	        during the partitioning.
 * 	</li>
 * 	<li><strong>IP-REC</strong>:
 * 	    <i>Deprecated</i> by {@link NestedEquivalenceClassBasedAlgorithm.IncrementalPartition}
 * 		<p>Incremental Partition Rough Equivalence Class based. Using un-repeated partial
 * 		    attributes in Equivalence Classes/Rough Equivalence Classes partitionings and repeat
 * 	    	until all given attributes are used. For this strategy, during partitioning, only
 * 	    	<code>0-REC</code>s are necessary for further partitioning using other attributes.
 * 	    	Which means that not all {@link Instance}s are required in every partitioning using
 * 	    	an attribute. So by partitioning on less {@link Instance}s, the total time efficiency
 * 	    	is expected to be accelerated especially in high dimensional calculations.
 * 	</li>
 * 	<li><strong>ID-REC</strong>:
 * 		<p>Algorithm published in paper:
 * 			<a href="https://www.sciencedirect.com/science/article/pii/S0031320320303204">
 * 			Accelerating information entropy-based feature selection using rough set theory with
 * 			classified nested equivalence classes</a>. In the paper, it has been re-named as
 * 			Classified Nested Equivalence Classes(C-NEC).
 * 		<p>Incremental Decision Rough Equivalent Class based. Using
 * 			{@link EquivalenceClassDecMapXtension} and {@link RoughEquivalenceClassDecMapXtension}
 * 			in calculations of entropy to accelerate.
 * 	</li>
 * </ul>
 * <p><strong>Notice</strong>: Later at the designing of Rough Equivalent Class based extension
 * 		algorithms, the name of Rough Equivalent Class has been renamed to <strong>Nested
 * 		Equivalence Class</strong> due to some reasons. However, the implemented algorithms
 * 		here are not affected.
 * 
 * @see NestedEquivalenceClassBasedAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class RoughEquivalenceClassBasedExtensionAlgorithm
	extends RoughEquivalenceClassBasedAlgorithm
{
	/**
	 * <strong>S-REC</strong> for short.
	 * <p>
	 * <code>Real-time counting</code> algorithm published in the paper:
	 * <a href="https://www.sciencedirect.com/science/article/pii/S0020025520302723">NEC: A nested 
	 * equivalence class-based dependency calculation approach for fast feature selection using 
	 * rough set theory</a>.
	 * 
	 * @author Benjamin_L
	 */
	public static class SimpleCounting {
		/**
		 * Algorithms for S-REC using last counting strategy: 
		 * <p>Count positive region numbers <strong>AFTER</strong> finish partitioning (Rough)
		 * Equivalence Classes.
		 * 
		 * @author Benjamin_L
		 */
		public static class LastCounting {
			public static class Basic {
				/**
				 * Count the size of positive region({@link ClassSetType#POSITIVE}).
				 * 
				 * @param equClasses 
				 * 		{@link EquivalenceClass}es.
				 * @param attributes
				 * 		Attributes of {@link Instance}s.(Starts from 1)
				 * @return the size of positive region.
				 */
				public static int countPositiveRegion(
						Collection<EquivalenceClass> equClasses, IntegerIterator attributes
				) {
					IntArrayKey key;
					int[] keyArray, counting;
					Map<IntArrayKey, int[]> posCounts = new HashMap<>();
					// Loop over equivalence classes
					for (EquivalenceClass equClass: equClasses) {
						keyArray = new int[attributes.size()];
						attributes.reset();
						while (attributes.hasNext()) {
							keyArray[attributes.currentIndex()] =
									equClass.getAttributeValueAt(attributes.next() - 1);
						}
						key = new IntArrayKey(keyArray);

						if (!posCounts.containsKey(key)) {
							// if key doesn't exist, initiate one.
							counting = equClass.sortable()?
									new int[] {equClass.getDecisionValue(), equClass.getInstanceSize()}:
									null;
							posCounts.put(key, counting);
						}else {
							// if key already exists & 1-REC, update.
							counting = posCounts.get(key);
							if (counting!=null) {
								if (equClass.sortable() && counting[0]==equClass.getDecisionValue()) {
									// Consistent
									counting[1] += equClass.getInstanceSize();
								}else if (!equClass.sortable() || equClass.getDecisionValue()!=counting[0]) {
									// In-consistent
									posCounts.replace(key, null);
								}
							}
						}
					}
					// pos=0
					int pos = 0;
					// Go through h in hp.
					//	for any h that h.cnst=1
					//		count pos
					for (int[] val : posCounts.values()) {
						if (val!=null){
							pos += val[1];
						}
					}
					return pos;
				}

				/**
				 * Inspection for reduct to remove redundant attributes.
				 * 
				 * @param equClasses
				 * 		An {@link EquivalenceClass} {@link Collection}.
				 * @param attributes
				 * 		Reduct attributes.
				 * @return Inspected reduct attributes.
				 */
				public static List<Integer> inspection(
					Collection<EquivalenceClass> equClasses, IntegerIterator attributes
				){
					int globalPos = countPositiveRegion(equClasses, attributes);
					
					List<Integer> red = new LinkedList<>();	
					attributes.reset();
					for (int i=0; i<attributes.size(); i++){
						red.add(attributes.next());
					}
							
					int examAttr, examPos = 0;
					for (int i=red.size()-1; i>=0; i--) {
						examAttr = red.remove(i);
						examPos = countPositiveRegion(equClasses, new IntegerCollectionIterator(red));
						if (examPos != globalPos){
							// Not redundant.
							red.add(i, examAttr);
						}
					}
					return red;
				}
			}
			
			/**
			 * Obtain the core attributes of the Equivalence classes based on positive region.
			 * 
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param globalPos
			 * 		The global positive region number.
			 * @param attributes
			 * 		Attributes of {@link Instance}. (Starts from 1)
			 * @return Core attributes. (Could be empty)
			 */
			public static List<Integer> core(
				Collection<EquivalenceClass> equClasses, int globalPos, int...attributes
			){
				// Initiate
				List<Integer> core = new LinkedList<>();
				// Go through attributes as the one removed, and use the reset of the attributes to 
				// partition.
				int posSize;
				int[] extracted = new int[attributes.length-1];
				for (int i=0; i<extracted.length; i++){
					extracted[i] = attributes[i+1];
				}
				for (int i=0; i<attributes.length; i++) {
					posSize = Basic.countPositiveRegion(equClasses, new IntegerArrayIterator(extracted));
					if (globalPos-posSize>0){
						core.add(attributes[i]);
					}
					// Next attribute
					if (i<extracted.length)	{
						extracted[i] = attributes[i];
					}
				}
				return core;
			}
			
			/**
			 * Get the most significant attribute by rough equivalence classes and attribute.
			 * <p>
			 * Using the original strategy of REC.
			 * 
			 * @param equClass
			 * 		An {@link EquivalenceClass} collection.
			 * @param red
			 * 		Attributes reduced.(Starts from 1)
			 * @param attributes
			 * 		All attribute indexes.(Starts from 1)
			 * @return the most significant attribute/ -1 if empty zero rough equivalence class,
			 * 			and the correspondent positive region size as significance.
			 */
			public static int[] mostSignificantAttribute(
					Collection<EquivalenceClass> equClass,
					Collection<Integer> red, IntegerIterator attributes
			){
				int maxPos = 0, sigAttr = -1, subPos;
				int[] examAttr = new int[red.size()+1];
				int r=0;	for (int v : red)	examAttr[r++] = v;

				int attr;
				attributes.reset();
				while (attributes.hasNext()) {
					attr = attributes.next();
					if (red.contains(attr))	continue;
					// Get the positive region size of partitioning.
					examAttr[examAttr.length-1] = attr;
					subPos = Basic.countPositiveRegion(equClass, new IntegerArrayIterator(examAttr));
					if (subPos>maxPos || sigAttr==-1) {
						// update max positive region value and the attribute.
						maxPos = subPos;
						sigAttr = attr;
					}
				}
				return new int[] {sigAttr, maxPos};
			}//*/
		}
		
		/**
		 * Algorithms for S-REC using real-time counting strategy: 
		 * <p>Count positive region numbers <strong>DURING</strong> the partitioning of (Rough)
		 * Equivalence Classes.
		 * 
		 * @author Benjamin_L
		 */
		public static class RealTimeCounting {
			/**
			 * Obtain the core attributes of the Equivalence classes based on positive region.
			 * 
			 * @param <Sig>
			 * 		{@link Number} implemented type as the value of Significance.
			 * @param insSize
			 * 		The number of {@link Instance}.
			 * @param calculation
			 * 		Implemented {@link RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation}.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider equal
			 * 		when the difference between two sig is less than the given deviation value.
			 * @param equClass
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param globalSig
			 * 		The global positive region number.
			 * @param attributes
			 * 		Attributes of {@link Instance}. (Starts from 1)
			 * @return Core attributes. (Could be empty)
			 */
			public static <Sig extends Number> List<Integer> core(
					int insSize,
					RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
					Sig sigDeviation, 
					Collection<EquivalenceClass> equClass, Sig globalSig,
					int...attributes
			){
				List<Integer> core = new LinkedList<>();
				// Loop over attributes and removed each respectively, use the reset of the
				//  attributes to partition
				Sig examSig;
				int[] extracted = new int[attributes.length-1];
				for (int i=0; i<extracted.length; i++){
					extracted[i] = attributes[i+1];
				}
				for (int i=0; i<attributes.length; i++) {
					examSig = calculation.calculate(equClass, new IntegerArrayIterator(extracted), insSize)
										.getResult();
					// Check for core attributes
					if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
						core.add(attributes[i]);
					}
					// Next attribute
					if (i<extracted.length){
						extracted[i] = attributes[i];
					}
				}
				return core;
			}
		
			/**
			 * Obtain the current least significant attribute.
			 * <p>
			 * Using the original strategy of REC.
			 * 
			 * @param calculation
			 * 		{@link RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation}
			 * 		instance.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider equal
			 * 		when the difference between two sig is less than the given deviation value.
			 * @param insSize
			 * 		The size of {@link Instance}s.
			 * @param reduct
			 * 		A reduct.(Starts from 1)
			 * @param attributes
			 * 		Attributes of {@link Instance}. (Starts from 1)
			 * @return the least significant attribute./ -1 if empty zero rough equivalence class.
			 *         And the significance value.
			 */
			public static <Sig extends Number> Number[] leastSignificantAttribute(
					RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
					Sig sigDeviation, 
					int insSize, Collection<EquivalenceClass> equClass, Collection<Integer> reduct,
					int...attributes
			){
				int sigAttr = -1;
				Sig minSig = null, subSig;
				int[] examAttr = new int[reduct.size()+1];
				int r=0;	for (int v : reduct)	examAttr[r++] = v;
				for (int i=0; i<attributes.length; i++) {
					if (reduct.contains(attributes[i]))	continue;
					examAttr[examAttr.length-1] = attributes[i];
					// Do partitioning and get the significance.
					subSig = calculation.calculate(equClass, new IntegerArrayIterator(examAttr), insSize)
										.getResult();
					if (calculation.value1IsBetter(minSig, subSig, sigDeviation) || sigAttr==-1) {
						minSig = subSig;
						sigAttr = attributes[i];
					}
				}
				return new Number[] {sigAttr, minSig};
			}
			
			/**
			 * Obtain the current most significant attribute.
			 * <p>
			 * Using the original strategy of REC.
			 * 
			 * @param calculation
			 * 		{@link RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation}
			 * 		instance.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider equal
			 * 		when the difference between two sig is less than the given deviation value.
			 * @param insSize
			 * 		The number of {@link Instance}s.
			 * @param equClass
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param reduct
			 * 		A reduct.(Starts from 1)
			 * @param attributes
			 * 		Attributes of {@link Instance}. (Starts from 1)
			 * @return the most significant attribute./ -1 if empty zero rough equivalence class.
			 *         And the significance.
			 */
			public static <Sig extends Number> Number[] mostSignificantAttribute(
					RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
					Sig sigDeviation, 
					int insSize, Collection<EquivalenceClass> equClass, Collection<Integer> reduct,
					int...attributes
			){
				int sigAttr = -1;
				Sig maxSig = null, subSig;
				int[] examAttr = new int[reduct.size()+1];
				int r=0;	for (int v : reduct)	examAttr[r++] = v;
				for (int i=0; i<attributes.length; i++) {
					if (reduct.contains(attributes[i]))	continue;
					examAttr[examAttr.length-1] = attributes[i];
					// Do partitioning and get the significance.
					subSig = calculation.calculate(equClass, new IntegerArrayIterator(examAttr), insSize)
										.getResult();
					if (calculation.value1IsBetter(subSig, maxSig, sigDeviation) || sigAttr==-1) {
						maxSig = subSig;
						sigAttr = attributes[i];
					}
				}
				return new Number[] {sigAttr, maxSig};
			}
		
			/**
			 * Inspection for reduct to remove redundant attributes.
			 * 
			 * @param <Sig>
			 * 		{@link Number} implemented type as the value of Significance.
			 * @param insSize
			 * 		The size of {@link Instance}s.
			 * @param calculation
			 * 		Implemented {@link RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation}.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider equal
			 * 		when the difference between two sig is less than the given deviation value.
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param attributes
			 * 		Reduct attributes. (Starts from 1)
			 * @return Inspected reduct attributes.
			 */
			public static <Sig extends Number> Collection<Integer> inspection(
					int insSize,
					RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
					Sig sigDeviation, Collection<EquivalenceClass> equClasses, int...attributes
			){
				// Calculate global significance.
				Sig globalPos = calculation.calculate(equClasses, new IntegerArrayIterator(attributes), insSize)
											.getResult();
				LinkedList<Integer> reduct = new LinkedList<>();
				for (int i=0; i<attributes.length; i++){
					reduct.add(attributes[i]);
				}
						
				Sig examPos;
				int examAttr;
				for (int i=attributes.length-1; i>=0; i--) {
					examAttr = reduct.removeLast();
					examPos = calculation.calculate(equClasses, new IntegerCollectionIterator(reduct), insSize)
										.getResult();
					if (calculation.value1IsBetter(globalPos, examPos, sigDeviation)) {
						// Not redundant
						reduct.addFirst(examAttr);
					}
				}
				return reduct;
			}
			
			/**
			 * Inspection for reduct to remove redundant attributes.
			 * 
			 * @param <Sig>
			 * 		{@link Number} implemented type as the value of Significance.
			 * @param insSize
			 * 		The size of {@link Instance}s.
			 * @param calculation
			 * 		Implemented {@link RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation}.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider equal
			 * 		when the difference between two sig is less than the given deviation value.
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param reduct
			 * 		Reduct attributes. (Starts from 1)
			 */
			public static <Sig extends Number> void inspection(
					int insSize,
					RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
					Sig sigDeviation, Collection<EquivalenceClass> equClasses,
					Collection<Integer> reduct
			){
				// Calculate global significance.
				Sig globalPos = calculation.calculate(equClasses, new IntegerCollectionIterator(reduct), insSize)
											.getResult();
				Collection<Integer> redSet = new HashSet<>(reduct);
						
				Sig examPos;
				int examAttr;
				Iterator<Integer> iterator = reduct.iterator();
				while (iterator.hasNext()) {
					redSet.remove(examAttr = iterator.next());
					examPos = calculation.calculate(equClasses, new IntegerCollectionIterator(redSet), insSize)
										.getResult();
					if (!calculation.value1IsBetter(globalPos, examPos, sigDeviation)) {
						iterator.remove();
					}else {
						redSet.add(examAttr);
					}
				}
			}
		}
	}
	
	/**
	 * <strong>IP-REC</strong> for short.
	 * 
	 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition
	 * 
	 * @deprecated Still could be used, but future major updates will be made in
	 * 				<code>IP-NEC</code> instead. --2019.5.11 (at the early stage of
	 * 				implementing <code>IP-NEC</code>) by Benjamin_L
	 * 
	 * @author Benjamin_L
	 */
	public static class IncrementalPartition {

		public static class Basic {
			/**
			 * Wrap as a rough equivalence class that contains only 1 element which is the given
			 * {@link EquivalenceClass} {@link Collection}.
			 *
			 * @see RoughEquivalenceClassDummy
			 * 
			 * @param equClasses
			 * 		An {@link EquivalenceClass} {@link Collection}.
			 * @return A {@link Collection} of {@link RoughEquivalenceClassDummy}s
			 */
			public static Collection<RoughEquivalenceClassDummy> wrapEquivalenceClasses(
					Collection<EquivalenceClass> equClasses
			){
				EquivalenceClass equClass;
				RoughEquivalenceClassDummy roughClass;
				
				Iterator<EquivalenceClass> equClassIterator = equClasses.iterator();
				
				Collection<RoughEquivalenceClassDummy> roughClasses = new HashSet<>();
				roughClasses.add(
						roughClass=new RoughEquivalenceClassDummy(equClass=equClassIterator.next())
				);
				
				while (equClassIterator.hasNext()) {
					equClass = equClassIterator.next();
					switch (equClass.getType()) {
						// 1-REC
						case POSITIVE:
							// if decision values conflict: 1-REC -> 0-REC
							if (Integer.compare(equClass.getDecisionValue(), roughClass.getDecision())!=0) {
								roughClass.setType(ClassSetType.BOUNDARY);
								roughClass.setDecision(-1);
							}
							// else: 1-REC, do nothing.
							break;
						// -1-REC
						case NEGATIVE:
							// if class set types conflict: -1-REC & 1-REC
							if (equClass.sortable()) {
								// -1-REC -> 0-REC
								roughClass.setType(ClassSetType.BOUNDARY);
							}
							// else do nothing.
							break;
						default: 
							break;
					}
					roughClass.addClassItem(equClass);
				}
				return roughClasses;
			}

			
			/**
			 * Count positive regions by counting {@link EquivalenceClass}es whose
			 * {@link EquivalenceClass#getType()} is {@link ClassSetType#POSITIVE} after the
			 * partitioning of an extra attribute.
			 * 
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param attribute
			 * 		An extra attribute for partitioning.
			 * @return A {@link StatisticResult} instance.
			 */
			public static StatisticResult<Collection<RoughEquivalenceClassDummy>>
				calculateEquivalenceClassPositiveRegionAfterPartition(
					Collection<EquivalenceClass> equClasses, int attribute
			) {
				final Map<Integer, RoughEquivalenceClassDummy> roughEquClasses =
						new HashMap<>(equClasses.size());
				
				int pos=0, key;
				boolean emptyBoundary = true;
				RoughEquivalenceClassDummy roughEquClass;
				for (EquivalenceClass equClass: equClasses) {
					// obtain the value of equClass with the given attribute.
					key = equClass.getAttributeValueAt(attribute-1);
					roughEquClass = roughEquClasses.get(key);
					// if rough equivalence classes doesn't contains key.
					if (roughEquClass==null) {
						// Initiate a rough equivalence class and add into it.
						roughEquClasses.put(key, roughEquClass=new RoughEquivalenceClassDummy(equClass));
						// count positive region
						if (equClass.sortable()){
							pos += equClass.getItemSize();
						}
					}else {
						switch (roughEquClass.getType()) {
							// 1-REC
							case POSITIVE:
								// if decision values conflict: 1-REC -> 0-REC
								if (Integer.compare(equClass.getDecisionValue(), roughEquClass.getDecision())!=0) {
									if (emptyBoundary)	emptyBoundary = false;
									roughEquClass.setType(ClassSetType.BOUNDARY);
									roughEquClass.setDecision(-1);
									// maintain pos by minus instance number of this rough
									// equivalence class.
									pos -= roughEquClass.getInstanceSize();
								// else update positive region
								}else {
									// Count positive region.
									pos += equClass.getItemSize();
								}
								break;
							// -1-REC
							case NEGATIVE:
								// if class set type conflict: -1-REC -> 0-REC 
								if (equClass.sortable()) {
									if (emptyBoundary)	emptyBoundary = false;
									roughEquClass.setType(ClassSetType.BOUNDARY);
								}
								break;
							// 0-REC
							default: 
								// do nothing.
								break;
						}
						roughEquClass.addClassItem(equClass);
					}
				}
				// return
				return new StatisticResult<>(pos, emptyBoundary, roughEquClasses.values());
			}

			/**
			 * Count positive regions by counting {@link EquivalenceClass}es whose
			 * {@link EquivalenceClass#getType()} is {@link ClassSetType#POSITIVE} after the
			 * partitioning of extra attributes.
			 * 
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param attributes
			 * 	    Extra attributes for partitioning.
			 * @return {@link StatisticResult} instance.
			 */
			public static StatisticResult<Collection<RoughEquivalenceClassDummy>>
				calculateEquivalenceClassPositiveRegionAfterPartition(
					Collection<EquivalenceClass> equClasses, IntegerIterator attributes
			) {
				final Map<IntArrayKey, RoughEquivalenceClassDummy> roughEquClasses =
						new HashMap<>(equClasses.size());
				
				int pos=0;
				int[] code;
				IntArrayKey key;
				boolean emptyBoundary = true;
				RoughEquivalenceClassDummy record;
				for (EquivalenceClass equClass: equClasses) {
					// Create key
					key = new IntArrayKey(code=new int[attributes.size()]);
					attributes.reset();
					for (int i=0; i<attributes.size(); i++) {
						code[i] = equClass.getAttributeValueAt(attributes.next() - 1);
					}
					record = roughEquClasses.get(key);
					// if rough equivalence classes doesn't contains key.
					if (record==null) {
						// Initiate a rough equivalence class and add into it.
						roughEquClasses.put(key, record=new RoughEquivalenceClassDummy(equClass));
						// Update positive region
						if (equClass.sortable())	pos += equClass.getItemSize();
					}else {
						switch (record.getType()) {
							// 1-REC
							case POSITIVE:
								// if decision values conflict: 1-REC -> 0-REC
								if (Integer.compare(equClass.getDecisionValue(), record.getDecision())!=0) {
									if (emptyBoundary)	emptyBoundary = false;
									record.setType(ClassSetType.BOUNDARY);
									record.setDecision(-1);
									// maintain pos by minus instance number of this rough
									// equivalence class.
									pos -= record.getInstanceSize();
								// else count positive region
								}else {
									pos += equClass.getItemSize();
								}
								break;
							// -1-REC
							case NEGATIVE:
								// if class set type conflict: -1-REC -> 0-REC 
								if (equClass.sortable()) {
									if (emptyBoundary)	emptyBoundary = false;
									record.setType(ClassSetType.BOUNDARY);
								}
								break;
							// 0-REC
							default: 
								// do nothing.
								break;
						}
						record.addClassItem(equClass);
					}
				}
				return new StatisticResult<>(pos, emptyBoundary, roughEquClasses.values());
			}
			
			
			/**
			 * Count positive regions by counting {@link RoughEquivalenceClassDummy}es whose
			 * {@link RoughEquivalenceClassDummy#getType()} is {@link ClassSetType#POSITIVE} after
			 * the further partitioning of an extra attribute.
			 * <p>
			 * For further(/incremental) partitioning, only <strong>0-RECs</strong>(
			 * {@link ClassSetType#BOUNDARY}) are meaningful for 1-RECs & -1-RECs would generate no
			 * more 1-REC after further partitioning. Which means the size of positive region is not
			 * affected by them but 0-NECs only.
			 * 
			 * @param roughEquClasses
			 * 		A {@link Collection} of {@link RoughEquivalenceClassDummy}s.
			 * @param attribute
			 * 	    An attribute for further partitioning.
			 * @return {@link StatisticResult} instance.
			 */
			public static StatisticResult<Collection<RoughEquivalenceClassDummy>>
				calculateRoughEquivalenceClassPosPartition(
					Collection<RoughEquivalenceClassDummy> roughEquClasses, int attribute
			) {
				final Collection<RoughEquivalenceClassDummy> incRoughEquClasses = new LinkedList<>();
				// pos = 0
				int pos = 0;
				boolean emptyBoundary = true;
				// Loop over rough equivalence classes in 0-REC
				for (RoughEquivalenceClassDummy roughClass: roughEquClasses) {
					StatisticResult<Collection<RoughEquivalenceClassDummy>> statisticResult =
							calculateEquivalenceClassPositiveRegionAfterPartition(
									roughClass.getItems(), attribute
							);
					// Collect further(/incremental) partitioning rough equivalence classes.
					incRoughEquClasses.addAll(statisticResult.getRecord());
					// Count the positive region.
					pos += statisticResult.getPositiveRegion();
					if (emptyBoundary && !statisticResult.isEmptyBoundaryClassSet()) {
						emptyBoundary = false;
					}
				}
				return new StatisticResult<>(pos, emptyBoundary, incRoughEquClasses);
			}
			
			
			/**
			 * Boundary sensitive rough equivalence classes partitioning. Comparing to normal
			 * partitioning, this method returns <code>null</code> once a 0-REC is obtained
			 * in partitioning. If partitioning is finished with no 0-REC generated, return
			 * the partitioned {@link RoughEquivalenceClassDummy}s.
			 *
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param attributes
			 * 		Attributes of {@link Instance} for partition.
			 * @param hashKeyCapacity
			 * 		Capacity for initiation of <code>HashMap</code> whose keys attribute values
			 * 		and values are Rough Equivalent Classes.
			 * 		<code>capacity = 0.75 * sizeOf(Equivalence Class)</code> is recommended based
			 * 	    on the recommendations in Java {@link HashMap}.
			 * @return <code>null</code> if partition result contains {@link ClassSetType#BOUNDARY}
			 *          {@link RoughEquivalenceClassDummy}. / {@link RoughEquivalenceClassDummy}
			 * 			{@link Collection}.
			 */
			public static Collection<RoughEquivalenceClassDummy>
				boundarySensitiveRoughEquivalenceClass(
					Collection<EquivalenceClass> equClasses, IntegerIterator attributes,
					int hashKeyCapacity
			){
				final Map<IntArrayKey, RoughEquivalenceClassDummy> incRoughClasses =
						new HashMap<>(hashKeyCapacity);
				int[] code;
				IntArrayKey key;
				RoughEquivalenceClassDummy roughClassValue;
				for (EquivalenceClass equClass: equClasses) {
					// obtain equivalent key for the given attributes
					attributes.reset();
					code=new int[attributes.size()];
					for (int i=0; i<attributes.size(); i++) {
						code[i] = equClass.getAttributeValueAt(attributes.next() - 1);
					}
					key=new IntArrayKey(code);
					roughClassValue = incRoughClasses.get(key);
					
					// If rough equivalence classes don't contain key.
					if (roughClassValue==null) {
						// create a rough equivalence class.
						incRoughClasses.put(key, roughClassValue=new RoughEquivalenceClassDummy(equClass));
					// Else rough equivalence classes contain key
					}else {
						// if 1-REC && equClass.cons=true && no conflict between decision values: 1-REC
						if (ClassSetType.POSITIVE.equals(roughClassValue.getType()) &&
							equClass.sortable() && 
							equClass.getDecisionValue()==roughClassValue.getDecision()
						) {
							// 1-REC
							// do nothing
						// else if -1-REC && equClass.cons=false:
						}else if (
							ClassSetType.NEGATIVE.equals(roughClassValue.getType()) &&
							!equClass.sortable()
						) {
							// -1-REC
							// do nothing
						}else {
							// 0-REC
							return null;
						}
						roughClassValue.addClassItem(equClass);
					}
				}
				return incRoughClasses.values();
			}


			/**
			 * Partition the given {@link EquivalenceClass}es incrementally using 1 attribute at
			 * each partitioning. Return immediately if 0-REC is empty.
			 * 
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param attributes
			 * 		Attributes to be used in partition in order.
			 * @return {@link PartitionResult} instance.
			 */
			public static PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>>
				inTurnIncrementalPartition(
					Collection<EquivalenceClass> equClasses, IntegerIterator attributes
			) {
				Collection<Integer> usedAttributes = new LinkedList<>();
				// Partition using 1 attribute each time.
				attributes.reset();
				int targetAttribute = attributes.next();
				StatisticResult<Collection<RoughEquivalenceClassDummy>> statisticResult =
						calculateEquivalenceClassPositiveRegionAfterPartition(
								equClasses, 
								targetAttribute
						);
				int pos = statisticResult.getPositiveRegion();
				usedAttributes.add(targetAttribute);

				// if |0-REC|=0, partitioning is "finished" for the positive regions are obtained.
				// (The partitioning is not finished strictly, however, the size of positive region
				//  is settle)
				if (statisticResult.isEmptyBoundaryClassSet() || 
					usedAttributes.size()==attributes.size()
				) {
					return new PartitionResult<>(
							pos, 
							statisticResult.isEmptyBoundaryClassSet(),
							usedAttributes,
							statisticResult.getRecord()
						);
				}else {
					// Keep performing incremental partitioning on rough equivalence classes
					//  until the size of positive region is settle. i.e. |0-REC|=0.
					boolean loopBoundary;
					Collection<RoughEquivalenceClassDummy> roughClasses;
					Collection<RoughEquivalenceClassDummy> roughClassesInLine =
							statisticResult.getRecord();
					do {
						loopBoundary = false;
						roughClasses = new LinkedList<>();
						targetAttribute = attributes.next();
						for (RoughEquivalenceClassDummy roughClass: roughClassesInLine) {
							switch(roughClass.getType()) {
								case BOUNDARY: 
									if (!loopBoundary)	loopBoundary=true;
									// Partition.
									statisticResult = 
										calculateEquivalenceClassPositiveRegionAfterPartition(
											roughClass.getItems(),
											targetAttribute
										);
									// Count the positive region.
									pos += statisticResult.getPositiveRegion();
									// if 0-REC is not empty after partition, collect for next round.
									if (!statisticResult.isEmptyBoundaryClassSet()) {
										roughClasses.addAll(statisticResult.getRecord());
									}
									break;
								default:
									break;
							}
						}
						// Collect used attributes.
						if (loopBoundary)	usedAttributes.add(targetAttribute);
						else				break;
						roughClassesInLine = roughClasses;
					// If no 0-REC left or no more attributes, partitioning is finish.
					} while (!roughClasses.isEmpty() && attributes.hasNext());
					return new PartitionResult<>(
							pos, 
							roughClasses.isEmpty(),
							usedAttributes,
							roughClasses
						);
				}
			}
			
			/**
			 * Partition the given {@link EquivalenceClass}es incrementally using partial attributes
			 * at each partitioning(by applying {@link AttributeProcessStrategy}). Return immediately
			 * if 0-REC is empty.
			 *
			 * @param attributeProcessStrategy
			 * 		An {@link AttributeProcessStrategy} instance.
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @return {@link PartitionResult} instance, results includes: 
			 * 		<code>positive region number</code>, 
			 * 		<code>whether positive region has reached global one</code>,
			 * 		<code>attributes used in actual partition</code>, 
			 * 		<code>partitioned {@link RoughEquivalenceClassDummy} {@link Collection}</code>
			 */
			public static PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>>
				dynamicIncrementalPartition(
					AttributeProcessStrategy attributeProcessStrategy,
					Collection<EquivalenceClass> equClasses
			){
				Collection<Integer> usedAttributes = new LinkedList<>();
				int attributesSize = attributeProcessStrategy.attributeLength();
				// Partition using partial attributes.
				int[] partitionAttribute = attributeProcessStrategy.next();
				StatisticResult<Collection<RoughEquivalenceClassDummy>> statisticResult =
						calculateEquivalenceClassPositiveRegionAfterPartition(
								equClasses, 
								new IntegerArrayIterator(partitionAttribute)
						);				
				int pos = statisticResult.getPositiveRegion();
				for (int attr: partitionAttribute)	usedAttributes.add(attr);

				// if |0-REC|=0, partitioning is "finished" for the positive regions are obtained.
				// (The partitioning is not finished strictly, however, the size of positive region
				//  is settle)
				if (statisticResult.isEmptyBoundaryClassSet() || 
					usedAttributes.size()==attributesSize
				) {
					return new PartitionResult<>(
							pos, 
							statisticResult.isEmptyBoundaryClassSet(),
							usedAttributes,
							statisticResult.getRecord()
						);
				}else {
					// Keep performing incremental partitioning on rough equivalence classes
					//  until the size of positive region is settle. i.e. |0-REC|=0.
					Collection<RoughEquivalenceClassDummy> roughClasses;
					Collection<RoughEquivalenceClassDummy> roughClassesInLine = statisticResult.getRecord();
					do {
						partitionAttribute = null; 
						roughClasses = new LinkedList<>();
						for (RoughEquivalenceClassDummy roughClass: roughClassesInLine) {
							switch(roughClass.getType()) {
								case BOUNDARY: 
									// Initiate partition attributes.
									if (partitionAttribute==null)
										partitionAttribute=attributeProcessStrategy.next();
									// Partition.
									statisticResult = 
										calculateEquivalenceClassPositiveRegionAfterPartition(
											roughClass.getItems(),
											new IntegerArrayIterator(partitionAttribute)
										);
									// Calculate positive region.
									pos += statisticResult.getPositiveRegion();
									// if 0-REC is not empty after partition.
									if (!statisticResult.isEmptyBoundaryClassSet())
										roughClasses.addAll(statisticResult.getRecord());
									break;
								default:
									break;
							}
						}
						// Collect used attributes.
						if (partitionAttribute!=null) {	
							for (int attr: partitionAttribute){
								usedAttributes.add(attr);
							}
						}
						roughClassesInLine = roughClasses;
						// If no 0-REC left or no more attributes, partitioning is finished.
					} while (!roughClasses.isEmpty() && usedAttributes.size()<attributesSize);
					return new PartitionResult<>(
							pos, 
							roughClasses.isEmpty(),
							usedAttributes,
							roughClasses
						);
				}
			}
		}

		/**
		 * Algorithms for obtaining core for IP-REC.
		 */
		public static class Core {
			/**
			 * <strong>Continuity</strong> based strategy for Core seeking. 
			 * <p>
			 * Compute and seek core attributes. While encountering |{@link ClassSetType#BOUNDARY}|=0
			 * without using (in-line) attributes in partitioning, confirms that examining
			 * attributes are not core attributes and <strong>continue</strong> the next round of
			 * using (in-line) attributes in group for partitioning.
			 *
			 * @see ContinuityBased#compute(AttrProcessStrategy4Comb, AttributeProcessStrategy,
			 *      Collection, Shrink4RECBoundaryClassSetStays)
			 * @see RecursionBased
			 * 
			 * @author Benjamin_L
			 */
			public static class ContinuityBased {
				/**
				 * Compute and obtain Core.
				 * 
				 * @see #compute(Collection, AttrProcessStrategy4Comb, AttributeProcessStrategy,
				 *      Collection, Shrink4RECBoundaryClassSetStays)
				 * 
				 * @param coreAttributeProcessStrategy
				 * 		{@link AttrProcessStrategy4Comb} instance.
				 * @param incPartitionAttributeProcessStrategy
				 * 		Implemented {@link AttributeProcessStrategy} as incremental partitioning
				 * 		attribute group process strategy.
				 * @param equClasses
				 * 		A {@link Collection} of {@link EquivalenceClass}es.
				 * @param shrinkInstance
				 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
				 * @return core attributes.
				 */
				public static Collection<Integer> compute(
						AttrProcessStrategy4Comb coreAttributeProcessStrategy,
						AttributeProcessStrategy incPartitionAttributeProcessStrategy,
						Collection<EquivalenceClass> equClasses,
						Shrink4RECBoundaryClassSetStays shrinkInstance
				) {
					return compute(
								new HashSet<>(),
								coreAttributeProcessStrategy,
								incPartitionAttributeProcessStrategy,
								equClasses,
								shrinkInstance
							);
				}
				
				/**
				 * Self-designed core obtaining procedure for IP-REC.
				 * <p>
				 * Use the given {@link AttrProcessStrategy4Comb} to group attributes in bulks for
				 * incremental partition during core seeking process.
				 * <p>
				 * Use the given {@link AttributeProcessStrategy} for incremental partitioning.
				 * <p>
				 * The idea is that, if a group of attributes are not core, then partitioning without
				 * them would not affect the positive region(i.e. |0-REC|=0), meaning non-core
				 * attributes could be identified by bulk instead of being checked one by one.
				 * However, if a core attribute is inside of the bulk, then it does generate 0-REC.
				 * In this case, further partitioning as well as checking is required.
				 *
				 * @param core
				 * 		Current core {@link Collection} which contains attributes that are sure
				 * 		to be within Reduct. <code>new HashSet()</code> for initialization.
				 * @param coreAttributeProcessStrategy
				 * 		Implemented {@link AttrProcessStrategy4Comb} instance.
				 * @param incPartitionAttributeProcessStrategy
				 * 		Implemented {@link AttributeProcessStrategy} instance as incremental partition 
				 * 		attribute process strategy as groups.
				 * @param equClasses
				 * 		{@link EquivalenceClass} {@link Collection}.
				 * @param shrinkInstance
				 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
				 * @return Core attributes.
				 */
				private static Collection<Integer> compute(
						Collection<Integer> core, 
						AttrProcessStrategy4Comb coreAttributeProcessStrategy,
						AttributeProcessStrategy incPartitionAttributeProcessStrategy,
						Collection<EquivalenceClass> equClasses,
						Shrink4RECBoundaryClassSetStays shrinkInstance
				) {
					// initiate pointer: begin = 0, end = m-1
					// Loop until all attributes are checked
					int[] attributeGroup;
					int hashKeyCapacity = equClasses.size();
					PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
					while (coreAttributeProcessStrategy.hasNext()) {
						// Extract attributes from begin to end: B
						attributeGroup = coreAttributeProcessStrategy.getInLineAttr();
						// Use the rest of the attributes to do 1st round of partitioning: U/(C-B)
						partitionResult = Basic.dynamicIncrementalPartition(
											incPartitionAttributeProcessStrategy.initiate(
												new IntegerArrayIterator(attributeGroup)
											), 
											equClasses
										);
						// if |0-REC|=0, attributes of begin~end are NOT core attributes.
						if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
							// update pointers begin and end.
							coreAttributeProcessStrategy.skipExamAttributes();
							coreAttributeProcessStrategy.updateInLineAttrs();
						// else continue to partition for checking attributes within groups.
						}else {
							// Remain 0-RECs only for further partitioning.
							shrinkInstance.shrink(partitionResult.getRoughClasses());
							// continue to check for core attributes.
							Common.traditionalCoreExam4CoreAttributeProcessStrategy(
									core, coreAttributeProcessStrategy, 
									partitionResult.getRoughClasses(), 
									hashKeyCapacity
							);
						}
					}
					return core;
				}
			}
			
			/**
			 * <strong>Recursion</strong> based strategy for Core seeking. 
			 * <p>
			 * Compute and seek core attributes. While encountering |{@link ClassSetType#BOUNDARY}|=0
			 * without using (in-line) attributes in partitioning, confirms that examining
			 * attributes are not core attributes and <strong>continue</strong> the next round of
			 * using (in-line) attributes in group for partitioning.
			 * 
			 * @see ContinuityBased
			 * 
			 * @author Benjamin_L
			 */
			public static class RecursionBased {
				/**
				 * Compute and obtain Core.
				 *
				 * @see RecursionBased#compute(Collection, AttrProcessStrategy4Comb,
				 *      AttributeProcessStrategy, Collection, Shrink4RECBoundaryClassSetStays)
				 * 
				 * @param coreAttributeProcessStrategy
				 * 		An {@link AttrProcessStrategy4Comb} instance.
				 * @param incPartitionAttributeProcessStrategy
				 * 		Implemented {@link AttributeProcessStrategy} instance as incremental
				 * 		partition attribute process strategy as groups.
				 * @param equClasses
				 * 		A {@link Collection} of {@link EquivalenceClass}es.
				 * @param shrinkInstance
				 * 		{@link Shrink4RECBoundaryClassSetStays}
				 * @return core attributes.
				 */
				public static Collection<Integer> compute(
						AttrProcessStrategy4Comb coreAttributeProcessStrategy,
						AttributeProcessStrategy incPartitionAttributeProcessStrategy,
						Collection<EquivalenceClass> equClasses,
						Shrink4RECBoundaryClassSetStays shrinkInstance
				) {
					return compute(
								new HashSet<>(), 
								coreAttributeProcessStrategy,
								incPartitionAttributeProcessStrategy,
								equClasses,
								shrinkInstance
							);
				}

				/**
				 * Self-designed core obtaining procedure for IP-REC.
				 * <p>
				 * Use the given {@link AttrProcessStrategy4Comb} to group attributes in bulks for
				 * incremental partition during core seeking process.
				 * <p>
				 * Use the given {@link AttributeProcessStrategy} for incremental partitioning.
				 * <p>
				 * The idea is that, if a group of attributes are not core, then partitioning without
				 * them would not affect the positive region(i.e. |0-REC|=0), meaning non-core
				 * attributes could be identified by bulk instead of being checked one by one.
				 * However, if a core attribute is inside of the bulk, then it does generate 0-REC.
				 * In this case, further partitioning as well as checking is required.
				 *
				 * @see RecursionBased#compute(Collection, Collection, AttrProcessStrategy4Comb,
				 *      AttributeProcessStrategy, Collection, Shrink4RECBoundaryClassSetStays)
				 *
				 * @param core
				 * 		Current core {@link Collection} which contains attributes that are sure
				 * 		to be within Reduct. <code>new HashSet()</code> for initialization.
				 * @param coreAttributeProcessStrategy
				 * 		Implemented {@link AttrProcessStrategy4Comb} instance.
				 * @param incPartitionAttributeProcessStrategy
				 * 		Implemented {@link AttributeProcessStrategy} instance as incremental partition
				 * 		attribute process strategy as groups.
				 * @param equClasses
				 * 		{@link EquivalenceClass} {@link Collection}.
				 * @param shrinkInstance
				 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
				 * @return Core attributes.
				 */
				private static Collection<Integer> compute(
						Collection<Integer> core, 
						AttrProcessStrategy4Comb coreAttributeProcessStrategy,
						AttributeProcessStrategy incPartitionAttributeProcessStrategy,
						Collection<EquivalenceClass> equClasses,
						Shrink4RECBoundaryClassSetStays shrinkInstance
				) {
					return compute(core, null, coreAttributeProcessStrategy, 
							incPartitionAttributeProcessStrategy,
							equClasses, shrinkInstance
						);
				}

				/**
				 * Self-designed core obtaining procedure for IP-REC.
				 * <p>
				 * Use the given {@link AttrProcessStrategy4Comb} to group attributes in bulks for
				 * incremental partition during core seeking process.
				 * <p>
				 * Use the given {@link AttributeProcessStrategy} for incremental partitioning.
				 * <p>
				 * The idea is that, if a group of attributes are not core, then partitioning without
				 * them would not affect the positive region(i.e. |0-REC|=0), meaning non-core
				 * attributes could be identified by bulk instead of being checked one by one.
				 * However, if a core attribute is inside of the bulk, then it does generate 0-REC.
				 * In this case, further partitioning as well as checking is required.
				 *
				 * @see RecursionBased#compute(Collection, Collection, AttrProcessStrategy4Comb,
				 *      AttributeProcessStrategy, Collection, Shrink4RECBoundaryClassSetStays)
				 *
				 * @param core
				 * 		Current core {@link Collection} which contains attributes that are sure
				 * 		to be within Reduct. <code>new HashSet()</code> for initialization.
				 * @param attributesFiltered
				 *      A {@link Collection} of attributes filtered(i.e. attributes have been
				 *      confirmed are not core attributes for sure). / null.
				 * @param coreAttributeProcessStrategy
				 * 		Implemented {@link AttrProcessStrategy4Comb} instance.
				 * @param incPartitionAttributeProcessStrategy
				 * 		Implemented {@link AttributeProcessStrategy} instance as incremental partition
				 * 		attribute process strategy as groups.
				 * @param equClasses
				 * 		{@link EquivalenceClass} {@link Collection}.
				 * @param shrinkInstance
				 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
				 * @return Core attributes.
				 */
				private static Collection<Integer> compute(
						Collection<Integer> core, Collection<Integer> attributesFiltered,
						AttrProcessStrategy4Comb coreAttributeProcessStrategy,
						AttributeProcessStrategy incPartitionAttributeProcessStrategy,
						Collection<EquivalenceClass> equClasses,
						Shrink4RECBoundaryClassSetStays shrinkInstance
				) {
					int[] attributeGroup;
					PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
					while (coreAttributeProcessStrategy.hasNext()) {
						// Extract attributes from begin to end: B
						attributeGroup = coreAttributeProcessStrategy.getInLineAttr();
						// Use the rest of the attributes to do 1st round of partitioning: U/(C-B)
						partitionResult = Basic.dynamicIncrementalPartition(
											incPartitionAttributeProcessStrategy.initiate(
												new IntegerArrayIterator(attributeGroup)
											), 
											equClasses
										);
						// if U/(C-B) has no 0-NEC.
						if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
							// Attributes in B are not Core for sure.
							//	maintain field: attributesFiltered
							//		Initiate
							if (attributesFiltered==null)	attributesFiltered = new LinkedList<>();
							//		Add attributes in examing line
							for (int attr: coreAttributeProcessStrategy.getExamingLineAttr()) {
								attributesFiltered.add(attr);
							}
							//		Add attribute of examining
							attributesFiltered.add(coreAttributeProcessStrategy.getExamingAttr());
							//		Add attributes outside of partitionResult.getAttributes().
							Collection<Integer> partitionAttributes =
									new HashSet<>(partitionResult.getAttributes());
							for (int attr: attributeGroup) {
								if (!partitionAttributes.contains(attr)) {
									attributesFiltered.add(attr);
								}
							}
							//	Recursively do compute().
							return compute(
									core, attributesFiltered,
									coreAttributeProcessStrategy.initiate(
										new IntegerCollectionIterator(partitionResult.getAttributes())
									),
									incPartitionAttributeProcessStrategy,
									equClasses,
									shrinkInstance
								);
						// else U/(C-B) contains 0-NEC
						}else {
							// remove 1-NEC, -1-NEC.
							shrinkInstance.shrink(partitionResult.getRoughClasses());
							Collection<RoughEquivalenceClassDummy> roughClass;
							if (attributesFiltered!=null && !attributesFiltered.isEmpty()) {
								// [Important] Use not-core-for-sure attributes to partition, otherwise
								//	in Common.traditionalCoreExam4CoreAttributeProcessStrategy(),
								//	it is not C-{a} that being calculated.
								final IntegerIterator filteredAttrIterator =
										new IntegerCollectionIterator(attributesFiltered);
								roughClass = partitionResult.getRoughClasses().stream().flatMap(rough->
									Basic.dynamicIncrementalPartition(
										incPartitionAttributeProcessStrategy.initiate(filteredAttrIterator),
										rough.getItems()
									).getRoughClasses().stream()
								).filter(r->ClassSetType.BOUNDARY.equals(r.getType()))
								.collect(Collectors.toList());
							}else {
								roughClass = partitionResult.getRoughClasses();
							}
							
							// Loop over a in B, check C-{a} for a in B.
							Common.traditionalCoreExam4CoreAttributeProcessStrategy(
								core, coreAttributeProcessStrategy, 
								roughClass, equClasses.size()
							);
						}
					}
					return core;
				}
			}

			/**
			 * Common core obtaining method.
			 */
			public static class Common {
				/**
				 * Use traditional core obtaining idea to obtain core. To check each attribute
				 * by removing it and see if 0-REC exists. Checking is performed on each attribute
				 * one by one.
				 * <p>
				 * However, uses {@link Basic#boundarySensitiveRoughEquivalenceClass(Collection,
				 * IntegerIterator, int) in partitioning who returns immediately if a 0-REC is
				 * obtained to speed up the process.
				 *
				 * @param core
				 *      A {@link Collection} to collect the core attributes.
				 * @param coreAttributeProcessStrategy
				 *      An {@link AttrProcessStrategy4Comb} instance.
				 * @param roughClasses
				 *      A {@link Collection} of {@link RoughEquivalenceClassDummy} to be
				 *      further(/incremental) partitioned.
				 * @param hashKeyCapacity
				 *      Hash capacity for {@link Basic#boundarySensitiveRoughEquivalenceClass(
				 *      Collection, IntegerIterator, int)}
				 */
				private static void traditionalCoreExam4CoreAttributeProcessStrategy(
						Collection<Integer> core, 
						AttrProcessStrategy4Comb coreAttributeProcessStrategy,
						Collection<RoughEquivalenceClassDummy> roughClasses,
						int hashKeyCapacity
				) {
					// Loop over attributes in coreAttributeProcessStrategy.
					int attr;
					int[] examingLineAttr;
					while (coreAttributeProcessStrategy.hasNextExamAttribute()) {
						// next attribute.
						attr = coreAttributeProcessStrategy.getExamingAttr();
						// Use the rest of the attributes to do partitioning
						examingLineAttr = coreAttributeProcessStrategy.getExamingLineAttr();
						for (RoughEquivalenceClassDummy roughClass: roughClasses) {
							// if 0-REC exists.
							if (Basic.boundarySensitiveRoughEquivalenceClass(
									roughClass.getItems(),
									new IntegerArrayIterator(examingLineAttr),
									hashKeyCapacity
								)==null
							) {
								// core = core U {a}.
								core.add(attr);
								break;
							}
						}
						// prepare for the next attribute to be checked.
						coreAttributeProcessStrategy.updateExamAttribute();
					}
					// prepare for the next attribute to be checked.
					coreAttributeProcessStrategy.updateInLineAttrs();
				}
			}
		}
		
		/**
		 * Obtain the current least significant attribute.
		 * Using the original strategy of REC.
		 * 
		 * @param attributeProcessStrategy
		 * 		An {@link AttributeProcessStrategy} instance.
		 * @param equClasses
		 * 		A {@link Collection} of {@link EquivalenceClass}es.
		 * @param reduct
		 * 		Reduct attributes.(Starts from 1)
		 * @param attributes
		 * 		Attributes of {@link Instance}.(Starts from 1, 0 as decision attribute)
		 * @return {@link PartitionResult}.
		 */
		public static PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>>
			leastSignificantAttribute(
					AttributeProcessStrategy attributeProcessStrategy,
					Collection<EquivalenceClass> equClasses, Collection<Integer> reduct,
					int[] attributes
		) {
			// a*.sig=null
			int minIndex = -1;
			// flag=false;
			// Go through a in P
			PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
			PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> leastResult = null;
			for (int a=0; a<attributes.length; a++) {
				if (reduct.contains(attributes[a]))	continue;
				// temp = P-{a}
				reduct.remove(attributes[a]);
				// flag, a.sig, A = dynamicPartition()
				partitionResult = Basic.dynamicIncrementalPartition(
										attributeProcessStrategy,
										equClasses
									);
				// if flag==true
				if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
					// return A, flag
					return partitionResult;
				}else {
					reduct.add(attributes[a]);
					// if a.sig<a*.sig
					if (leastResult==null || 
						leastResult.getPositive() > partitionResult.getPositive()
					) {
						// a*=a
						leastResult = partitionResult;
						minIndex = a;
					}
				}
			}
			if (minIndex!=-1) {
				reduct.remove(attributes[minIndex]);
				leastResult.setAttributes(reduct);
			}else {
				throw new RuntimeException("Illegal attributes index: "+ minIndex);
			}
			// return a*, flag
			return leastResult;
		}
		
		/**
		 * Obtain the current most significant attribute.
		 * Using the original strategy of REC.
		 * 
		 * @param roughClasses
		 * 		A {@link Collection} of {@link RoughEquivalenceClass}.
		 * @param reduct
		 * 		Reduct attributes.(Starts from 1)
		 * @param attributes
		 * 		Attributes of {@link Instance}.(Starts from 1)
		 * @return
		 */
		public static SignificantAttributeClassPack<Integer>
			mostSignificantAttribute(
				Collection<RoughEquivalenceClassDummy> roughClasses, Collection<Integer> reduct,
				int[] attributes
		) {
			int maxPos=0, sigAttr=-1;
			// Loop over attributes outside of reduct.
			StatisticResult<Collection<RoughEquivalenceClassDummy>> statisticResult;
			Collection<RoughEquivalenceClassDummy> maxSigRoughClasses = null;
			for (int i=0; i<attributes.length; i++) {
				if (reduct.contains(attributes[i]))	continue;
				// Calculate the significance of reduct ∪ {a}
				statisticResult = Basic.calculateRoughEquivalenceClassPosPartition(
										roughClasses, attributes[i]
									);
				// Update if needed.
				if (statisticResult.getPositiveRegion()>maxPos || sigAttr==-1) {
					sigAttr = attributes[i];
					maxSigRoughClasses = statisticResult.getRecord();
					maxPos = statisticResult.getPositiveRegion();
				}
			}
			return new SignificantAttributeClassPack<>(sigAttr, maxSigRoughClasses);
		}
		
		/**
		 * Inspection of reduct to remove redundant attributes.
		 * 
		 * @author Benjamin_L
		 */
		public static class Inspection {
			/** 
			 * Self-designed inspection procedure for IP-REC.
			 * <p>
			 * Use the given {@link AttrProcessStrategy4Comb} to group attributes in bulks for
			 * incremental partition in inspection.
			 * <p>
			 * The idea is that, if a group of attributes are not redundant, then partitioning
			 * without them would not affect the positive region(i.e. |0-REC|=0), meaning
			 * redundant attributes could be identified in bulk instead of being checked one by one.
			 * However, if a non-redundant attribute is inside of the bulk, then it does generate
			 * 0-REC. In this case, further partitioning as well as checking is required.
			 * 
			 * @param inspectAttributeProcessStrategy
			 * 		Implemented {@link AttributeProcessStrategy} instance.
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @return Reduct inspected.
			 */
			public static Collection<Integer> compute(
					AttrProcessStrategy4Comb inspectAttributeProcessStrategy,
					Collection<EquivalenceClass> equClasses
			) {
				// If contains 1 attribute only, no need to inspect
				if (inspectAttributeProcessStrategy.getAllLength()==1) {
					return Arrays.asList(inspectAttributeProcessStrategy.getExamingAttr());
				}
				Collection<Integer> red = null, redundant = null;
				// Initiate pointer: begin = 0, end = m-1
				int[] attributeGroup;
				int hashKeyCapacity = equClasses.size();
				PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
				// Loop over all attribute groups
				while (inspectAttributeProcessStrategy.hasNext()) {
					Collection<Integer> tmp1 = redundant;
					// Extract attributes from begin to end: B as attributeGroup
					if (redundant==null || redundant.isEmpty()) {
						// no redundant attributes yet, extract directly.
						attributeGroup = inspectAttributeProcessStrategy.getInLineAttr();
					}else {
						// need to check if redundant attributes in extracted group.
						Collection<Integer> examingLineAttrs = new LinkedList<>();
						// Loop over in-line attributes and collect ones that are not redundant.
						for (int each: inspectAttributeProcessStrategy.getInLineAttr()) {
							if (!tmp1.contains(each)){
								examingLineAttrs.add(each);
							}
						}
						// if |examing line attributes|==|in-line attributes|: all not redundant
						//  set attributeGroup = in-line attributes directly
						// else: has redundant attributes
						//  set attributeGroyp = in-line attributes without redundant ones.
						attributeGroup =
								examingLineAttrs.size()==inspectAttributeProcessStrategy.getInLineAttr().length?
										Arrays.copyOf(
												inspectAttributeProcessStrategy.getInLineAttr(),
												inspectAttributeProcessStrategy.getInLineAttr().length
										):
										ArrayCollectionUtils.getIntArrayByCollection(examingLineAttrs);
					}
					// If no non-redundant attributes in attributeGroup
					if (attributeGroup.length==0) {
						// Collect attributes left in examing line attributes, plus the examing
						//  attribute.
						int[] left = Arrays.copyOf(
										inspectAttributeProcessStrategy.getExamingLineAttr(), 
										inspectAttributeProcessStrategy.getExamingLineAttr().length+1
									);
						left[left.length-1] = inspectAttributeProcessStrategy.getExamingAttr();
						return compute(
								inspectAttributeProcessStrategy.initiate(
									new IntegerArrayIterator(left)
								),
								equClasses
							);
					}
					// Use attributes in attributeGroup to do 1st round of partitioning: U/(C-B),
					//  one by one.
					partitionResult = Basic.inTurnIncrementalPartition(
										equClasses,
										new IntegerArrayIterator(attributeGroup)
									);
					// if 0-REC is empty
					if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
						if (partitionResult.getAttributes().size()==1) {
							return partitionResult.getAttributes();
						}else {
							// The rest of the attributes are redundant, inspect the rest of the
							//  attributes by recursively compute().
							return compute(
										inspectAttributeProcessStrategy.initiate(
											// rest of the attributes
											new IntegerCollectionIterator(partitionResult.getAttributes())
										),
										equClasses
									);
						}
					}else {
						// Attributes used in partitioning are not enough.
						// Loop over the rest of attributes(that are not in the attributes used in
						//  the 1st round partitioning).
						if (red==null) {
							red = new HashSet<>(inspectAttributeProcessStrategy.getAllLength());
							red.addAll(partitionResult.getAttributes());
						}
						int attr;
						IntegerIterator examingLine;
						AttributeLoop:
						while (inspectAttributeProcessStrategy.hasNextExamAttribute()) {
							Collection<Integer> tmp2 = redundant;
							// Select an attribute for redundancy inspection.
							attr = inspectAttributeProcessStrategy.getExamingAttr();
							if (tmp2==null || tmp2.isEmpty()) {
								examingLine = new IntegerArrayIterator(
											inspectAttributeProcessStrategy.getExamingLineAttr()
										);
							}else {
								// Filter redundant attributes in examing line attributes.
								Collection<Integer> examingLineAttrs = new LinkedList<>();
								for (int each: inspectAttributeProcessStrategy.getExamingLineAttr()) {
									// filter redundant
									if (!tmp2.contains(each))	examingLineAttrs.add(each);
								}
								examingLine = new IntegerCollectionIterator(examingLineAttrs);
							}
							
							if (examingLine.size()!=0) {
								// Loop over attributes in examing line to partition
								for (RoughEquivalenceClassDummy roughClass:
										partitionResult.getRoughClasses()
								) {
									// If |0-REC|!=0, the attribute is required to eliminate
									//  the 0-REC => not redundant.
									if (Basic.boundarySensitiveRoughEquivalenceClass(
											roughClass.getItems(),
											examingLine,
											hashKeyCapacity
										)==null
									) {
										red.add(attr);
										// Inspect the next attribute in examing attribute line.
										inspectAttributeProcessStrategy.updateExamAttribute();
										continue AttributeLoop;
									}
								}
								// Used all examing line attributes and still no 0-REC occur,
								//  then the current examing attribute is redundant.
								if (redundant==null){
									redundant = new HashSet<>(inspectAttributeProcessStrategy.getAllLength()-1);
								}
								redundant.add(attr);
							}else {
								// not redundant.
								red.add(attr);
							}
							// Inspect the next attribute in examing attribute line.
							inspectAttributeProcessStrategy.updateExamAttribute();
						}
						// Update pointers:
						//  begin=end
						//  end = i+1<g? end+m: |C|-1
						inspectAttributeProcessStrategy.updateInLineAttrs();
					}
				}
				// remove all redundant attributes from reduct.
				if (redundant!=null && !redundant.isEmpty()){
					red.removeAll(redundant);
				}
				return red;
			}
		}
	}

	/**
	 * <strong>ID-REC</strong> for short.
	 * 
	 * @author Benjamin_L
	 */
	public static class IncrementalDecision {
		/**
		 * Basic algorithms for ID-REC
		 *
		 * @author Benjamin_L
		 */
		public static class Basic{
			/**
			 * Obtain an {@link EquivalenceClassDecMapXtension} {@link Collection} generated based on the given
			 * {@link Instance}s and all attributes.
			 * 
			 * @param instances
			 * 		A {@link Collection} of {@link Instance}s.
			 * @param attributes
			 * 		Attributes of {@link Instance} .
			 * @return Generated {@link EquivalenceClassDecMapXtension} {@link Collection}.
			 */
			public static <Sig extends Number> Collection<EquivalenceClassDecMapXtension<Sig>>
				equivalenceClass(Collection<Instance> instances, IntegerIterator attributes
			) {
				Map<IntArrayKey, EquivalenceClassDecMapXtension<Sig>> equClasses = new HashMap<>(instances.size());
				// Loop over instances and partition using attributes.
				IntArrayKey key;
				int[] value;
				EquivalenceClassDecMapXtension<Sig> equClass;
				for (Instance instance: instances) {
					// Construct a key based on attribute values.
					value = Instance.attributeValuesOf(instance, attributes);
					key = new IntArrayKey(value);
					
					equClass = equClasses.get(key);
					if (equClass==null) {
						// if key not exist.
						//  initiate the equivalence class.
						equClasses.put(key, equClass = new EquivalenceClassDecMapXtension<>(instance));
					}else {
						// else update equivalence class base on the instance.
						equClass.addClassItem(instance);
						//  update consistency if needed.
						if (equClass.sortable() &&
							Integer.compare(equClass.getDecisionValue(), instance.getAttributeValue(0))!=0
						) {
							equClass.setUnsortable();
						}
					}
				}
				return equClasses.values();
			}
		
			/**
			 * Obtain {@link RoughEquivalenceClassDecMapXtension}s induced by the given attributes.
			 * 
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClassDecMapXtension}s.
			 * @param attributes
			 * 		Attributes of {@link Instance}.
			 * @return Generated {@link RoughEquivalenceClassDecMapXtension} {@link Collection}.
			 */
			public static <Sig extends Number> Collection<RoughEquivalenceClassDecMapXtension<Sig>>
				roughEquivalenceClass(
					Collection<EquivalenceClassDecMapXtension<Sig>> equClasses,
					IntegerIterator attributes
			){
				Map<IntArrayKey, RoughEquivalenceClassDecMapXtension<Sig>> roughClasses = new HashMap<>();
				// Loop over equivalence classes.
				int[] keyArray;
				IntArrayKey key;
				RoughEquivalenceClassDecMapXtension<Sig> roughEquClass;
				for (EquivalenceClassDecMapXtension<Sig> equClass: equClasses) {
					// key = P(e)
					keyArray = new int[attributes.size()];
					attributes.reset();
					for (int i=0; i<keyArray.length; i++) {
						keyArray[i] = equClass.getAttributeValueAt(attributes.next() - 1);
					}
					key = new IntArrayKey(keyArray);

					roughEquClass = roughClasses.get(key);
					if (roughEquClass==null) {
						// if key not exists, create one
						roughClasses.put(key, roughEquClass=new RoughEquivalenceClassDecMapXtension<>());
					}
					// add the equivalence class into the rough equivalence class, and update dec. info.
					roughEquClass.addClassItem(equClass);
				}
				return roughClasses.values();
			}

			/**
			 * Perform further partitioning on {@link RoughEquivalenceClassDecMapXtension} using the given attributes.
			 * <p>
			 * Skipping {@link ClassSetType#POSITIVE} ones in partitioning.
			 * 
			 * @see #incrementalPartitionRoughEquivalenceClass(Collection, IntegerIterator, boolean)
			 *
			 * @param roughEquClasses
			 * 		A {@link Collection} of {@link RoughEquivalenceClassDecMapXtension}s.
			 * @param attributes
			 * 		Attributes of {@link Instance}.
			 * @return A {@link Collection} of {@link RoughEquivalenceClassDecMapXtension}s.
			 */
			public static <Sig extends Number> Set<RoughEquivalenceClassDecMapXtension<Sig>>
				incrementalPartitionRoughEquivalenceClass(
					Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughEquClasses,
					IntegerIterator attributes
			){
				return incrementalPartitionRoughEquivalenceClass(roughEquClasses, attributes, true);
			}
			/**
			 * Perform further partitioning on {@link RoughEquivalenceClassDecMapXtension} using the given attributes.
			 * <p>
			 * Notice: if <code>skipPos</code> is true, 1-RECs are not partitioned in this method for further
			 * partitioning will not increase the positive region, which is expected to accelerate the process of
			 * feature significance calculations and significant feature searching in Feature Selection.
			 * 
			 * @param roughEquClasses
			 * 		A {@link Collection} of {@link RoughEquivalenceClassDecMapXtension}s.
			 * @param attributes
			 * 		Attributes of {@link Instance}.
			 * @param skipPos
			 *      Whether to skip {@link RoughEquivalenceClassDecMapXtension} whose {@link ClassSetType#isPositive()}.
			 * @return A {@link Collection} of {@link RoughEquivalenceClassDecMapXtension}s.
			 */
			public static <Sig extends Number> Set<RoughEquivalenceClassDecMapXtension<Sig>>
				incrementalPartitionRoughEquivalenceClass(
					Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughEquClasses,
					IntegerIterator attributes, boolean skipPos
			){
				Set<RoughEquivalenceClassDecMapXtension<Sig>> incrementalRoughClasses = new HashSet<>();
				for (RoughEquivalenceClassDecMapXtension<Sig> roughClass: roughEquClasses) {
					if (skipPos){
						// Partition non 1-RECs only.
						if (!roughClass.getType().isPositive()) {
							incrementalRoughClasses.addAll(
									roughEquivalenceClass(roughClass.getItems(), attributes)
							);
						}else {
							incrementalRoughClasses.add(roughClass);
						}
					}else{
						incrementalRoughClasses.addAll(
								roughEquivalenceClass(roughClass.getItems(), attributes)
						);
					}
				}
				return incrementalRoughClasses;
			}
		
			/**
			 * Calculate the negative significance of the given {@link ClassSet}
			 * {@link Collection}, using {@link Calculation}.
			 * 
			 * @param <Sig>
			 * 		Significance Type.
			 * @param equClasses
			 * 		Implemented {@link ClassSet} instance.
			 * @param insSize
			 * 		The number of {@link Instance}.
			 * @param attributeLength
			 * 		The length of conditional attributes.
			 * @param calculation
			 * 		{@link RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation} implemented instance.
			 * @return {@link Sig} value as significance. / <code>null</code> if significance is 0.
			 * @throws Exception if exceptions occur when creating instance of {@link Calculation}.
			 */
			public static <Sig extends Number> Sig globalSignificance(
					Collection<EquivalenceClassDecMapXtension<Sig>> equClasses,
					int insSize, int attributeLength,
					RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation
			) throws Exception {
				Sig sigSum = null;
				// Loop over equivalence classes.
				for (EquivalenceClassDecMapXtension<Sig> equClass: equClasses) {
					calculation.calculate(equClass, attributeLength, insSize);
					// if -1-REC, mark the significance value for the equivalence class.
					if (equClass.getType().isNegative()) {
						equClass.setSingleSigMark(calculation.getResult());
					}
					sigSum = calculation.plus(sigSum, calculation.getResult());
				}
				return sigSum;
			}
		}
		
		/**
		 * Core algorithms for ID-REC, Including: Classic, Classic Improved (0-REC), Redundancy
		 * Mining.
		 * 
		 * @author Benjamin_L
		 */
		public static class Core{
			/**
			 * Obtain core.
			 * <p>
			 * Using classic core strategy.
			 * 
			 * @param <Sig>
			 * 		Type of feature significance.
			 * @param insSize
			 * 		The number of {@link Instance}s.
			 * @param calculation
			 * 		{@link RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation}
			 * 		implemented instance.
			 * @param globalSig
			 * 		Global positive region value.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider
			 * 		equal when the difference between two sig is less than the given deviation
			 * 		value.
			 * @param equClasses
			 * 		An {@link EquivalenceClassDecMapXtension} {@link Collection}.
			 * @param attributes
			 * 		Attributes of {@link Instance}.
			 * @return An {@link Integer} {@link Collection}.
			 * @throws Exception if exceptions occur when creating {@link Calculation} instance.
			 */
			public static <Sig extends Number> Collection<Integer> classic(
					int insSize,
					RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation,
					Sig globalSig, Sig sigDeviation, 
					Collection<EquivalenceClassDecMapXtension<Sig>> equClasses, int[] attributes
			) throws Exception {
				Collection<Integer> core = new HashSet<>(attributes.length);
				// Initiate an array to contain examining attributes.
				int[] examAttr = new int[attributes.length-1];
				for (int i=0; i<examAttr.length; i++)	examAttr[i] = attributes[i+1];
				// Loop over all attributes
				Sig sig = null;
				Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses;
				for (int i=0; i<attributes.length; i++) {
					// Obtain Rough Equivalence Classes induced by C-{a}
					roughClasses = Basic.roughEquivalenceClass(
										equClasses, 
										new IntegerArrayIterator(examAttr)
									);
					sig = null;
					for (RoughEquivalenceClassDecMapXtension<Sig> roughClass: roughClasses) {
						if (!roughClass.getType().isPositive()) {
							// if -1-REC & only contains 1 equivalence class: it can not be
							//  further partitioned with more attributes, significance values are
							//  settle.
							if (roughClass.getType().isNegative() && roughClass.getItemSize()==1) {
								// sig = sig + E.sig
								for (EquivalenceClassDecMapXtension<Sig> equClass: roughClass.getItems()) {
									sig = calculation.plus(sig, equClass.getSingleSigMark());
								}
							// else calculate the significance.
							}else {
								// sig = sig + sig calculation(E)
								calculation = calculation.calculate(roughClass, examAttr.length, insSize);
								sig = calculation.plus(sig, calculation.getResult());
							}
						}
					}
					// Check if the significance value are equal to the global significance value.
					if (calculation.value1IsBetter(globalSig, sig, sigDeviation)) {
						core.add(attributes[i]);
					}
					// next attribute.
					if (i<examAttr.length)	examAttr[i] = attributes[i];
				}
				return core;
			}
			
			/**
			 * Obtain core.
			 * <p>
			 * Improved version.
			 *
			 * @author Benjamin_L
			 */
			public static class ClassicImproved {
				/**
				 * Execute and get core.
				 * 
				 * @param <Sig>
				 * 		Type of feature significance.
				 * @param insSize
				 * 		The number of {@link Instance}.
				 * @param calculation
				 * 		{@link RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation}
				 * 		implemented instance.
				 * @param globalSig
				 * 		Global positive region value.
				 * @param sigDeviation
				 * 		Acceptable deviation when calculating significance of attributes.
				 * 		Consider equal when the difference between two sig is less than the
				 * 		given deviation value.
				 * @param equClasses
				 * 		An {@link EquivalenceClassDecMapXtension} {@link Collection}.
				 * @param attributes
				 * 		Attributes of {@link Instance}.
				 * @return An {@link Integer} {@link Collection}.
				 * @throws Exception if exceptions occur in {@link Calculation}.
				 */
				public static <Sig extends Number> Collection<Integer> exec(
						int insSize,
						RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation,
						Sig globalSig, Sig sigDeviation,
						Collection<EquivalenceClassDecMapXtension<Sig>> equClasses, int[] attributes
				) throws Exception{
					Collection<Integer> core = new HashSet<>(attributes.length);
					int[] examAttr = new int[attributes.length-1];
					for (int i=0; i<examAttr.length; i++)	examAttr[i] = attributes[i+1];
					// Loop over attributes
					Sig sig = null;
					Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses;
					for (int i=0; i<attributes.length; i++) {
						// Obtain Rough Equivalence Classes induced by C-{a}
						roughClasses = boundarySensitiveRoughEquivalenceClass(
											equClasses, 
											new IntegerArrayIterator(examAttr)
										);
						// If no 0-REC in rough equivalence classes.
						sig = null;
						if (roughClasses!=null) {
							for (RoughEquivalenceClassDecMapXtension<Sig> roughClass: roughClasses) {
								if (roughClass.getType().isNegative()) {
									// if -1-REC & only contains 1 equivalence class: it can not be
									//  further partitioned with more attributes, significance values are
									//  settle.
									if (roughClass.getItemSize()==1) {
										// sig = sig + E.sig
										for (EquivalenceClassDecMapXtension<Sig> equClass: roughClass.getItems()) {
											sig = calculation.plus(sig, equClass.getSingleSigMark());
										}
									}else {
										// sig = sig + sig calculation(E)
										calculation.calculate(roughClass, examAttr.length, insSize);
										sig = calculation.plus(sig, calculation.getResult());
									}
								}
							}
							if (calculation.value1IsBetter(globalSig, sig, sigDeviation)) {
								core.add(attributes[i]);
							}
						// else the attribute is core attribute for it can not be removed.
						}else {
							core.add(attributes[i]);
						}
						// next attribute.
						if (i<examAttr.length)	examAttr[i] = attributes[i];
					}
					return core;
				}
			
				/**
				 * Obtain {@link RoughEquivalenceClassDecMapXtension} induced by the given attributes.
				 * <p>
				 * <strong>Notice:</strong> This method is sensitive to 0-REC, once a 0-REC is obtained in process,
				 * return null.
				 * 
				 * @see Basic#roughEquivalenceClass(Collection, IntegerIterator)
				 * 
				 * @param <Sig>
				 * 		Type of feature significance.
				 * @param equClasses
				 * 		{@link EquivalenceClassDecMapXtension} {@link Collection}.
				 * @param attributes
				 * 		Attributes of {@link Instance}.
				 * @return {@link RoughEquivalenceClassDecMapXtension} {@link Collection}. /
				 * 			<code>null</code> if <code>RoughEquivalenceClassDecisionMapExtension
				 * 			</code>'s type equals {@link ClassSetType#BOUNDARY}.
				 */
				public static <Sig extends Number> Collection<RoughEquivalenceClassDecMapXtension<Sig>>
					boundarySensitiveRoughEquivalenceClass(
						Collection<EquivalenceClassDecMapXtension<Sig>> equClasses,
						IntegerIterator attributes
				){
					Map<IntArrayKey, RoughEquivalenceClassDecMapXtension<Sig>> roughClasses = new HashMap<>(attributes.size());
					// Loop over all equivalence classes.
					int[] keyArray;
					IntArrayKey key;
					RoughEquivalenceClassDecMapXtension<Sig> roughEqyClass;
					for (EquivalenceClassDecMapXtension<Sig> equClass: equClasses) {
						keyArray = new int[attributes.size()];
						attributes.reset();
						for (int i=0; i<keyArray.length; i++)
							keyArray[i] = equClass.getAttributeValueAt(attributes.next()-1);
						key = new IntArrayKey(keyArray);

						roughEqyClass = roughClasses.get(key);
						if (roughEqyClass==null) {
							roughClasses.put(key, roughEqyClass=new RoughEquivalenceClassDecMapXtension<>());
							roughEqyClass.addClassItem(equClass);
						}else {
							roughEqyClass.addClassItem(equClass);
							// return null if is 0-REC
							if (roughEqyClass.getType().isBoundary()){
								return null;
							}
						}
					}
					return roughClasses.values();
				}
			}
			
			/** 
			 * Obtain core.
			 * <p>
			 * Using incremental redundancy mining core strategy.
			 *
			 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Core
			 * 
			 * @param <Sig>
			 * 		Type of feature significance.
			 * @param insSize
			 * 		The number of {@link Instance}.
			 * @param shrinkInstance
			 * 		{@link Shrink4RECBasedDecisionMapExt} instance.
			 * @param calculation
			 * 		{@link Calculation}.
			 * @param globalSig
			 * 		Global positive region value.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider
			 * 		equal when the difference between two sig is less than the given deviation
			 * 		value.
			 * @param equClasses
			 * 		An {@link EquivalenceClassDecMapXtension} {@link Collection}.
			 * @param attributes
			 * 		Attributes of {@link Instance}.
			 * @return An {@link Integer} {@link Collection}.
			 * @throws Exception if exceptions occur when creating {@link Calculation} instance.
			 */
			public static <Sig extends Number> Collection<Integer> redundancyMining(
					int insSize,
					Shrink4RECBasedDecisionMapExt<Sig> shrinkInstance,
					RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation,
					Sig globalSig, Sig sigDeviation,
					Collection<EquivalenceClassDecMapXtension<Sig>> equClasses, int[] attributes
			) throws Exception {
				Collection<Integer> core = new HashSet<>(attributes.length);
				int limit = attributes.length;
				// Loop over attributes to check.
				Sig sig;
				boolean redundant;
				Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses;
				for (int i=0; i<limit; i++) {
					roughClasses = null;
					redundant = false;
					// Go through b[i] in C-{a}
					for (int j=0; j<attributes.length; j++) {
						if (i==j)	continue;
						// Use b[i] to partition.
						if (roughClasses==null) {
							roughClasses = Basic.roughEquivalenceClass(
												equClasses, 
												new IntegerArrayIterator(attributes[j])
											);
						}else {
							roughClasses = Basic.incrementalPartitionRoughEquivalenceClass(
												roughClasses, 
												new IntegerArrayIterator(attributes[j])
											);
						}
						// Remain 0-REC, & -1-REC that contains more than 1 equivalence classes.
						roughClasses =
								shrinkInstance.shrink(
										new ShrinkInput4RECIncrementalDecisionExtension<>(
												roughClasses, 1
										)
								).getRoughClasses();
						// If no rough equivalence classes left to further partition.
						if (roughClasses.isEmpty()) {
							// confirm redundant
							redundant = true;
							limit = j+1;
							break;
						// else calculate significance value.
						}else {
							sig = null;
							for (RoughEquivalenceClassDecMapXtension<Sig> roughClass: roughClasses) {
								if (calculation.calculateAble(roughClass)) {
									if (roughClass.getItemSize()!=1) {
										// sig = sig + sig calculation(E)
										calculation.calculate(roughClass, 1, insSize );
										sig = calculation.plus(sig, calculation.getResult());
									}else {
										// sig = sig + E.sig
										for (EquivalenceClassDecMapXtension<Sig> equClass: roughClass.getItems()) {
											sig = calculation.plus(sig, equClass.getSingleSigMark());
										}
									}
								}
							}
							if (calculation.value1IsBetter(globalSig, sig, sigDeviation)) {
								redundant = true;
								limit = j+1;
								break;
							}
						}
					}
					if (!redundant)	core.add(attributes[i]);
				}
				return core;
			}
		}

		/**
		 * Obtain the current most significant attribute.
		 * 
		 * @param <Sig>
		 * 		Type of feature significance.
		 * @param roughClasses
		 * 		A {@link Collection} of  {@link RoughEquivalenceClassDecMapXtension}s.
		 * @param reduct
		 * 		Current reduct.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @param calculation
		 * 		{@link RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation}
		 * 		instance.
		 * @param deviation
		 * 		Acceptable deviation when calculating significance of attributes. Consider equal
		 * 		when the difference between two sig is less than the given deviation value.
		 * @return {@link MostSignificantAttributeResult} with the most significant attribute
		 * 			and the correspondent {@link RoughEquivalenceClassDecMapXtension}
		 * 			{@link Collection}.
		 * @throws Exception if exceptions occur in {@link Calculation}.
		 */
		public static <Sig extends Number> MostSignificantAttributeResult<Sig>
			mostSignificantAttribute(
				Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses,
				Collection<Integer> reduct, int[] attributes,
				RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation,
				Sig deviation,
				Shrink4RECBasedDecisionMapExt<Sig> shrinkInstance
		) throws Exception {
			int sigAttr=-1;
			Sig redSig, staticSig, sigSum;
			Sig maxRedSig = null, maxStaticSig = null, maxSigSum = null;
			ShrinkResult4RECIncrementalDecisionExtension<Sig> shrinkResult;
			Collection<RoughEquivalenceClassDecMapXtension<Sig>> sigRoughClasses = null;
			Collection<RoughEquivalenceClassDecMapXtension<Sig>> incrementalRoughClasses;
			// Loop over all attributes
			for (int attr: attributes) {
				if (reduct.contains(attr))	continue;
				// Obtain rough equivalence classes incrementally
				incrementalRoughClasses =
						Basic.incrementalPartitionRoughEquivalenceClass(
								roughClasses,
								new IntegerArrayIterator(attr)
						);
				// Remain 0-RECs, & -1-RECs that contains more than 1 equivalence classes.
				shrinkResult =
						shrinkInstance.shrink(
								new ShrinkInput4RECIncrementalDecisionExtension<Sig>(
										incrementalRoughClasses, 1
								)
						);
				incrementalRoughClasses = shrinkResult.getRoughClasses();
				staticSig = shrinkResult.getRemovedUniverseSignificance();//*/
				// Calculate outer significance.
				calculation.calculate(incrementalRoughClasses, 1);
				redSig = calculation.getResult();
				sigSum = calculation.plus(redSig, staticSig);
				// if (a.outerSig > sig) update.
				if (maxSigSum==null || 
					calculation.value1IsBetter(sigSum, maxSigSum, deviation)
				) {
					sigAttr = attr;
					maxRedSig = redSig;
					maxStaticSig = staticSig;
					maxSigSum = sigSum;
					sigRoughClasses = incrementalRoughClasses;
				}
			}
			return new MostSignificantAttributeResult<>(maxRedSig, maxStaticSig, sigAttr, sigRoughClasses);
		}
	
		/**
		 * Reduct inspection algorithms for ID-REC.
		 * 
		 * @author Benjamin_L
		 */
		public static class InspectReduct{
			
			/**
			 * Inspect the given reduct and remove redundant attributes.
			 * 
			 * @param <Sig>
			 * 		Type of feature significance.
			 * @param insSize
			 * 		The number of {@link Instance}s.
			 * @param reduct
			 * 		Current reduct.
			 * @param globalSig
			 * 		Global positive region value.
			 * @param equClasses
			 * 		An {@link EquivalenceClassDecMapXtension} {@link Collection}.
			 * @param calculation
			 * 		{@link RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation} instance.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider equal when the 
			 * 		difference between two sig is less than the given deviation value.
			 * @throws IllegalArgumentException if exceptions occur when creating {@link Calculation} instance.
			 * @throws SecurityException if exceptions occur when creating {@link Calculation} instance.
			 */
			public static <Sig extends Number> void classic(
					int insSize,
					Collection<Integer> reduct, Sig globalSig,
					Collection<EquivalenceClassDecMapXtension<Sig>> equClasses,
					RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation,
					Sig sigDeviation
			) throws IllegalArgumentException, SecurityException {
				// Loop over attributes in reduct
				Sig examSig;
				Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses;
				Integer[] redCopy = reduct.toArray(new Integer[reduct.size()]);
				for (int attr: redCopy) {
					// calculate Sig(R-{a}).
					reduct.remove(attr);
					roughClasses = Basic.roughEquivalenceClass(equClasses, new IntegerCollectionIterator(reduct));
					calculation = calculation.calculate(roughClasses, reduct.size(), insSize);
					examSig = calculation.getResult();
					// if (R-{a}.sig==C.sig)
					if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)) {
						// Not redundant.
						reduct.add(attr);
					}
				}
			}
			
			/**
			 * Inspect the given reduct and remove redundant attributes.
			 * 
			 * @param <Sig>
			 * 		Type of feature significance.
			 * @param insSize
			 * 		The number of {@link Instance}.
			 * @param reduct
			 * 		Current reduct.
			 * @param globalSig
			 * 		Global positive region value.
			 * @param equClasses
			 * 		An {@link EquivalenceClassDecMapXtension} {@link Collection}.
			 * @param calculation
			 * 		{@link RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation} instance.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider equal when the 
			 * 		difference between two sig is less than the given deviation value.
			 * @throws IllegalArgumentException if exceptions occur when creating {@link Calculation} instance.
			 * @throws SecurityException if exceptions occur when creating {@link Calculation} instance.
			 */
			public static <Sig extends Number> void classicImproved(
					int insSize,
					Collection<Integer> reduct, Sig globalSig,
					Collection<EquivalenceClassDecMapXtension<Sig>> equClasses,
					RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation,
					Sig sigDeviation
			) throws IllegalArgumentException, SecurityException {
				// Loop over attributes in reduct
				Sig examSig;
				Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses;
				Integer[] redCopy = reduct.toArray(new Integer[reduct.size()]);
				for (int attr: redCopy) {
					// calculate Sig(R-{a}).
					reduct.remove(attr);
					roughClasses = Core.ClassicImproved.boundarySensitiveRoughEquivalenceClass(equClasses, new IntegerCollectionIterator(reduct));
					// * Doesn't contain 0-REC, otherwise, current attribute is not redundant
					if (roughClasses!=null) {
						calculation.calculate(roughClasses, reduct.size(), insSize);
						examSig = calculation.getResult();
						// if (R-{a}.sig==C.sig)
						if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)) {
							// Not redundant.
							reduct.add(attr);
						}
					}else {
						reduct.add(attr);
					}
				}
			}
		}
	}
}