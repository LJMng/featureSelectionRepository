package featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation;

public interface PitchAdjustmentRateAlgorithm {

	Number getMinPitchAdjustmentRate();
	Number getMaxPitchAdjustmentRate();
	
	void reset();
	void preProcess();
	void afterProcess();
	
	/**
	 * Get the value of PitchAdjustmentRate(PAR)
	 * 
	 * @return PAR.
	 */
	double getPitchAdjustmentRate();

}