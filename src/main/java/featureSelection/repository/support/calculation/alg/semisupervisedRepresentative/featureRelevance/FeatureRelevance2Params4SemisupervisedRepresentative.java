package featureSelection.repository.support.calculation.alg.semisupervisedRepresentative.featureRelevance;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.ConditionalEntropyCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.InformationEntropyCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.MutualInformationEntropyCalculation;
import featureSelection.repository.support.calculation.markovBlanket.approximate.symmetricalUncertainty.mutualInformationEntropy.MutualInformationEntropyBasedSymmetricalUncertaintyCalculation;
import lombok.Data;

/**
 * An entity for <strong>Semi-supervised Representative</strong> Feature Relevance 1
 * calculation parameters.
 * (F_Rel2) With the following fields/parameters:
 * <ul>
 * 	<li><strong>{@link #labeledUniverses}</strong>: {@link Collection}
 * 		<p>	Labeled {@link Instance} {@link Collection}. (i.e. <strong>labeled U</strong>)
 * 	</li>
 * 	<li><strong>{@link #allUniverses}</strong>: {@link Collection}
 * 		<p>	Labeled+Unlabeled {@link Instance} {@link Collection}. (i.e. <strong>U</strong>)
 * 	</li>
 * 	<li><strong>attributes</strong>: <code>int[]</code>
 * 		<p>	All attributes of {@link Instance}.
 *	 </li>
 *	 <li><strong>equClassesAttribute</strong>: <code>int</code>
 * 		<p>	Attributes used in Equivalent Classes partitioning: <strong>F<sub>i</sub></strong>.
 * 			For calculations of UI(F<sub>i</sub>), H(F<sub>i</sub> | F<sub>j</sub>) where j in
 * 			1:n and j!=i and n = |attributes|.
 * 	</li>
 * 	<li><strong>equClassesOfLabeledUniverses</strong>: {@link Collection}
 * 		<p>	Equivalent Classes partitioned by F<sub>i</sub>: <strong>(labeled U)/F<sub>i</sub></strong> using
 * 			labeled Universes only. For calculations of H(C|F<sub>i</sub>).
 * 	</li>
 * 	<li><strong>infoEntropyOfConEquClassesOfAllUniverses</strong>: <code>double</code>>
 * 		<p>	Information entropy of F<sub>i</sub>: <strong>H(F<sub>i</sub>)</strong>, using
 * 			labeled and unlabeled Universes.
 * 	</li>
 * 	<li><strong>infoEntropyOfEquClassesOfLabeledUniverses</strong>: <code>double</code>>
 * 		<p>	Information entropy of F<sub>i</sub>: <strong>H(F<sub>i</sub>)</strong>, using
 * 			labeled Universes only.
 * 	</li>
 * 	<li><strong>condAttributes</strong>: <code>int</code>
 * 		<p>	Attributes used in the partitioning of U/C, i.e. <strong>C</strong>.
 * 	</li>
 * 	<li><strong>infoEntropyOfConEquClassesOfAllUniverses</strong>: <code>double</code>>
 * 		<p>	Information entropy of C: <strong>H(C)</strong>, using labeled and unlabeled
 * 			instances.
 * 	</li>
 * 	<li><strong>infoEntropyOfConEquClassesOfLabeledUniverses</strong>: <code>double</code>>
 * 		<p>	Information entropy of C: <strong>H(C)</strong>, using labeled Universes only.
 * 	</li>
 * 	<li><strong>tradeOff</strong>: <code>double</code>>
 * 		<p>	Superivsed/Semi-supervised/Un-supervised trade-off, marked as <strong>&beta;</strong>.
 * 		<p>	<strong>supervise</strong> only:    &beta; = <strong>0</strong>;
 * 		<p>	<strong>un-supervise</strong> only: &beta; = <strong>1</strong>;
 * 		<p>	<strong>semi-supervise</strong>:    &beta; = <strong>(0, 1)</strong>.
 * 	</li>
 * 	<li><strong>condEntropyCalculation</strong>: <code>condEntropyCalculation</code>
 * 		<p>	{@link CondEntropyCal} for <strong>H(F<sub>i</sub>|F<sub>j</sub>)</strong>.
 * 	</li>
 * 	<li><strong>mutualInfoEntropyCalculation</strong>: {@link InformationEntropyCalculation}
 * 		<p>	{@link MutualInfoEntropyCal} for <strong>I(F<sub>i</sub>; F<sub>j</sub>)</strong>.
 * 	</li>
 * 	<li><strong>args</strong>: <code>Object[]</code>
 * 		<p>	Extra arguments.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Data
public class FeatureRelevance2Params4SemisupervisedRepresentative<
		CondEntropyCal extends ConditionalEntropyCalculation,
		MutualInfoEntropyCal extends MutualInformationEntropyCalculation>
{
	/**
	 * Labeled U
	 */
	private Collection<Instance> labeledUniverses;
	/**
	 * U
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
	 * H(F<sub>i</sub>), bases on U/F<sub>i</sub>
	 */
	private double infoEntropyOfEquClassesOfAllUniverses;
	/**
	 * H(F<sub>i</sub>), bases on (Labeled U)/F<sub>i</sub>
	 */
	private double infoEntropyOfEquClassesOfLabeledUniverses;

	/**
	 * F<sub>j</sub>
	 */
	private IntegerIterator condAttributes;
	/**
	 * H(F<sub>j</sub>), bases on (U)/F<sub>j</sub>
	 */
	private double infoEntropyOfConEquClassesOfAllUniverses;
	/**
	 * H(F<sub>j</sub>), bases on (labeled U)/F<sub>j</sub>
	 */
	private double infoEntropyOfConEquClassesOfLabeledUniverses;
	
	/**
	 * &beta;
	 */
	private double tradeOff;
	
	private CondEntropyCal condEntropyCalculation;
	private MutualInfoEntropyCal mutualInfoEntropyCalculation;
	private MutualInformationEntropyBasedSymmetricalUncertaintyCalculation symmetricalUncertaintyCalculation;

	private Object[] args;
}