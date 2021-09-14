package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.calculation;

import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation.BandWidthAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation.GenerationBasedDynamicCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

/**
 * Implementation of {@link BandWidthAlgorithm}, based on the paper
 * <a href="https://link.springer.com/article/10.1007/s00521-015-1840-0">
 * "A novel hybrid feature selection method based on rough set and improved harmony search"</a>
 * by H.Hannah Inbarani, et al. 
 * <p> Refer to page 1867, EQ (17):
 * <pre>
 * 	bw(gn) = max(bw) * exp(c*gn);
 * 	c = ln(min(bw)/max(bw))/NI;
 * 
 * 	NI: max iteration number.
 * 	gn: current generation.
 * </pre>
 * <p>
 * For the calculating of the dynamic parameter BandWidth(BW), ranging 0~1, note that
 * Generation(NI) should be added manually before each calculation, and initiated as value 1:
 * <pre>
 * 	<code>preProcess(); // or nextGeneration();</code>
 * 	<code>Double bw = getBandwidth();</code>
 * </pre>
 * <p>Do remember to <code>reset()</code> at the begining of each new Calculation.
 * 
 * @see GenerationBasedDynamicCalculation
 * @see BandWidthAlgorithm
 * 
 * @author Benjamin_L
 */
public class DynamicBandWidthAlgorithm 
	extends GenerationBasedDynamicCalculation
	implements BandWidthAlgorithm
{
	@Getter private double minBW, maxBW;
	private Double cValue = null;
	
	public DynamicBandWidthAlgorithm(double minBW, double maxBW, int maxIterationNum) {
		super(maxIterationNum);
		this.minBW = minBW;
		this.maxBW = maxBW;
	}
	
	/**
	 * Reset by resetting generation count: 
	 * <pre>
	 * 	this.resetGeneration();
	 * </pre>
	 * 
	 * @see #resetGeneration()
	 */
	@Override
	public void reset() {
		this.resetGeneration();
	}
	
	/**
	 * Get band width calculate result:
	 * <pre>
	 * bw(gn) = max(bw) * exp(<strong>c</strong>*gn);
	 * </pre>
	 * <p>PS: <code>cValue</code>(c) will be set if it is <code>null</code> by {@link #calculateCValue()}
	 * 
	 * @return band width value in {@link Double}
	 */
	@Override
	public double getBandwidth() {
		if (cValue==null)	calculateCValue();
		return maxBW * FastMath.exp(cValue * this.getCurrentGeneration()) ;
	}
	
	/**
	 * Calculate <code>c</code>: 
	 * <pre>
	 * c = ln(min(bw)/max(bw)) / NI;
	 * </pre>
	 * 
	 * @return c in {@link double}.
	 */
	private void calculateCValue() {
		cValue = FastMath.log(minBW/maxBW) / (double) this.getMaxIteration();
	}

	/**
	 * Normalize band width: 
	 * <pre>
	 * max = bandWidth.max * exp(c * 1)
	 * min = bandWidth.max * exp(c * generation.max)
	 * result = (band width - min) / (max - min)
	 * </pre>
	 * 
	 * @param bandwidth
	 * 		current bandwidth value.
	 * @return normalized band width value.
	 */
	@Override
	public double normalizeBandwidth(double bandwidth) {
		double maxValue = maxBW * FastMath.exp(cValue * 1);
		double minValue = maxBW * FastMath.exp(cValue * getMaxIteration());
		return (bandwidth - minValue) / (maxValue - minValue);
	}
}