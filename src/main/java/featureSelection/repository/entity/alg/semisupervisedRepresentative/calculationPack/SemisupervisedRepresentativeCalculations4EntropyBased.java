package featureSelection.repository.entity.alg.semisupervisedRepresentative.calculationPack;

import featureSelection.basic.support.alg.SemisupervisedRepresentativeStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.conditionalEntropy.semisupervisedRepresentative.ConditionalEntropyCalculation4SemisupervisedRepresentative;
import featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.informationEntropy.semisupervisedRepresentative.InformationEntropyCalculation4SemisupervisedRepresentative;
import featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.mutualInformationEntropy.semisupervisedRepresentative.MutualInformationEntropyCalculation4SemisupervisedRepresentative;
import featureSelection.repository.support.calculation.markovBlanket.approximate.symmetricalUncertainty.mutualInformationEntropy.MutualInformationEntropyBasedSymmetricalUncertaintyCalculation;
import featureSelection.repository.support.calculation.relevance.semisupervisedRepresentative.FeatureRelevance4SemisupervisedRepresentative4MutualInfoEntropyBased;
import lombok.Data;

/**
 * A calculation for <strong>Semi-supervised Representative Feature Selection</strong>.
 * <p>
 * <strong>Notice</strong>:
 * Calculations including <i>entropy value calculations</i> and <i>feature relevance calculations</i>.
 * This class doesn't implements calculations, but contains implemented {@link Calculation} instances:
 * <ul>
 *     <li>{@link FeatureRelevance4SemisupervisedRepresentative4MutualInfoEntropyBased}
 *          <p>
 *          For calculating feature relevance.
 *     </li>
 *     <li>{@link InformationEntropyCalculation4SemisupervisedRepresentative}
 *          <p>
 *          For calculating info. entropy.
 *     </li>
 *     <li>{@link ConditionalEntropyCalculation4SemisupervisedRepresentative}
 *          <p>
 *          For calculating conditional info. entropy.
 *     </li>
 *     <li>{@link MutualInformationEntropyCalculation4SemisupervisedRepresentative}
 *          <p>
 *          For calculating mutual info. entropy.
 *     </li>
 *     <li>{@link MutualInformationEntropyBasedSymmetricalUncertaintyCalculation}
 *          <p>
 *          For calculating mutual info. entropy based Symmetrical Uncertainty (SU).
 *     </li>
 * </ul>
 */
@Data
public class SemisupervisedRepresentativeCalculations4EntropyBased 
	implements SemisupervisedRepresentativeStrategy,
				Calculation<Object>
{
	public static final String CALCULATION_NAME = "SRFS-Entropy+Relevance";
	
	private FeatureRelevance4SemisupervisedRepresentative4MutualInfoEntropyBased relevanceCalculation;
	private InformationEntropyCalculation4SemisupervisedRepresentative infoEntropyCalculation;
	private ConditionalEntropyCalculation4SemisupervisedRepresentative condEntropyCalculation;
	private MutualInformationEntropyCalculation4SemisupervisedRepresentative mutualInfoEntropyCalculation;
	private MutualInformationEntropyBasedSymmetricalUncertaintyCalculation symmetricalUncertaintyCalculation;

	public SemisupervisedRepresentativeCalculations4EntropyBased() {
		relevanceCalculation = new FeatureRelevance4SemisupervisedRepresentative4MutualInfoEntropyBased();
		infoEntropyCalculation = new InformationEntropyCalculation4SemisupervisedRepresentative();
		condEntropyCalculation = new ConditionalEntropyCalculation4SemisupervisedRepresentative();
		mutualInfoEntropyCalculation = new MutualInformationEntropyCalculation4SemisupervisedRepresentative();
		symmetricalUncertaintyCalculation = new MutualInformationEntropyBasedSymmetricalUncertaintyCalculation();
	}
	
	public long sumCalculationTimes() {
		return relevanceCalculation.getCalculationTimes() + 
				infoEntropyCalculation.getCalculationTimes() +
				condEntropyCalculation.getCalculationTimes() +
				mutualInfoEntropyCalculation.getCalculationTimes() +
				symmetricalUncertaintyCalculation.getCalculationTimes();
	}
	
	public long sumCalculationAttributeLength() {
		return relevanceCalculation.getCalculationAttributeLength() + 
				infoEntropyCalculation.getCalculationAttributeLength() +
				condEntropyCalculation.getCalculationAttributeLength() +
				mutualInfoEntropyCalculation.getCalculationAttributeLength();
	}

	@Override
	public Object getResult() {
		throw new RuntimeException("Unimplemented method!");
	}

	/**
	 * Get the sum of calculation times.
	 * 
	 * @see #sumCalculationTimes()
	 */
	@Override
	public long getCalculationTimes() {
		return sumCalculationTimes();
	}
	
	/**
	 * Get the sum of calculation attribute length.
	 * 
	 * @see #sumCalculationAttributeLength()
	 */
	public long getCalculationAttributeLength() {
		return sumCalculationAttributeLength();
	}
}