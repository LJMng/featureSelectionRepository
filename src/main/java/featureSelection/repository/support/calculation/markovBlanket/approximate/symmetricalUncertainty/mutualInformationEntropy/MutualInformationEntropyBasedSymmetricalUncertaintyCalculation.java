package featureSelection.repository.support.calculation.markovBlanket.approximate.symmetricalUncertainty.mutualInformationEntropy;

import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.InformationEntropyCalculation;
import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.MutualInformationEntropyCalculation;
import featureSelection.basic.support.calculation.markovBlanket.approximate.SymmetricalUncertaintyCalculation;
import lombok.Getter;

/**
 * Symmetrical Uncertainty (SU) based on Mutual Information Entropy calculation.
 * <p>
 * <strong>SU(F<sub>i</sub>, F<sub>j</sub>)</strong> = 2 * [ I(F<sub>i</sub>; F<sub>j</sub>) / 
 * (H(F<sub>i</sub>) + H(F<sub>j</sub>))]
 * 
 * @see SymmetricalUncertaintyCalculation
 * 
 * @author Benjamin_L
 */
public class MutualInformationEntropyBasedSymmetricalUncertaintyCalculation
	implements SymmetricalUncertaintyCalculation<Double>
{
	@Getter protected long calculationTimes = 0;
	@Getter private Double result;

	/**
	 * Calculate the <i>Symmetrical Uncertainty</i> of 2 attributes bases on Mutual Information
	 * Entropy value and Information Entropy values.
	 * 
	 * @see InformationEntropyCalculation
	 * @see MutualInformationEntropyCalculation
	 * 
	 * @param mutualInfoEntropy
	 * 		The mutual information entropy value of the 2 attributes:
	 * 		<strong><i>I(F<sub>i</sub>; F<sub>j</sub>)</i></strong>.
	 * @param infoEntropyOfAttribute1
	 * 		The information entropy value of a attribute: <strong><i>H(F<sub>i</sub>)</i></strong>.
	 * @param infoEntropyOfAttribute2
	 * 		The information entropy value of another attribute:
	 * 		<strong><i>H(F<sub>j</sub>)</i></strong>.
	 * @return <code>this</code> instance.
	 */
	public MutualInformationEntropyBasedSymmetricalUncertaintyCalculation calculate(
			double mutualInfoEntropy, double infoEntropyOfAttribute1,
			double infoEntropyOfAttribute2
	) {
		// Count calculation
		calculationTimes++;
		// Calculate.
		result = 2 * ( mutualInfoEntropy / (infoEntropyOfAttribute1+infoEntropyOfAttribute2) );
		return this;
	}
}