package featureSelection.repository.support.calculation.alg.semisupervisedRepresentative.featureRelevance;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.ConditionalEntropyCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.MutualInformationEntropyCalculation;
import lombok.Data;

/**
 * An entity for <strong>Semi-supervised Representative</strong> Feature Relevance calculation
 * parameters.
 * <p>
 * (F_Rel) With the following fields/parameters:
 * <ul>
 * 	<li><strong>{@link #equClassesOfLabeledUniverses}</strong>: {@link Items}
 * 		<p>
 * 		Equivalence Classes induced by <i>F<sub>i</sub></i>: <strong><i>U/F<sub>i</sub></i></strong> with
 * 		labeled + unlabeled {@link Instance}s.
 * 	</li>
 * 	<li><strong>{@link #infoEntropyOfEquClassesOfAllUniverses}</strong>: <code>double</code>
 * 		<p>
 * 		Info. entropy value of Equivalence Classes induced by <i>F<sub>i</sub></i> with labeled +
 * 	    unlabeled {@link Instance}s: <strong><i>H(F<sub>i</sub>)</i></strong>.
 * 	</li>
 * 	<li><strong>{@link #condEquClassesOfLabeledUniverses}</strong>: {@link Items}
 * 		<p>
 * 		Equivalence Classes induced by <i>C</i>: <strong><i>(labeled U)/C</i></strong> with labeled
 * 		{@link Instance}s.
 * 	</li>
 * 	<li><strong>{@link #infoEntropyOfCondEquClassesOfLabeledUniverses}</strong>: <code>double</code>
 * 		<p>
 * 		Info. entropy value of <i>C</i>: <strong><i>H(C)</i></strong> using labeled {@link Instance}s.
 * 	</li>
 * 	<li><strong>{@link #condAttributes}</strong>: {@link IntegerIterator}
 * 		<p>
 * 		Attributes used in the partitioning of <i>U/C</i>, i.e. <strong><i>C</i></strong>.
 * 	</li>
 * 	<li><strong>tradeOff</strong>: <code>double</code>
 * 		<p> Supervised/Semi-supervised/Un-supervised trade-off, marked as <strong>&beta;</strong>.
 * 		<p>	<strong>supervised</strong> only:    &beta; = <strong>0</strong>;
 * 		<p>	<strong>un-supervised</strong> only: &beta; = <strong>1</strong>;
 * 		<p>	<strong>semi-supervised</strong>:    &beta; = <strong>(0, 1)</strong>.
 * 	</li>
 * 	<li><strong>condEntropyCalculation</strong>: <code>condEntropyCalculation</code>
 * 		<p>
 * 		{@link CondEntropyCal} for <strong><i>H(F<sub>i</sub>|F<sub>j</sub>)</i></strong> calculation.
 * 	</li>
 * 	<li><strong>mutualInfoEntropyCalculation</strong>: <code>InformationEntropyCalculation</code>
 * 		<p>
 * 		{@link MutualInfoEntropyCal} for <strong><i>I(F<sub>i</sub>; F<sub>j</sub>)</i></strong>
 *      calculation.
 * 	</li>
 * 	<li><strong>args</strong>: <code>Object[]</code>>
 * 		<p>	Extra arguments including:
 * 		<p>	· Labeled {@link Instance} number in <code>int</code>: <strong>|labeled U|</strong>
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Data
public class FeatureRelevanceParams4SemisupervisedRepresentative<
		Items,
		CondEntropyCal extends ConditionalEntropyCalculation,
		MutualInfoEntropyCal extends MutualInformationEntropyCalculation>
{
	/**
	 * (labeled U)/F<sub>i</sub>
	 */
	private Items equClassesOfLabeledUniverses;
	/**
	 * H(F<sub>i</sub>), bases on U/F<sub>i</sub>
	 */
	private double infoEntropyOfEquClassesOfAllUniverses;

	/**
	 * (labeled U)/C
	 */
	private Items condEquClassesOfLabeledUniverses;
	/**
	 * H(C)
	 */
	private double infoEntropyOfCondEquClassesOfLabeledUniverses;
	/**
	 * C
	 */
	private IntegerIterator condAttributes;
	
	/**
	 * &beta;
	 */
	private double tradeOff;
	
	private CondEntropyCal condEntropyCalculation;
	private MutualInfoEntropyCal mutualInfoEntropyCalculation;
	
	private Object[] args;
}