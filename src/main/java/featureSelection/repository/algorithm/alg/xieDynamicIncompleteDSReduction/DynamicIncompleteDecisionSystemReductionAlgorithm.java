package featureSelection.repository.algorithm.alg.xieDynamicIncompleteDSReduction;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainerByDirectCollectNCache;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateInfoPack;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateResult;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.MostSignificantAttributeResult;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.PreviousInfoPack;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.attributeRelatedUpdate.AlteredAttributeValues;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.objectRelatedUpdate.AlteredInstanceItem;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReduction;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Algorithm repository for Xie's Dynamic Incomplete Decision System Reduction(DIDSR) which bases on
 * the paper <a href="https://linkinghub.elsevier.com/retrieve/pii/S0888613X17302918">A novel
 * incremental attribute reduction approach for dynamic incomplete decision systems</a> by Xiaojun
 * Xie, Xiaolin Qin.
 * 
 * @author Benjamin_L
 */
public class DynamicIncompleteDecisionSystemReductionAlgorithm {
	
	public static class Basic {
		
		/**
		 * Obtain the tolerance class of instances.
		 * 
		 * @param instances
		 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
		 * @param attributes
		 * 		Attributes of {@link Instance}: <strong>P</strong>
		 * @param toleranceClassObtainer
		 * 		A {@link ToleranceClassObtainer} instance to execute tolerance class obtaining.
		 * @param completeData4Attributes
		 * 		{@link InstancesCollector} that contains complete data
		 * 		(in <strong><code>instances</code></strong>) for every attributes(in
		 * 		<code>attributes</code>).
		 * @return tolerance classes of the given <code>instances</code>.
		 */
		public static Map<Instance, Collection<Instance>> toleranceClass(
			Collection<Instance> instances, IntegerIterator attributes,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4Attributes
		){
			return toleranceClass(
						instances, instances, attributes,
						toleranceClassObtainer, completeData4Attributes
					);
		}
		/**
		 * Return tolerance classes(values extracted from <code>candidates</code>) of the given 
		 * <code>instances</code>.
		 * <p>
		 * Tolerance classes of an {@link Instance} are wrapped by {@link HashSet}.
		 * 
		 * @param instances
		 * 		{@link Instance}s of the target ones with tolerance classes.
		 * @param candidates
		 * 		{@link Instance} candidates for tolerance classes searching.
		 * @param attributes
		 * 		Attributes for partitioning.
		 * @param toleranceClassObtainer
		 * 		A {@link ToleranceClassObtainer} instance to execute tolerance class obtaining.
		 * @param completeCandidates4Attributes
		 * 		{@link InstancesCollector} that contains complete data
		 * 		(in <strong><code>candidates</code></strong>) for every attributes(in 
		 * 		<code>attributes</code>).
		 * @return A {@link Map} of <code>instances</code>'s tolerance classes.
		 */
		public static Map<Instance, Collection<Instance>> toleranceClass(
			Collection<Instance> instances, Collection<Instance> candidates,
			IntegerIterator attributes, 
			ToleranceClassObtainer toleranceClassObtainer, 
			InstancesCollector completeCandidates4Attributes
		){
			return toleranceClassObtainer.obtain(
						instances, candidates, attributes, 
						completeCandidates4Attributes
					);
		}
		
		public static Map<Instance, Collection<Instance>>
			toleranceClassConsideringDecisionValue(
				Map<Instance, Collection<Instance>> toleranceClasses
		){
			final Map<Instance, Collection<Instance>> result =
					new HashMap<>(toleranceClasses.size());
			
			for (Entry<Instance, Collection<Instance>> entry:
				toleranceClasses.entrySet()
			) {
				int decisionValue = entry.getKey().getAttributeValue(Instance.DECISION_ATTRIBUTE_INDEX);
				Collection<Instance> tolerance =
					entry.getValue().stream().filter(ins->ins.getAttributeValue(Instance.DECISION_ATTRIBUTE_INDEX)==decisionValue)
						.collect(Collectors.toList());
				
				result.put(entry.getKey(), tolerance);
			}
			
			return result;
		}
		
		
		/**
		 * Copy the given tolerance classes.
		 * 
		 * @param <ToleranceCollection>
		 * 		Type of {@link Collection} to contain the tolerance classes of an {@link Instance}.
		 * @param tolerances
		 * 		The tolerance classes to be copied
		 * @param tolerancesClass
		 * 		The class of {@link ToleranceCollection}.
		 * @return copied tolerance classes.
		 * @throws InstantiationException if exceptions occur when creating new instance of {@link ToleranceCollection}.
		 * @throws IllegalAccessException if exceptions occur when creating new instance of {@link ToleranceCollection}.
		 * @throws IllegalArgumentException if exceptions occur when creating new instance of {@link ToleranceCollection}.
		 * @throws InvocationTargetException if exceptions occur when creating new instance of {@link ToleranceCollection}.
		 * @throws NoSuchMethodException if exceptions occur when creating new instance of {@link ToleranceCollection}.
		 * @throws SecurityException if exceptions occur when creating new instance of {@link ToleranceCollection}.
		 */
		public static <ToleranceCollection extends Collection<Instance>> Map<Instance, Collection<Instance>>
			copyToleranceClass(
				Map<Instance, Collection<Instance>> tolerances,
				Class<ToleranceCollection> tolerancesClass
		) throws InstantiationException, IllegalAccessException, IllegalArgumentException, 
				InvocationTargetException, NoSuchMethodException, SecurityException
		{
			Map<Instance, Collection<Instance>> copy = new HashMap<>(tolerances.size());
			for (Entry<Instance, Collection<Instance>> entry: tolerances.entrySet()) {
				copy.put(entry.getKey(), copyToleranceClass(entry.getValue(), tolerancesClass));
			}
			return copy;
		}
		/**
		 * Copy the given tolerance class.
		 * 
		 * @param <Tolerance>
		 * 		Type of {@link Collection} to contain the tolerance classes of an {@link Instance}.
		 * @param tolerance
		 * 		The tolerance class to be copied
		 * @param tolerancesClass
		 * 		The class of {@link Tolerance}.
		 * @return A new {@link Collection} of {@link Instance} to contain tolerance class.
		 * @throws InstantiationException if exceptions occur when creating new instance of {@link Tolerance}.
		 * @throws IllegalAccessException if exceptions occur when creating new instance of {@link Tolerance}.
		 * @throws IllegalArgumentException if exceptions occur when creating new instance of {@link Tolerance}.
		 * @throws InvocationTargetException if exceptions occur when creating new instance of {@link Tolerance}.
		 * @throws NoSuchMethodException if exceptions occur when creating new instance of {@link Tolerance}.
		 * @throws SecurityException if exceptions occur when creating new instance of {@link Tolerance}.
		 */
		public static <Tolerance extends Collection<Instance>> Tolerance copyToleranceClass(
				Collection<Instance> tolerance, Class<Tolerance> tolerancesClass
		) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException 
		{
			Tolerance tolerancesOfIns = tolerancesClass.getConstructor(Collection.class).newInstance(tolerance);
			return tolerancesOfIns;
		}

		/**
		 * Maintain the given tolerance classes by adding the given 2 {@link Instance}s into
		 * each others' tolerance class.
		 * 
		 * @param tolerances
		 * 		The tolerance classes to be maintained.
		 * @param ins1
		 * 		An {@link Instance} that is tolerant with <code>ins2</code>.
		 * @param ins2
		 * 		An {@link Instance} that is tolerant with <code>ins1</code>.
		 */
		public static void maintainSymmetricTolerance(
			Map<Instance, Collection<Instance>> tolerances,
			Instance ins1, Instance ins2
		) {
			Collection<Instance> toleranceOfIns1 = tolerances.get(ins1);
			if (toleranceOfIns1==null)	{
				tolerances.put(ins1, toleranceOfIns1=new HashSet<>());
				toleranceOfIns1.add(ins1);
			}
			toleranceOfIns1.add(ins2);
			
			Collection<Instance> toleranceOfIns2 = tolerances.get(ins2);
			if (toleranceOfIns2==null)	{
				tolerances.put(ins2, toleranceOfIns2=new HashSet<>());
				toleranceOfIns2.add(ins2);
			}
			toleranceOfIns2.add(ins1);
		}
		
		/**
		 * Check if the 2 {@link Instance} are tolerant for given attributes:
		 * <pre>
		 * T(B)={(x,y)∈U×U|∀b∈B,f(x,b)=f(y,b)∨f(x,b)=∗∨f(y,b)=∗}
		 * </pre>
		 * 
		 * @param ins1
		 * 		An {@link Instance}.
		 * @param ins2
		 * 		Another {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance} for checking.(starts from 1)
		 * @return true if they are tolerant to each other. / false if not.
		 */
		public static boolean tolerant(Instance ins1, Instance ins2, IntegerIterator attributes) {
			// ---------------------------------------------------------------
			// | T(B)={(x,y)∈U×U|∀b∈B,f(x,b)=f(y,b)∨f(x,b)=∗∨f(y,b)=∗}		 |
			// ---------------------------------------------------------------
			attributes.reset();
			for (int i=0; i<attributes.size(); i++) {
				int attr = attributes.next();
				int attrV1 = ins1.getAttributeValue(attr), attrV2 = ins2.getAttributeValue(attr);
				if (attrV1==attrV2) {
					continue;
				}else if (
					ins1 instanceof IncompleteInstance &&
					IncompleteInstance.MISSING_VALUE==attrV1
				) {
					continue;
				}else if (
					ins2 instanceof IncompleteInstance &&
					IncompleteInstance.MISSING_VALUE==attrV2
				) {
					continue;
				}else {
					return false;
				}
			}
			return true;
		}

	}
	
	public static class Dynamic {
		
		public static PreviousInfoPack generatePreviousInfoPack(
				Collection<Instance> previousInstances,
				int[] attributes, Collection<Integer> previousReduct
		) {
			ToleranceClassObtainer toleranceClassObtainer = 
					new ToleranceClassObtainerByDirectCollectNCache();
			InstancesCollector dataCollector = 
					toleranceClassObtainer.getCacheInstanceGroups(previousInstances);
			
			Map<Instance, Collection<Instance>> toleranceClassOfPreviousInsWithAttributes =
				Basic
					.toleranceClass(
						previousInstances, new IntegerArrayIterator(attributes),
						toleranceClassObtainer, dataCollector
					);
			Map<Instance, Collection<Instance>> toleranceClassOfPreviousInsWithAttributesNDec =
				Basic
					.toleranceClassConsideringDecisionValue(toleranceClassOfPreviousInsWithAttributes);
			
			Map<Instance, Collection<Instance>> toleranceClassOfPreviousInsWithPreviousRed =
					Basic
						.toleranceClass(
							previousInstances, new IntegerCollectionIterator(previousReduct),
							toleranceClassObtainer, dataCollector
						);
				Map<Instance, Collection<Instance>> toleranceClassOfPreviousInsWithPreviousRedNDec =
					Basic
						.toleranceClassConsideringDecisionValue(toleranceClassOfPreviousInsWithPreviousRed);
				
					
			return new PreviousInfoPack(
					previousInstances,
					toleranceClassOfPreviousInsWithAttributes, 
					toleranceClassOfPreviousInsWithAttributesNDec, 
					previousReduct,
					toleranceClassOfPreviousInsWithPreviousRed, 
					toleranceClassOfPreviousInsWithPreviousRedNDec
			);
		}
		
		public static class Common {
			
			/**
			 * Apply {@link DynamicUpdateInfoPack} into all {@link Instance}. Simply replace
			 * the previous {@link Instance}s with the latest ones with altered attribute
			 * values applied.
			 * 
			 * @author Benjamin_L
			 */
			public static class TransformPrevious2Latest{
				public static void exec(
					Collection<Instance> previous, Collection<Instance> latest,
					DynamicUpdateInfoPack updateInfo
				) {
					for (Instance ins: previous)
						latest.add(updateInfo.getLatestInstanceOf(ins));
				}
				
				public static Map<Instance, Collection<Instance>> exec(
					Map<Instance, Collection<Instance>> previousTolerances,
					DynamicUpdateInfoPack updateInfo
				) {
					Map<Instance, Collection<Instance>> latestTolerances =
							new HashMap<>(previousTolerances.size());
					
					for (Entry<Instance, Collection<Instance>> ins:
						previousTolerances.entrySet()
					) {
						Instance key = updateInfo.getLatestInstanceOf(ins.getKey());
						Collection<Instance> latestTolerance = new HashSet<>(ins.getValue().size());
						latestTolerances.put(key, latestTolerance);
						
						TransformPrevious2Latest.exec(ins.getValue(), latestTolerance, updateInfo);
					}
					
					return latestTolerances;
				}
			}
			
			/**
			 * Taking {@link AlteredAttributeValues} into priority while checking if the 2 
			 * {@link Instance} are tolerant for given attributes:
			 * <pre>
			 * T(B)={(x,y)∈U×U|∀b∈B,f(x,b)=f(y,b)∨f(x,b)=∗∨f(y,b)=∗}
			 * </pre>
			 * 
			 * @see Basic#tolerant(Instance, Instance, IntegerIterator)
			 * 
			 * @param ins1
			 * 		An {@link Instance}.
			 * @param ins2
			 * 		Another {@link Instance}.
			 * @param attributes
			 * 		Attributes of {@link Instance} for checking.(starts from 1)
			 * @param alteredAttributeValues
			 * 		An {@link AlteredAttributeValues} instance.
			 * @return true if they are tolerant to each other. / false if not.
			 */
			public static boolean tolerant(
				Instance ins1, Instance ins2, IntegerIterator attributes,
				AlteredAttributeValues alteredAttributeValues
			) {
				// ---------------------------------------------------------------
				// | T(B)={(x,y)∈U×U|∀b∈B,f(x,b)=f(y,b)∨f(x,b)=∗∨f(y,b)=∗}		 |
				// ---------------------------------------------------------------
				attributes.reset();
				for (int i=0; i<attributes.size(); i++) {
					int attr = attributes.next();
					int attrV1 = alteredAttributeValues.getAlteredAttributeValueOf(attr, ins1), 
						attrV2 = alteredAttributeValues.getAlteredAttributeValueOf(attr, ins2);

					if (attrV1==attrV2) {
						continue;
					}else if (IncompleteInstance.MISSING_VALUE==attrV1) {
						continue;
					}else if (IncompleteInstance.MISSING_VALUE==attrV2) {
						continue;
					}else {
						return false;
					}
				}
				return true;
			}
			
			/**
			 * Update <code>tolerances</code> for attribute values changed.
			 * <p>
			 * <strong>Notice</strong>: 
			 * According to the >Algorithm 3< of the original paper, all {@link Instance}s inputs
			 * (i.e. <code>relatedInstances</code>, <code>candidateInstances</code>) into this method 
			 * should be the original ones(<strong>S<sup>N</sup></strong>, instead of S<sup>N+1</sup>), 
			 * <strong>except</strong> for <code>tolerances</code> which is a set contains elements 
			 * updated(S<sup>N</sup> --update-→ S<sup>N+1</sup>).
			 * 
			 * @param tolerances
			 * 		The tolerance classes to update: 
			 * 		<strong>T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup></strong>
			 * @param relatedInstances
			 * 		{@link Instance}s whose tolerance classes are to be updated in
			 * 		<code>tolerances</code>:
			 * 		<strong>
			 * 			x<sub>i</sub> in 
			 * 			T<sub>B-P-C<sub>ALT</sub></sub><sup>S<sub>U</sub><sup>N</sup></sup>(x<sub>i</sub>) - 
			 * 			T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x<sub>i</sub>)
			 * 		</strong>
			 * @param previousToleranceAttributes
			 * 		Attributes used in calculating the previous tolerance classes.
			 * @param selectedUnalteredInPreviousToleranceAttributes
			 * 		Selected un-altered attributes in <code>previousToleranceAttributes</code>: 
			 * 		<strong>P</strong>
			 * @param tolerancesOfSelectedUnalteredPreviousToleranceAttrs
			 * 		Tolerance classes of <code>selectedUnalteredInPreviousToleranceAttributes</code>:
			 * 		<strong>T<sub>P</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x<sub>i</sub>)</strong>
			 * @param alteredAttributes
			 * 		Altered attributes: <strong>C<sub>ALT</sub></strong>
			 */
			public static void updateToleranceClass4AttributesDeleted(
					Map<Instance, Collection<Instance>> tolerances,
					Collection<Instance> relatedInstances,
					Collection<Integer> previousToleranceAttributes, 
					Collection<Integer> selectedUnalteredInPreviousToleranceAttributes,
					Map<Instance, Collection<Instance>> tolerancesOfSelectedUnalteredPreviousToleranceAttrs,
					Collection<Integer> alteredAttributes
			) {
				// ------------------------------------------------------------------------------------
				// | Calculate B-P-C<sub>ALT</sub> for later use.									  |
				// | 	B is attribute set used in calculation of previous tolerance classes, 	 	  |
				// |	P is the selected un-altered attributes in B,								  |
				// | 	C<sub>ALT</sub> is the global altered attribute set.						  |
				// ------------------------------------------------------------------------------------
				Collection<Integer> bMinusPMinusCAlt = new HashSet<>(previousToleranceAttributes);
				bMinusPMinusCAlt.removeAll(selectedUnalteredInPreviousToleranceAttributes);
				bMinusPMinusCAlt.removeAll(alteredAttributes);
				IntegerIterator bMinusPMinusCAltIterator = new IntegerCollectionIterator(bMinusPMinusCAlt);
				// for i=1 to |U| do
				for (Instance targetIns: relatedInstances) {
					// for each x[k] in T<sub>P</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x[i]) - 
					//	T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x[i]) do
					Collection<Instance> tolerancesOfInsOfPreviousTolerances =
							tolerances.get(targetIns);
					Collection<Instance> tolerancesOfInsOfSelectedUnAlteredPreviousToleranceAttrs =
							tolerancesOfSelectedUnalteredPreviousToleranceAttrs.get(targetIns);
					for (Instance xk: tolerancesOfInsOfSelectedUnAlteredPreviousToleranceAttrs) {
						if (!tolerancesOfInsOfPreviousTolerances.contains(xk)) {
							// if (x[k], x[i]) in T(B-P-C<sub>ALT</sub>) then
							if (Basic.tolerant(targetIns, xk, bMinusPMinusCAltIterator)) {
								// T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ← 
								//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ∪
								//	{x[i]}
								// T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) ← 
								//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) ∪
								//	{x[k]}
								Basic.maintainSymmetricTolerance(tolerances, xk, targetIns);
							}
						}
					}
				}
			}
		
			/**
			 * Initiate a reduct based on the given <code>previousReduct</code> and 
			 * {@link AlteredAttributeValues#getAlteredAttributes() alteredAttributes}.
			 * <p>
			 * According to the original paper, a new reduct is initialized by checking altered attributes
			 * by comparing the significance values of the attribute before and after the attribute values 
			 * changed one by one.
			 * 
			 * @param <Sig>
			 * 		Type of feature (subset) significance.
			 * @param updateInfo
			 * 		{@link DynamicUpdateInfoPack} instance that contains update info.
			 * @param calculation
			 * 		{@link FeatureImportance4XieDynamicIncompleteDSReduction} instance.
			 * @param sigDeviation
			 * 		Acceptable deviation when calculating significance of attributes. Consider equal when the 
			 * 		difference between two sig is less than the given deviation value.
			 * @return Initiated new reduct.
			 */
			public static <Sig extends Number> Collection<Integer> 
				initialiseReductByCheckingAlteredAttributesOneByOne(
					DynamicUpdateInfoPack updateInfo, ToleranceClassObtainer toleranceClassObtainer,
					FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation,
					Sig sigDeviation
			) {
				Collection<Integer> initNewReduct = new HashSet<>(updateInfo.getPreviousInfo().getReduct());
				
				for (int alteredAttribute: updateInfo.getAlteredAttributeValues().getAlteredAttributes()) {
					// Compute in-consistency degree on the previous and newest instances value using the 
					//	altered attribute
					Sig previousSig = 
						calculation.calculate(
							updateInfo.getPreviousInfo().getInstances(),
							new IntegerArrayIterator(alteredAttribute),
							toleranceClassObtainer, 
							toleranceClassObtainer.getCacheInstanceGroups(
								updateInfo.getPreviousInfo().getInstances(), 
								new IntegerArrayIterator(alteredAttribute)		
							)
						).getResult();
					Sig alteredSig =
						calculation.calculate(
							updateInfo.getAlteredAttrValAppliedInstances(), 
							new IntegerArrayIterator(alteredAttribute),
							toleranceClassObtainer, 
							toleranceClassObtainer.getCacheInstanceGroups(
								updateInfo.getAlteredAttrValAppliedInstances(), 
								new IntegerArrayIterator(alteredAttribute)	
							)
						).getResult();
					// if altered attribute is in previous reduct.
					if (updateInfo.getPreviousInfo().getReduct().contains(alteredAttribute)) {
						// if sig value of newest <= value of previous (== not newest > previous)
						if (!calculation.value1IsBetter(alteredSig, previousSig, sigDeviation)) {
							// initialize REC<sub>N+1</sub> ← RED<sub>N</sub>
						}else {
							// initialize REC<sub>N+1</sub> ← RED<sub>N</sub> - {a<sub>ALT</sub>}
							initNewReduct.remove(alteredAttribute);
						}
					// else	altered attribute is not in the previous reduct.
					}else {
						// if sig value of newest < value of previous
						if (calculation.value1IsBetter(previousSig, alteredSig, sigDeviation)) {
							// initialize REC<sub>N+1</sub> ← RED<sub>N</sub> ∪ {a<sub>ALT</sub>}
							initNewReduct.add(alteredAttribute);
						}else {
							// initialize REC<sub>N+1</sub> ← RED<sub>N</sub>
						}
					}
				}
				return initNewReduct;
			}
			
			/**
			 * Update significance for object-related update by updating tolerance classes of 
			 * <code>B</code> and <code>B∪D</code>.
			 * 
			 * @param <Sig>
			 * 		Type of feature (subset) significance.
			 * @param originalInstances
			 * 		The original {@link Instance}s before any updated is made:
			 * 		<strong>S<sub>U</sub><sup>N</sup></strong>
			 * @param previousTolerancesOfCondAttrs
			 * 		The previous tolerance classes obtained using conditional attributes:
			 * 		<strong>T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)</strong>
			 * @param previousTolerancesOfCondAttrsNDecAttr
			 * 		The conditional attributes that were used to obtain the previous tolerance classes:
			 * 		<strong>T<sub>B∪D</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)</strong>
			 * @param previousToleranceCondAttrs
			 * 		The conditional attributes used to obtain <code>previousTolerancesOfCondAttrs</code>:
			 * 		<strong>B</strong>
			 * @param alterInstanceItems
			 * 		Items of altered {@link Instance}s wrapped in {@link AlteredInstanceItem}
			 * 		{@link Map}.
			 * @param calculation
			 * 		{@link FeatureImportance4XieDynamicIncompleteDSReduction} instance.
			 * @return {@link DynamicUpdateResult}
			 * @throws Exception if exceptions occur when copying <code>previousTolerances</code>.
			 */
			public static <Sig extends Number> DynamicUpdateResult<Sig> updateSignificance4ObjectRelated(
					Collection<Instance> originalInstances,
					Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs,
					Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttr,
					Collection<Integer> previousToleranceCondAttrs,
					Map<Instance, AlteredInstanceItem> alterInstanceItems,
					FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation
			) throws Exception {
				// Obtain updated Tolerance classes: T<sub>B</sub>
				Map<Instance, Collection<Instance>> latestTolerancesOfReduct =
					calculation.updateToleranceClass4ObjectRelatedUpdate(
							originalInstances,
							previousTolerancesOfCondAttrs,
							new IntegerCollectionIterator(previousToleranceCondAttrs), 
							alterInstanceItems
					);	
				// Obtain updated Tolerance classes with decision attribute: T<sub>B∪D</sub>
				Collection<Integer> previousToleranceCondAttrsNDecAttr = new ArrayList<>(
						previousToleranceCondAttrs.size()+1
				);
				previousToleranceCondAttrsNDecAttr.addAll(previousToleranceCondAttrs);
				previousToleranceCondAttrsNDecAttr.add(0);
				
				Map<Instance, Collection<Instance>> latestTolerancesWithDecAttr =
					calculation.updateToleranceClass4ObjectRelatedUpdate(
							originalInstances,
							previousTolerancesOfCondAttrsNDecAttr,
							new IntegerCollectionIterator(previousToleranceCondAttrsNDecAttr), 
							alterInstanceItems
					);
				
				// Calculate feature significance.
				calculation.calculate(latestTolerancesOfReduct, latestTolerancesWithDecAttr);
				Sig sig = calculation.getResult();
				
				return new DynamicUpdateResult<>(sig, latestTolerancesOfReduct, latestTolerancesWithDecAttr);
			}
		
			/**
			 * Update significance for attribute-related update by updating tolerance classes of 
			 * <code>B</code> and <code>B∪D</code>.
			 * 
			 * @param <Sig>
			 * 		Type of feature (subset) significance.
			 * @param updateInfo
			 * 		{@link DynamicUpdateInfoPack} that contains {@link PreviousInfoPack#getInstances()}
			 * 		(<strong>S<sub>U</sub><sup>N</sup></strong>) and
			 * 		{@link DynamicUpdateInfoPack#getAlteredAttributeValues()}.
			 * @param previousTolerancesOfCondAttrs
			 * 		The previous tolerance classes obtained using conditional attributes:
			 * 		<strong>T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)</strong>
			 * @param previousTolerancesOfCondAttrsNDecAttr
			 * 		The conditional attributes that were used to obtain the previous tolerance classes:
			 * 		<strong>T<sub>B∪D</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)</strong>
			 * @param previousToleranceCondAttrs
			 * 		The conditional attributes used to obtain <code>previousTolerancesOfCondAttrs</code>:
			 * 		<strong>B</strong>
			 * @param toleranceClassObtainer
			 *      {@link ToleranceClassObtainer} instance for obtaining tolerance classes.
			 * @param calculation
			 * 		{@link FeatureImportance4XieDynamicIncompleteDSReduction} instance.
			 * @return {@link DynamicUpdateResult}
			 * @throws InstantiationException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws IllegalAccessException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws IllegalArgumentException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws InvocationTargetException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws NoSuchMethodException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws SecurityException if exceptions occur when copying <code>previousTolerances</code>.
			 */
			public static <Sig extends Number> DynamicUpdateResult<Sig> updateSignificance4AttributeRelated(
					DynamicUpdateInfoPack updateInfo,
					Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs,
					Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttr,
					Collection<Integer> previousToleranceCondAttrs,
					ToleranceClassObtainer toleranceClassObtainer, 
					FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation
			) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
					InvocationTargetException, NoSuchMethodException, SecurityException
			{
				// Obtain updated Tolerance classes: T<sub>B</sub>
				Map<Instance, Collection<Instance>> latestTolerancesOfReduct =
					updateToleranceClass4AttributeRelatedUpdate(
							updateInfo,
							previousToleranceCondAttrs, 
							previousTolerancesOfCondAttrs,
							true, 
							toleranceClassObtainer
					);	
				// Obtain updated Tolerance classes with decision attribute: T<sub>B∪D</sub>
				Collection<Integer> previousToleranceCondAttrsNDecAttr = new ArrayList<>(
						previousToleranceCondAttrs.size()+1
				);
				previousToleranceCondAttrsNDecAttr.addAll(previousToleranceCondAttrs);
				previousToleranceCondAttrsNDecAttr.add(0);
				
				Map<Instance, Collection<Instance>> latestTolerancesWithDecAttr =
					updateToleranceClass4AttributeRelatedUpdate(
							updateInfo,
							previousToleranceCondAttrsNDecAttr, 
							previousTolerancesOfCondAttrsNDecAttr,
							true,
							toleranceClassObtainer
					);
				
				// Calculate feature significance.
				calculation.calculate(latestTolerancesOfReduct, latestTolerancesWithDecAttr);
				Sig sig = calculation.getResult();
				
				return new DynamicUpdateResult<>(sig, latestTolerancesOfReduct, latestTolerancesWithDecAttr);
			}
		
			/**
			 * Update significance for both-related update by updating tolerance classes of 
			 * <code>B</code> and <code>B∪D</code>.
			 * 
			 * @param <Sig>
			 * 		Type of feature (subset) significance.
			 * @param updateInfo
			 *      {@link DynamicUpdateInfoPack} that contains updated info.
			 * @param previousTolerancesOfCondAttrs
			 * 		The previous tolerance classes obtained using conditional attributes:
			 * 		<strong>T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)</strong>
			 * @param previousTolerancesOfCondAttrsNDecAttr
			 * 		The conditional attributes that were used to obtain the previous tolerance classes:
			 * 		<strong>T<sub>B∪D</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)</strong>
			 * @param previousToleranceCondAttrs
			 * 		The conditional attributes used to obtain <code>previousTolerancesOfCondAttrs</code>:
			 * 		<strong>B</strong>
			 * @param toleranceClassObtainer
			 *      {@link ToleranceClassObtainer} instance for obtaining tolerance classes.
			 * @param calculation
			 * 		{@link FeatureImportance4XieDynamicIncompleteDSReduction} instance.
			 * @return {@link DynamicUpdateResult}
			 * @throws InstantiationException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws IllegalAccessException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws IllegalArgumentException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws InvocationTargetException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws NoSuchMethodException if exceptions occur when copying <code>previousTolerances</code>.
			 * @throws SecurityException if exceptions occur when copying <code>previousTolerances</code>.
			 */
			public static <Sig extends Number> DynamicUpdateResult<Sig> updateSignificance4BothRelated(
					DynamicUpdateInfoPack updateInfo,
					Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs,
					Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttr,
					Collection<Integer> previousToleranceCondAttrs,
					ToleranceClassObtainer toleranceClassObtainer, 
					FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation
			) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
					InvocationTargetException, NoSuchMethodException, SecurityException
			{
				// Obtain updated Tolerance classes: T<sub>B</sub>
				Map<Instance, Collection<Instance>> latestTolerancesOfReduct =
					updateToleranceClass4BothRelatedUpdate(
							updateInfo,
							previousToleranceCondAttrs, 
							previousTolerancesOfCondAttrs,
							true,
							toleranceClassObtainer
					);	
				// Obtain updated Tolerance classes with decision attribute: T<sub>B∪D</sub>
				Collection<Integer> previousToleranceCondAttrsNDecAttr = new ArrayList<>(
						previousToleranceCondAttrs.size()+1
				);
				previousToleranceCondAttrsNDecAttr.addAll(previousToleranceCondAttrs);
				previousToleranceCondAttrsNDecAttr.add(0);
				
				Map<Instance, Collection<Instance>> latestTolerancesWithDecAttr =
					updateToleranceClass4BothRelatedUpdate(
							updateInfo,
							previousToleranceCondAttrsNDecAttr, 
							previousTolerancesOfCondAttrsNDecAttr,
							true,
							toleranceClassObtainer
					);
				
				// Calculate feature significance.
				calculation.calculate(latestTolerancesOfReduct, latestTolerancesWithDecAttr);
				Sig sig = calculation.getResult();
				
				return new DynamicUpdateResult<>(sig, latestTolerancesOfReduct, latestTolerancesWithDecAttr);
			}
		}
		
		/**
		 * Update tolerance classes for attributed-related update.
		 * 
		 * @see Common.TransformPrevious2Latest#exec(Map, DynamicUpdateInfoPack)
		 * 
		 * @param updateInfo
		 * 		{@link DynamicUpdateInfoPack} that contains {@link PreviousInfoPack#getInstances()},
		 * 		{@link AlteredAttributeValues#getSelectedUnalteredAttributes()} and 
		 * 		{@link AlteredAttributeValues}.
		 * @param previousToleranceAttributes
		 * 		The previous attributes used to calculate the <code>previousTolerances</code>: 
		 * 		<strong>B</strong>.
		 * @param previousTolerances
		 * 		Previous tolerance classes: 
		 * 		<strong>T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x), x in S<sub>U</sub><sup>N</sup></strong>
		 * @param transformPrevious2Latest
		 * 		Whether to transform the previous {@link Instance}s into the latest ones(with
		 * 		altered attributes' values applied) or not.
		 * @return Updated Tolerance classes.
		 * @throws InstantiationException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws IllegalAccessException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws IllegalArgumentException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws InvocationTargetException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws NoSuchMethodException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws SecurityException if exceptions occur when copying <code>previousTolerances</code>.
		 */
		public static Map<Instance, Collection<Instance>> updateToleranceClass4AttributeRelatedUpdate(
				DynamicUpdateInfoPack updateInfo,
				Collection<Integer> previousToleranceAttributes, 
				Map<Instance, Collection<Instance>> previousTolerances,
				boolean transformPrevious2Latest,
				ToleranceClassObtainer toleranceClassObtainer
		) throws InstantiationException, IllegalAccessException, IllegalArgumentException, 
					InvocationTargetException, NoSuchMethodException, SecurityException
		{
			// Unpack parameters of this method.
			Collection<Instance> originalInstances = updateInfo.getPreviousInfo().getInstances();
			Collection<Integer> selectedUnalteredInPreviousToleranceAttributes = 
					updateInfo.getAlteredAttributeValues().getSelectedUnalteredAttributes();
			AlteredAttributeValues alteredAttributeValues = updateInfo.getAlteredAttributeValues();
			
			// Initialize T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x) =
			//		T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)
			@SuppressWarnings("unchecked")
			Map<Instance, Collection<Instance>> tolerances =
				Basic
					.copyToleranceClass(previousTolerances, HashSet.class);
			// if B∩C<sub>ALT</sub>=∅ then
			//	return T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x)
			Collection<Integer> previousToleranceAttrsAltered = 
				previousToleranceAttributes.stream()
					.filter(attr->alteredAttributeValues.attributeValueIsAltered(attr))
					.collect(Collectors.toSet());
			if (previousToleranceAttrsAltered.isEmpty())	return tolerances;
			// else
			// 	C<sub>ALT</sub>=B∩C<sub>ALT</sub>
			Collection<Integer> affectedAlteredAttrs = 
				alteredAttributeValues.getAlteredAttributes().stream()
					.filter(attr->previousToleranceAttrsAltered.contains(attr))
					.collect(Collectors.toSet());
			// for i=1 to |U| do
			//	for each x[k] in 
			//			T<sub>P</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x[i]) - 
			//			T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x[i])
			//	do
			//		if (x[k], x[i]) in T(B-P-C<sub>ALT</sub>) then
			//			T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ← 
			//				T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ∪ {x[i]}
			//			T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) ← 
			//				T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) ∪ {x[k]}
			// ------------------------------------------------------------------------------------
			// | Calculate T<sub>P</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x) for later use.	  |
			// ------------------------------------------------------------------------------------
			Map<Instance, Collection<Instance>> tolerancesOfSelectedUnalteredPreviousToleranceAttrs =
				Basic.toleranceClass(
					originalInstances,
					new IntegerCollectionIterator(selectedUnalteredInPreviousToleranceAttributes),
					toleranceClassObtainer,
					toleranceClassObtainer.getCacheInstanceGroups(
						originalInstances, 
						new IntegerCollectionIterator(selectedUnalteredInPreviousToleranceAttributes)
					)
				);
			Common
				.updateToleranceClass4AttributesDeleted(
					tolerances, originalInstances,
					previousToleranceAttributes, 
					selectedUnalteredInPreviousToleranceAttributes, 
					tolerancesOfSelectedUnalteredPreviousToleranceAttrs,
					alteredAttributeValues.getAlteredAttributes()
				);
			
			// ------------------------------------------------------------------------------------
			// | Calculate B-C<sub>ALT</sub> for later use.										  |
			// | 	B is the previous reduct, C<sub>ALT</sub> is the altered attributes.		  |
			// ------------------------------------------------------------------------------------
			Collection<Integer> bMinusCAlt = new HashSet<>(previousToleranceAttributes);
			bMinusCAlt.removeAll(affectedAlteredAttrs);
			IntegerIterator bMinusCAltIterator = new IntegerCollectionIterator(bMinusCAlt);
			// ----------------------------------------------------------------------------------------------------
			// | Calculate T<sub>B-C<sub>ALT</sub></sub><sup>S<sub>U</sub><sup>N</sup></sup>(x) for later use.	  |
			// ----------------------------------------------------------------------------------------------------
			Map<Instance, Collection<Instance>> tolerancesOfPreviousReductMinusAltered =
				Basic
					.toleranceClass(
						originalInstances, bMinusCAltIterator,
						toleranceClassObtainer,
						toleranceClassObtainer.getCacheInstanceGroups(
							originalInstances, bMinusCAltIterator
						)
					);
			
			// for n=1 to |U| do
			Collection<Instance> tolerancesOfIns;
			IntegerIterator cAltIterator = new IntegerCollectionIterator(affectedAlteredAttrs);
			for (Instance ins: originalInstances) {
				// for each x[m] in T<sub>B-C<sub>ALT</sub></sub><sup>S<sub>U</sub><sup>N</sup></sup>(x[n])
				Collection<Instance> tolerancesOfInsOfPreviousRedMinusAltered =
						tolerancesOfPreviousReductMinusAltered.get(ins);
				for (Instance xm: tolerancesOfInsOfPreviousRedMinusAltered) {
					// if (x[m], x[n]) not in T(C<sub>ALT</sub>*) then
					if (!Common
							.tolerant(xm, ins, cAltIterator, alteredAttributeValues)
					) {
						// --------------------------------------------------------------------------------
						// | In the original pseudo code of the >Algorithm 3< in the paper, the following |
						// |  2 calculations are:														  |
						// |  > T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ← 			  |
						// |	 T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ∪			  |
						// |	 {x[i]}																	  |
						// |  > T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) ← 			  |
						// |	 T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) ∪			  |
						// |	 {x[k]}																	  |
						// |  which could be mistaken, cause they are the same as the above calculations  |
						// |  when using B-P-C<sub>ALT</sub> and do not make sense to what the authors 	  |
						// |  implied in the paper in >Proposition 2<.		 							  |
						// | So at the below implementations, I've changed x[k] into x[m], x[i] into	  | 
						// | x[n], and changed the calculation from "∪" into "-", which removes 		  |
						// | (x[m], x[n]) that isn't tolerant with each other. By doing so, a more 		  |
						// | reasonable result is acquired.												  |
						// --------------------------------------------------------------------------------
						// T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[m]) ← 
						//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[m]) -
						//	{x[n]}
						tolerancesOfIns = tolerances.get(xm);
						if (tolerancesOfIns==null)		tolerances.put(xm, tolerancesOfIns=new HashSet<>());
						if (!tolerancesOfIns.isEmpty())	tolerancesOfIns.remove(ins);
						// T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[n]) ← 
						//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[n]) -
						//	{x[m]}
						tolerancesOfIns = tolerances.get(ins);
						if (tolerancesOfIns==null)		tolerances.put(ins, tolerancesOfIns=new HashSet<>());
						if (!tolerancesOfIns.isEmpty())	tolerancesOfIns.remove(xm);
					}
				}
			}
			// Transform previous instances to latest ones.
			if (transformPrevious2Latest)
				tolerances = Common.TransformPrevious2Latest.exec(tolerances, updateInfo);
			return tolerances;
		}

		/**
		 * Update tolerance classes for both-related update.
		 * 
		 * @param updateInfo
		 * 		{@link DynamicUpdateInfoPack} that contains {@link PreviousInfoPack#getInstances()},
		 * 		{@link DynamicUpdateInfoPack#getAlteredAttributeValues()} and 
		 * 		{@link DynamicUpdateInfoPack#getAlteredObjects()}.
		 * @param previousToleranceAttributes
		 * 		The previous attributes used to calculate the <code>previousTolerances</code>: 
		 * 		<strong>B</strong>.
		 * @param previousTolerances
		 * 		Previous tolerance classes: 
		 * 		<strong>T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x), x in S<sub>U</sub><sup>N</sup></strong>
		 * @param transformPrevious2Latest
		 * 		Whether to transform the previous {@link Instance}s into the latest ones(with
		 * 		altered attributes' values applied) or not.
		 * @return Updated Tolerance classes.
		 * @throws InstantiationException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws IllegalAccessException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws IllegalArgumentException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws InvocationTargetException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws NoSuchMethodException if exceptions occur when copying <code>previousTolerances</code>.
		 * @throws SecurityException if exceptions occur when copying <code>previousTolerances</code>.
		 */
		public static Map<Instance, Collection<Instance>> updateToleranceClass4BothRelatedUpdate(
				DynamicUpdateInfoPack updateInfo,
				Collection<Integer> previousToleranceAttributes, 
				Map<Instance, Collection<Instance>> previousTolerances,
				boolean transformPrevious2Latest,
				ToleranceClassObtainer toleranceClassObtainer
		) throws InstantiationException, IllegalAccessException, IllegalArgumentException, 
					InvocationTargetException, NoSuchMethodException, SecurityException
		{
			Collection<Instance> originalInstances = updateInfo.getPreviousInfo().getInstances();
			Collection<Integer> allAlteredAttributes = updateInfo.countAlteredAttributes();
			Collection<Instance> allAlteredInstances = updateInfo.countAlteredInstances();
			
			// Initialize T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x) =
			//		T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)
			@SuppressWarnings("unchecked")
			Map<Instance, Collection<Instance>> tolerances =
				Basic
					.copyToleranceClass(previousTolerances, HashSet.class);
			// if B∩C<sub>ALT</sub>=∅ then
			//	return T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x)
			Collection<Integer> previousToleranceAttrsAltered = 
				previousToleranceAttributes.stream()
					.filter(attr->allAlteredAttributes.contains(attr))
					.collect(Collectors.toSet());
			if (previousToleranceAttrsAltered.isEmpty()) {
				return transformPrevious2Latest?
						Common
							.TransformPrevious2Latest
							.exec(tolerances, updateInfo):
						tolerances;
			}
			// else
			// 	C<sub>ALT</sub>=B∩C<sub>ALT</sub>
			Collection<Integer> affectedAlteredAttrs = previousToleranceAttrsAltered;
			IntegerIterator affectedAlteredAttrsIterator = new IntegerCollectionIterator(affectedAlteredAttrs);
			// for each x in U do
			for (Instance ins: originalInstances) {
				// if x in U - U<sub>ALT</sub> then
				//	(i.e. x is not altered)
				if (!updateInfo.hasAnyAlter(ins)) {
					//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) ←
					//		T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) - U<sub>ALT</sub>
					tolerances.get(ins).removeAll(allAlteredInstances);
				// else
				//	(i.e. x is altered)
				}else{
					//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]) ← x
					Collection<Instance> toleranceOfIns = new HashSet<>();
					toleranceOfIns.add(ins);
					tolerances.put(ins, toleranceOfIns);
				}
			}
			//	/* this process updates the related and unrelated objects' tolerance */
			
			// for each x' in U<sub>ALT</sub>
			//	Compute T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x')
			Map<Instance, Collection<Instance>> tolerancesOfAlteredInsWithUnalteredAttrs =
				allAlteredInstances.stream()
					.collect(Collectors.toMap(
						Function.identity(), objAlteredIns->new HashSet<>(tolerances.get(objAlteredIns))
					));
			Map<Instance, Collection<Instance>> tolerancesOfAttrAlteredInsWithUnalteredAttrs =
				Basic
					.toleranceClass(
						allAlteredInstances, originalInstances, 
						new IntegerArrayIterator(new int[0]), 
						toleranceClassObtainer, null
					);
			
			Common
				.updateToleranceClass4AttributesDeleted(
						tolerancesOfAlteredInsWithUnalteredAttrs, allAlteredInstances, 
						previousToleranceAttributes, 
						Collections.emptyList(), tolerancesOfAttrAlteredInsWithUnalteredAttrs,
						allAlteredAttributes
				);
			
			// for each x' in U<sub>ALT</sub>
			for (Instance alteredIns: allAlteredInstances) {
				// for each x[k] in 
				//	T<sub>B-C<sub>ALT</sub></sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x) - 
				//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x)
				// do
				Collection<Instance> tolerancesOfInsOfUnalteredAttr =
						tolerancesOfAlteredInsWithUnalteredAttrs.get(alteredIns);
				Collection<Instance> tolerancesOfInsOfPreviousToleranceAttrs =
						tolerances.get(alteredIns);
				for (Instance xk: tolerancesOfInsOfUnalteredAttr) {
					// (skip ones in T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x))
					if (tolerancesOfInsOfPreviousToleranceAttrs.contains(xk))	continue;
					// if (x', x[k]) in T(C<sub>ALT</sub>) then
					if (Basic
							.tolerant(alteredIns, xk, affectedAlteredAttrsIterator)
					) {
						//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x') ←
						//		T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x') + {x[k]}
						tolerancesOfInsOfPreviousToleranceAttrs.add(xk);
						//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ←
						//		T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) + {x'}
						Collection<Instance> tolerancesOfIns = tolerances.get(xk);
						if (tolerancesOfIns==null)	tolerances.put(xk, tolerancesOfIns=new HashSet<>());
						tolerancesOfIns.add(alteredIns);
					}
				}
			}

			// Transform previous instances to latest ones.
			if (transformPrevious2Latest) {
					Common
						.TransformPrevious2Latest
						.exec(tolerances, updateInfo);
			}
			
			return tolerances;
		}
	}
	
	/**
	 * Get the core of the given {@link Instance}s.
	 * 
	 * @param <Sig>
	 * 		Type of feature importance.
	 * @param instances
	 * 		{@link Instance}s.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @param globalSignificance
	 * 		The global significance.
	 * @param completeDataOfAttributes
	 * 		{@link InstancesCollector} that contains complete data
	 * 		(in <strong><code>instances</code></strong>) for every attributes(in
	 * 		<code>attributes</code>).
	 * @param calculation
	 * 		{@link FeatureImportance4XieDynamicIncompleteDSReduction} instance.
	 * @param deviation
	 * 		Acceptable deviation when calculating significance of attributes. Consider equal when the 
	 * 		difference between two sig is less than the given deviation value.
	 * @return
	 */
	public static <Sig extends Number> Collection<Integer> core(
		Collection<Instance> instances, int[] attributes, Sig globalSignificance,
		ToleranceClassObtainer toleranceClassObtainer,
		InstancesCollector completeDataOfAttributes,
		FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation, Sig deviation
	){
		Collection<Integer> core = new LinkedList<>();
		
		int[] examing = new int[attributes.length-1];
		for (int i=0; i<attributes.length-1; i++)	examing[i] = attributes[i+1];
		
		Sig sig;
		for (int i=0; i<attributes.length; i++) {
			sig = calculation.calculate(
						instances, new IntegerArrayIterator(examing),
						toleranceClassObtainer, completeDataOfAttributes
					).getResult();
			
			if (calculation.value1IsBetter(globalSignificance, sig, deviation))
				core.add(attributes[i]);
			
			if (i<examing.length)	examing[i] = attributes[i];
		}
		return core;
	}

	/**
	 * Get the most significant attribute 
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @param reduct
	 * 		Reduct attributes.
	 * @param completeDataOfAttributes
	 * 		{@link InstancesCollector} that contains complete data
	 * 		(in <strong><code>instances</code></strong>) for every attributes(in
	 * 		<code>attributes</code>).
	 * @param calculation
	 * 		{@link FeatureImportance4XieDynamicIncompleteDSReduction} instance.
	 * @param deviation
	 * 		Acceptable deviation when calculating significance of attributes. Consider equal when the 
	 * 		difference between two sig is less than the given deviation value.
	 * @return
	 */
	public static <Sig extends Number> MostSignificantAttributeResult<Sig> mostSignificantAttribute(
			Collection<Instance> instances, int[] attributes,
			Collection<Integer> reduct,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeDataOfAttributes,
			FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation, Sig deviation
	) {
		int sigAttr = -1;
		Sig sigValue = null;
		
		int i=0;	int[] attribute = new int[reduct.size()+1];	for (int r : reduct) attribute[i++] = r;
		
		for (int attr: attributes) {
			if (!reduct.contains(attr)){
				attribute[attribute.length-1] = attr;
				Sig sig = calculation.calculate(
							instances, new IntegerArrayIterator(attribute),
							toleranceClassObtainer, completeDataOfAttributes
						).getResult();
				if (sigAttr==-1 || calculation.value1IsBetter(sig, sigValue, deviation)) {
					sigAttr = attr;
					sigValue = sig;
				}
			}
		}
		
		return new MostSignificantAttributeResult<Sig>(sigAttr, sigValue);
	}
	
	public static <Sig extends Number> void inspection(
			Collection<Instance> instances, Collection<Integer> reduct,
			Sig redSig, 
			ToleranceClassObtainer toleranceClassObtainer, InstancesCollector completeDataOfAttribuets,
			FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation, Sig deviation
	){
		Integer[] reductCopy = reduct.toArray(new Integer[reduct.size()]);
		for (int attr: reductCopy) {
			reduct.remove(attr);
			Sig sig = calculation.calculate(
						instances, new IntegerCollectionIterator(reduct),
						toleranceClassObtainer, completeDataOfAttribuets
					).getResult();
			if (calculation.value1IsBetter(redSig, sig, deviation))
				reduct.add(attr);
		}
	}
}