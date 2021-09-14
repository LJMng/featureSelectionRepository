package featureSelection.repository.entity.opt.improvedHarmonySearch;

import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.model.optimization.OptimizationParameters;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation.BandWidthAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation.PitchAdjustmentRateAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.code.HarmonyInitialization;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import lombok.Data;

@Data
public class ReductionParameters<Sig extends Number, Hrmny extends Harmony<?>, FValue extends FitnessValue<Sig>>
	implements OptimizationParameters
{
	/**
	 * Group size of harmony
	 */
	private int groupSize;
	/**
	 * Maximum fitness
	 */
	private FValue maxFitness;
	
	/**
	 * Memory size of harmony
	 * <p>
	 * HMS: 和声记忆大小=属性C个数
	 */
	private int harmonyMemorySize;
	/**
	 * Available attributes of {@link Instance}
	 */
	private int[] attributes;
	/**
	 * Rate of repeatly using harmony memory.
	 * <p>
	 * HMCR: 和声记忆复用率
	 */
	private double harmonyMemoryConsiderationRate;
	/**
	 * Maximum generation.
	 * <p>
	 * NI: 最大迭代次数
	 */
	private int iteration;
	/**
	 * Maximum convergence times.
	 * <p>
	 * conTimes: 连续最优解相同次数
	 */
	private int convergence;
	/**
	 * The max number of distinct fitnesses/feature subset. <code>null</code> as no limitation.
	 */
	private Integer maxDistinctBestFitness;
	/**
	 * If get multiple solutions, return one only.
	 */
	private Boolean singleSolutionOnly;
	/**
	 * Pitch adjustment rate algorithm
	 * <p>
	 * PAR: X调整率参数
	 */
	private PitchAdjustmentRateAlgorithm parAlg;
	/**
	 * Band width algorithm
	 * <p>
	 * BW: X调整参数
	 */
	private BandWidthAlgorithm bwAlg;
	
	/**
	 * fitnessCount: 计算fitness的方法, pos计算方法
	 */
	@SuppressWarnings("rawtypes") private ReductionAlgorithm redAlg;
	
	/**
	 * Initiation of harmonies.
	 * <p>
	 * Harmony初始化算法
	 */
	private HarmonyInitialization<Hrmny> harmonyInitializationAlg;
//	/**
//	 * Class of harmony fitness.
//	 */
//	private Class<? extends Fitness<Sig, FValue>> fitnessClass;

	@Override
	public OptimizationAlgorithm getOptimizationAlgorithm() {
		return redAlg;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public ReductionParameters<Sig, Hrmny, FValue> clone() throws CloneNotSupportedException {
		return (ReductionParameters<Sig, Hrmny, FValue>) super.clone();
	}
}