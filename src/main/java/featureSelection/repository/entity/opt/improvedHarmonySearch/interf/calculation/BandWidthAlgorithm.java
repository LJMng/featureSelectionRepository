package featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation;

public interface BandWidthAlgorithm {
	double getBandwidth();
	double normalizeBandwidth(double bandwidth);
	
	void reset();
	void preProcess();
	void afterProcess();
}
