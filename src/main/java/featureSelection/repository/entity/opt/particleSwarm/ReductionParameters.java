package featureSelection.repository.entity.opt.particleSwarm;

import java.util.Arrays;

import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.model.optimization.OptimizationParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.inertiaWeight.InertiaWeightAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.inertiaWeight.InertiaWeightAlgorithmParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.code.ParticleInitialization;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.code.ParticleInitializationParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.code.ParticleUpdateAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import lombok.Data;

/**
 * {@link OptimizationParameters} for <strong>Particle Swarm Optimization</strong>.
 * <p>
 * <code>r1</code>, <code>r2</code> = -1 if <code>auto-setting</code> (using random value) in
 * particle updating.
 * <p>
 * <code>maxDistinctBestFitness</code> = <strong>null</strong> if <strong>NOT</strong> limiting
 * the number of max. distinct best fitness.
 * 
 * @param <Velocity>
 * 		Type of velocity of the {@link Particle}.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 * 
 * @author Benjamin_L
 */
@Data
public class ReductionParameters<Velocity, Posi extends Position<?>, FValue extends FitnessValue<?>>
	implements OptimizationParameters
{
	/**
	 * Population of the particles.
	 * <p>
	 * 种群大小
	 */
	private int population;
	/**
	 * 粒子编码可取值
	 */
	private int[] attributes;
	/**
	 * 加速度常数/学习步长
	 */
	private double c1, c2;
	/**
	 * 速度范围
	 */
	private Velocity velocityMin, velocityMax;
	/**
	 * fitness最大值
	 */
	private FValue maxFitness;
	/**
	 * 连续收敛次数
	 */
	private int convergence;
	/**
	 * 一共进化多少次
	 */
	private int iteration;
	/**
	 * 个体系数
	 */
	private double r1;
	/**
	 * 社会系数
	 */
	private double r2;
	
	/**
	 * 最大不重复解个数(含global best)
	 */
	private Integer maxDistinctBestFitness;
	/**
	 * If get multiple solutions, return one only.
	 */
	private Boolean singleSolutionOnly;
	/**
	 * 在Greedy search的过程中对reduct检验冗余
	 * <p>true by default.
	 */
	private boolean inspectReductInGreedySearch = true;

	@SuppressWarnings("rawtypes")
	private ParticleInitialization particleInitAlgorithm;
	private ParticleInitializationParameters particleInitAlgorithmParameters;
	
	private ParticleUpdateAlgorithm<Velocity, Posi, FValue> particleUpdateAlgorithm;
	
	@SuppressWarnings("rawtypes")
	private InertiaWeightAlgorithm inertiaWeightAlgorithm;
	private InertiaWeightAlgorithmParameters<?> inertiaWeightAlgorithmParameters;
	
	@SuppressWarnings("rawtypes") private ReductionAlgorithm reductionAlgorithm;	// 计算pos的方法
	
	@Override
	public OptimizationAlgorithm getOptimizationAlgorithm() {
		return getReductionAlgorithm();
	}
	
	public String info() {
		return "ReductionParameters ["+"\r\n" +
				"population=" + population + "\r\n" +
				"attributes=" + Arrays.toString(attributes) + "\r\n" +
				"c1=" + c1 + ", c2=" + c2 + "\r\n" +
				"volecityMin=" + velocityMin + ", volecityMax=" + velocityMax +  "\r\n" + 
				"maxFitness=" + maxFitness +  "\r\n" +
				"convergence=" + convergence +  "\r\n" +
				"times=" + iteration +  "\r\n" +
				"r1=" + r1 + ", r2=" + r2 + "\r\n" +  
				"particleInitAlgorithm=" + particleInitAlgorithm + "\r\n" +
				"particleUpdateAlgorithm=" + particleUpdateAlgorithm + "\r\n" +
				"inertiaWeightAlgorithm=" + inertiaWeightAlgorithm+"\r\n" +
				"reductionAlgorithm=" + reductionAlgorithm+"\r\n"+
				"]";
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReductionParameters<Velocity, Posi, FValue> clone() throws CloneNotSupportedException {
		return (ReductionParameters<Velocity, Posi, FValue>) super.clone();
	}
}