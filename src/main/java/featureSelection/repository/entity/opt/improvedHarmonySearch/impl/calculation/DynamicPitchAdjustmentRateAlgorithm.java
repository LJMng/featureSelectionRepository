package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.calculation;

import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation.GenerationBasedDynamicCalculation;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation.PitchAdjustmentRateAlgorithm;

/**
 * Implementation of {@link PitchAdjustmentRateAlgorithm}, based on article 
 * <a href="https://link.springer.com/article/10.1007/s00521-015-1840-0">
 * "A novel hybrid feature selection method based on rough set and improved harmony search"</a>
 * by H.Hannah Inbarani, et al. 
 * <p> Refer to page 1867, EQ (17):
 * <pre>
 * PAR = minPAR + (maxPAR-minPAR)/NI * gn
 * 
 * NI: max iteration number.
 * gn: current generation.
 * </pre>
 * For the calculating of the dynamic parameter PitchAdjustmentRate(PAR), ranging 0~1, note that 
 * Generation(NI) should be added manually before each calculation, and initiated as value 1:
 * <pre>
 * preProcess(); // or nextGeneration();
 * Double par = getPitchAdjustmentRate();
 * </pre>
 * Do remember to <code>reset()</code> at the beginning of each new Calculation.
 * 
 * @author Benjamin_L
 * 
 * @see GenerationBasedDynamicCalculation
 * @see PitchAdjustmentRateAlgorithm
 */
public class DynamicPitchAdjustmentRateAlgorithm 
	extends GenerationBasedDynamicCalculation
	implements PitchAdjustmentRateAlgorithm
{
	private double minPAR, maxPAR;
	
	public DynamicPitchAdjustmentRateAlgorithm(double minPAR, double maxPAR, int maxIterationNum) {
		super(maxIterationNum);
		this.minPAR = minPAR;
		this.maxPAR = maxPAR;
	}
	
	/**
	 * Reset by resetting generation count: 
	 * <pre>
	 * 	this.resetGeneration();
	 * </pre>
	 * 
	 * @see {@link #resetGeneration()}
	 */
	@Override
	public void reset() {
		this.resetGeneration();
	}
	
	/**
	 * Get the pitch adjustment rate: 
	 * <pre>
	 * value = par.min + (par.max-par.min) / iteration.max * generation.current;
	 * </pre>
	 * 
	 * @return Pitch adjustment rate in {@link double}.
	 */
	@Override
	public double getPitchAdjustmentRate() {
		return minPAR + (maxPAR-minPAR) / getMaxIteration() * getCurrentGeneration();
	}

	@Override
	public Number getMinPitchAdjustmentRate() {
		return minPAR;
	}

	@Override
	public Number getMaxPitchAdjustmentRate() {
		return maxPAR;
	}

}
