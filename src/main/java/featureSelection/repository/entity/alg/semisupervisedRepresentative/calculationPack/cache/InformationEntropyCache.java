package featureSelection.repository.entity.alg.semisupervisedRepresentative.calculationPack.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.semisupervisedRepresentative.SemisupervisedRepresentativeAlgorithm;
import featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.informationEntropy.semisupervisedRepresentative.InformationEntropyCalculation4SemisupervisedRepresentative;
import lombok.Getter;

/**
 * An entity for cache for information entropy in calculations:
 * <ul>
 *     <li>
 *         <strong>{@link #infoEntropyByAllUniverses}</strong> to contain info. entropy values
 *         on labeled+unlabeled {@link Instance}s for each attribute as cache usage.
 *     </li>
 *     <li>
 *         <strong>{@link #infoEntropyByLabeledUniverses}</strong> to contain info. entropy values
 *         on labeled {@link Instance}s for each attribute as cache usage.
 *     </li>
 * </ul>
 * <p>
 * Relevant calculations are executed in {@link #calculate(int,
 * InformationEntropyCalculation4SemisupervisedRepresentative, Collection, Map) calculate()}
 *
 * @author Benjamin_L
 */
@Getter
public class InformationEntropyCache {
	private Map<Integer, InformationEntropyCacheResult> infoEntropyByAllUniverses;
	private Map<Integer, InformationEntropyCacheResult> infoEntropyByLabeledUniverses;
	
	public InformationEntropyCache(int attributeSize) {
		infoEntropyByAllUniverses = new HashMap<>(attributeSize);
		infoEntropyByLabeledUniverses = new HashMap<>(attributeSize);
	}
	
	/**
	 * Calculate <i>H(D/{a})</i>, where <i>D</i> is the decision attribute and <i>{a}</i> is an
	 * conditional attribute, using labeled + unlabeled {@link Instance}s.
	 * <p>
	 * <strong>Notice</strong>:
	 * If calculation is executed, <strong><i>U/{a}</i></strong> is calculated in order to calculate
	 * <i>H(D/{a})</i>.
	 * 
	 * @param attribute
	 * 		Attribute to be calculated: <strong><i>a</i></strong>
	 * @param calculation
	 * 		{@link InformationEntropyCalculation4SemisupervisedRepresentative} instance.
	 * @param allInstances
	 * 		A {@link Collection} of labeled and unlabeled {@link Instance}s.
	 * @return Conditional Information entropy of <i>D</i> related to <i>{a}</i>:
	 *      <strong>H(D/{a})</strong>, wrapped in {@link InformationEntropyCacheResult}
	 */
	public InformationEntropyCacheResult calculateByAllUniverses(
			int attribute, InformationEntropyCalculation4SemisupervisedRepresentative calculation,
			Collection<Instance> allInstances
	) {
		return calculate(attribute, calculation, allInstances, infoEntropyByAllUniverses);
	}

	/**
	 * Calculate <i>H(D/{a})</i>, where <i>D</i> is the decision attribute and <i>{a}</i> is an
	 * conditional attribute, using labeled {@link Instance}s.
	 * <p>
	 * <strong>Notice</strong>:
	 * If calculation is executed, <strong><i>U/{a}</i></strong> is calculated in order to calculate
	 * <i>H(D/{a})</i>.
	 * 
	 * @param attribute
	 * 		Attribute to be calculated: <strong>a</strong>
	 * @param calculation
	 * 		{@link InformationEntropyCalculation4SemisupervisedRepresentative} instance.
	 * @param labeledInstances
	 * 		A {@link Collection} of labeled {@link Instance}s.
	 * @return Conditional Information entropy of <i>D</i> related to <i>{a}</i>:
	 *      <strong>H(D/{a})</strong>, wrapped in {@link InformationEntropyCacheResult}
	 */
	public InformationEntropyCacheResult calculateByLabeledUniverses(
			int attribute, InformationEntropyCalculation4SemisupervisedRepresentative calculation, 
			Collection<Instance> labeledInstances
	) {
		return calculate(attribute, calculation, labeledInstances, infoEntropyByLabeledUniverses);
	}
	
	/**
	 * Calculate <i>H(D/{a})</i>, where <i>D</i> is the decision attribute and <i>{a}</i> is an
	 * conditional attribute, using the given {@link Instance}s.
	 * <p>
	 * <strong>Notice</strong>: If calculation is needed, <strong>U/{a}</strong> is
	 * calculated in order to calculate H(D/{a}).
	 * 
	 * @see InformationEntropyCalculation4SemisupervisedRepresentative#calculate(Collection, Object...)
	 * 
	 * @param attribute
	 * 		Attribute to be calculated: <strong>a</strong>
	 * @param calculation
	 * 		{@link InformationEntropyCalculation4SemisupervisedRepresentative} instance.
	 * @param instances
	 * 		A {@link Collection} of labeled and unlabeled {@link Instance}s.
	 * @param cache
	 * 		A {@link Map} contains info. entropy value history whose keys are attributes
	 * 		of {@link Instance} and values are info. entropy values.
	 * @return Conditional Information entropy of <i>D</i> related to <i>{a}</i>:
	 *      <strong>H(D/{a})</strong>, wrapped in {@link InformationEntropyCacheResult}
	 */
	private InformationEntropyCacheResult calculate(
			int attribute, InformationEntropyCalculation4SemisupervisedRepresentative calculation, 
			Collection<Instance> instances,
			Map<Integer, InformationEntropyCacheResult> cache
	) {
		InformationEntropyCacheResult cacheValue = cache.get(attribute);
		//	if exists in cache, use cache value.
		if (cacheValue!=null) {
			// do nothing.
		//	if doesn't, calculate
		}else {
			// Obtain U/F[i]
			Collection<Collection<Instance>> equClassesOfLabeledU =
				SemisupervisedRepresentativeAlgorithm
					.Basic
					.equivalenceClass(instances, new IntegerArrayIterator(attribute))
					.values();
			// Calculate H(F[i]), bases on U/F[i]
			infoEntropyByLabeledUniverses.put(
				attribute,
				cacheValue =
						new InformationEntropyCacheResult(
								calculation.calculate(equClassesOfLabeledU, instances.size())
											.getResult().doubleValue(),
								equClassesOfLabeledU
						)
			);
		}
		return cacheValue;
	}
}