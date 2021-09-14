package featureSelection.tester.procedure.opt.particalSwarm.procedure.common.psoInitiate;

import java.util.Collection;

import common.utils.LoggerUtil;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.original.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Initialization of <strong>Particle Swarm Optimization</strong>. Mainly, this contains 
 * {@link ProcedureComponent}s : 
 * <ul>
 * <li>
 * 	<strong>PSO basic initiation</strong>
 * 	<p>Create the instance of {@link FeatureImportance}, a {@link GenerationRecord} and calculate 
 * 		max fitness if needed
 * </li>
 * </ul>
 * <p>
 * Different from {@link ParticleSwarmOptimizationInitiateProcedureContainer}, this one initiate 
 * using the created the instance of {@link FeatureImportance} with previous reduct and other info 
 * to support calculations for data arrived.
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * </ul>
 * <p>
 * {@link DefaultAsitKDasFitnessCalculation} instance should be set into {@link ProcedureParameters} 
 * by user with value associated key {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}.
 * 
 * @see DefaultAsitKDasFitnessCalculation
 * @see ParticleSwarmOptimizationInitiateProcedureContainer
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Velocity>
 * 		Type of {@link Particle}'s Velocity.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class ParticleSwarmOptimizationInitiate4AsitKDasAlgsProcedureContainer<Cal extends DefaultAsitKDasFitnessCalculation<? extends FeatureImportance<Double>, Double>,
																				CollectionItem,																				
																				Velocity, 
																				Posi extends Position<?>,
																				FValue extends FitnessValue<?>>
	extends ParticleSwarmOptimizationInitiateProcedureContainer4REC<Cal, Double, FValue>
{
	public ParticleSwarmOptimizationInitiate4AsitKDasAlgsProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(parameters, logOn);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. PSO basic initiation.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						Collection<Instance> instances =
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						getParameters().setNonRoot(
								ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS, 
								instances
							);
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								instances,
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation =
								(Cal) parameters[p++];
						ReductionParameters<?, ?, FValue> params =
								(ReductionParameters<?, ?, FValue>) parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Double> redAlg =
								params.getReductionAlgorithm();
						if (params.getMaxFitness()==null) {
							params.setMaxFitness(
								redAlg.fitnessValue(calculation, collectionList, params.getAttributes())
							);
						}
						return new Object[] {
								redAlg,
								new GenerationRecord<>(),
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Double> redAlg = 
								(ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Double>) 
								result[r++];
						GenerationRecord<?, ?, FValue> geneRecord = (GenerationRecord<?, ?, FValue>) result[r++];
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							ReductionParameters<?, ?, FValue> params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
							log.info(LoggerUtil.spaceFormat(2, "Max fitness = {}"), 
									params.getMaxFitness().getValue() instanceof Double?
									String.format("%.4f", params.getMaxFitness().getValue()):
									params.getMaxFitness().getValue()
							);
						}
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM, redAlg);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder().loadCurrentInfo(instances)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("PSO basic initiation"),
		};
	}
}