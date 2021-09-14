package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.MixedEquivalenceClassSequentialList;

import java.util.Collection;

/**
 * An interface for Feature Importance calculation of Liang incremental Feature Selection
 * algorithm.
 * <p>
 * Implementations should base on the original paper
 * <a href="https://ieeexplore.ieee.org/document/6247431/">"A Group Incremental Approach to
 * Feature Selection Applying Rough Set Technique"</a> by Jiye Liang, Feng Wang, Chuangyin Dang,
 * Yuhua Qian.
 *
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of feature significance.
 */
public interface FeatureImportance4LiangIncremental<Sig extends Number>
		extends FeatureImportance<Sig>
{
	/**
	 * Calculate.
	 * 
	 * @param instances
	 * 		{@link Instance} {@link Collection} as Universe before adding the new instance.
	 * @param equClass
	 * 		An Equivalence Class that has the same Equivalence relation(key) as
	 * 		<code>newInstance</code>.
	 * @param newInstance
	 * 		The instance added.
	 * @param previousSig
	 * 		Previous significance of <code>instances</code>.
	 * @param previousSigWithDenominator
	 * 		Whether the calculation(entropy mostly) of <code>previousSig</code> contains denominator.
	 * @param attributeLength
	 * 		The length of the {@link Instance} conditional attribute.
	 * @return <code>this</code>.
	 */
	FeatureImportance4LiangIncremental<Sig> calculate(
			Collection<Instance> instances, Collection<Instance> equClass,
			Instance newInstance, Sig previousSig, boolean previousSigWithDenominator,
			int attributeLength
	);

	/**
	 * Calculate.
	 * 
	 * @param equClassesCMBResult
	 * 		Combined Equivalence Classes result in {@link MixedEquivalenceClassSequentialList} on B:
	 * 		<strong>(U∪U<sub>X</sub>)/B</strong>
	 * @param decEquClassesCMBResult
	 * 		Combined Equivalence Classes result in {@link MixedEquivalenceClassSequentialList} on D:
	 * 		<strong>(U∪U<sub>X</sub>)/D</strong>
	 * @param previousInstance
	 * 		The number previous {@link Instance}.
	 * @param newInstance
	 * 		The number new {@link Instance}.
	 * @param previousSig
	 * 		Sig value of previous Universe instances on B: <strong>Entropy<sub>U</sub>(D|B)</strong>.
	 * @param newSig
	 * 		Sig value of new Universe instances on B: <strong>Entropy<sub>U<sub>X</sub></sub>(D|B)</strong>.
	 * @param previousSigWithDenominator
	 * 		Whether the calculation(entropy mostly) of <code>previousSig</code> contains denominator.
	 * @param attributeLength
	 * 		The length of the {@link Instance} conditional attribute.
	 * @return <code>this</code>.
	 */
	FeatureImportance4LiangIncremental<Sig> calculate(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult,
			int previousInstance, int newInstance,
			Sig previousSig, Sig newSig, boolean previousSigWithDenominator, int attributeLength
	);
}