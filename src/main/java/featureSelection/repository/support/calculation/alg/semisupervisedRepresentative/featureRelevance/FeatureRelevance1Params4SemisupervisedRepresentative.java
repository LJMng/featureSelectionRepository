package featureSelection.repository.support.calculation.alg.semisupervisedRepresentative.featureRelevance;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.ConditionalEntropyCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.MutualInformationEntropyCalculation;
import featureSelection.repository.support.calculation.markovBlanket.approximate.symmetricalUncertainty.mutualInformationEntropy.MutualInformationEntropyBasedSymmetricalUncertaintyCalculation;
import lombok.Data;

/**
 * An entity for <strong>Semi-supervised Representative</strong> Feature Relevance 1
 * calculation parameters.
 * (F_Rel1) With the following fields/parameters:
 * <ul>
 * 	<li><strong>{@link #labeledUniverses}</strong>: {@link Collection}
 * 		<p>	Labeled {@link Instance} {@link Collection}. (i.e. <strong>labeled U</strong>)
 * 	</li>
 * 	<li><strong>{@link #allUniverses}</strong>: {@link Collection}
 * 		<p>	Labeled+Unlabeled {@link Instance} {@link Collection}. (i.e. <strong>U</strong>)
 * 	</li>
 * 	<li><strong>{@link #attributes}</strong>: <code>int[]</code>
 * 		<p>	All attributes of {@link Instance}.
 * 	</li>
 * 	<li><strong>{@link #equClassesAttribute}</strong>: <code>int</code>
 * 		<p>	Attributes used in Equivalent Classes partitioning: <strong>F<sub>i</sub></strong>.
 * 			For calculations of UI(F<sub>i</sub>), H(F<sub>i</sub> | F<sub>j</sub>) where j
 * 			in 1:n and j!=i and n = |attributes|.
 * 	</li>
 * 	<li><strong>{@link #equClassesOfLabeledUniverses}</strong>: {@link Collection}
 * 		<p>	Equivalent Classes partitioned by F<sub>i</sub>:
 * 			<strong>(labeled U)/F<sub>i</sub></strong>.
 * 			For calculations of H(C|F<sub>i</sub>).
 * 	</li>
 * 	<li><strong>{@link #infoEntropyOfEquClassesOfLabeledUniverses}</strong>: <code>double</code>
 * 		<p>	Information entropy of F<sub>i</sub>: <strong>H(F<sub>i</sub>)</strong>, using
 * 			labeled {@link Instance}s only.
 * 	</li>
 * 	<li><strong>{@link #infoEntropyOfEquClassesOfAllUniverses}</strong>: <code>double</code>
 * 		<p>	Information entropy of F<sub>i</sub>: <strong>H(F<sub>i</sub>)</strong>, using
 * 			labeled and unlabeled {@link Instance}s.
 * 	</li>
 * 	<li><strong>{@link #condAttributes}</strong>: <code>int</code>
 * 		<p>	Attributes used in the partitioning of U/C, i.e. <strong>C</strong>.
 * 	</li>
 * 	<li><strong>{@link #infoEntropyOfCondEquClassesOfLabeledUniverses}</strong>:
 * 				<code>double</code>
 * 		<p>	Information entropy of C: <strong>H(C)</strong>.
 * 	</li>
 * 	<li><strong>{@link #tradeOff}</strong>: <code>double</code>
 * 		<p>	Supervised/Semi-supervised/Un-supervised trade-off, marked as
 * 			<strong>&beta;</strong>.
 * 		<p>	<strong>supervise</strong> only:    &beta; = <strong>0</strong>;
 * 		<p>	<strong>un-supervise</strong> only: &beta; = <strong>1</strong>;
 * 		<p>	<strong>semi-supervise</strong>:    &beta; = <strong>(0, 1)</strong>.
 * 	</li>
 * 	<li><strong>{@link #condEntropyCalculation}</strong>: <code>condEntropyCalculation</code>>
 * 		<p>	{@link CondEntropyCal} for <strong>H(F<sub>i</sub>|F<sub>j</sub>)</strong>.
 * 	</li>
 * 	<li><strong>{@link #mutualInfoEntropyCalculation}</strong>: <code>InformationEntropyCalculation</code>
 * 		<p>	{@link MutualInfoEntropyCal} for <strong>I(F<sub>i</sub>; F<sub>j</sub>)</strong>.
 * 	</li>
 * 	<li><strong>{@link #args}</strong>: <code>Object[]</code>
 * 		<p>	Extra arguments.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Data
public class FeatureRelevance1Params4SemisupervisedRepresentative<
		CondEntropyCal extends ConditionalEntropyCalculation,
		MutualInfoEntropyCal extends MutualInformationEntropyCalculation>
{
	/**
	 * Labeled Instances
	 */
	private Collection<Instance> labeledUniverses;
	/**
	 * Unlabeled+Labeled Instances
	 */
	private Collection<Instance> allUniverses;
	/**
	 * All attributes of {@link Instance}
	 */
	private int[] attributes;

	/**
	 * F<sub>i</sub>
	 */
	private int equClassesAttribute;
	/**
	 * (labeled U)/F<sub>i</sub>
	 */
	private Collection<Collection<Instance>> equClassesOfLabeledUniverses;
	/**
	 * H(F<sub>i</sub>), bases on U//F<sub>i</sub>.
	 */
	private double infoEntropyOfEquClassesOfLabeledUniverses;
	/**
	 * H(F<sub>i</sub>), bases on (labeled U)//F<sub>i</sub>.
	 */
	private double infoEntropyOfEquClassesOfAllUniverses;

	/**
	 * C
	 */
	private IntegerIterator condAttributes;
	/**
	 * H(C), bases on (labeled U)/C.
	 */
	private double infoEntropyOfCondEquClassesOfLabeledUniverses;
	
	/**
	 * &beta;
	 */
	private double tradeOff;
	
	private CondEntropyCal condEntropyCalculation;
	private MutualInfoEntropyCal mutualInfoEntropyCalculation;
	private MutualInformationEntropyBasedSymmetricalUncertaintyCalculation symmetricalUncertaintyCalculation;

	private Object[] args;
}