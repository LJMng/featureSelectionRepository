package featureSelection.tester.procedure.opt.particalSwarm.procedure.common.psoInitiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer4NECStreaming;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.original.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
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
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * </ul>
 * 
 * @see ParticleSwarmOptimizationInitiate4AsitKDasAlgsProcedureContainer
 * @see ParticleSwarmOptimizationInitiateProcedureContainer4REC
 * @see ParticleSwarmOptimizationInitiateProcedureContainer4NECStreaming
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class ParticleSwarmOptimizationInitiateProcedureContainer<Cal extends FeatureImportance<Sig>,
																Sig extends Number, 
																CollectionItem, 
																FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public ParticleSwarmOptimizationInitiateProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new ArrayList<>(1);
	}

	@Override
	public String shortName() {
		return "Initiation";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
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
						Collection<Instance> instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						getParameters().setNonRoot(
								ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS,
								instances
							);
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								instances,
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Class<Cal> calculationClass = (Class<Cal>) parameters[p++];
						ReductionParameters<?, ?, FValue> params = (ReductionParameters<?, ?, FValue>) parameters[p++];
						Collection<CollectionItem> collectionItem = (Collection<CollectionItem>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Cal calculation = calculationClass.newInstance();
						ReductionAlgorithm<CollectionItem, ?, ?, FValue, Cal, Sig> redAlg = params.getReductionAlgorithm();
						// Initiate max fitness for ReductionAlgorithm.
						if (params.getMaxFitness()==null) {
							params.setMaxFitness(
								redAlg.fitnessValue(calculation, collectionItem, params.getAttributes())
							);
						}
						return new Object[] {
								calculation,
								redAlg,
								new GenerationRecord<>(),
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Cal calculation = (Cal) result[r++];
						ReductionAlgorithm<CollectionItem, ?, ?, FValue, Cal, Sig> redAlg =
								(ReductionAlgorithm<CollectionItem, ?, ?, FValue, Cal, Sig>) 
								result[r++];
						GenerationRecord<?, ?, FValue> geneRecord = (GenerationRecord<?, ?, FValue>) result[r++];
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							ReductionParameters<?, ?, FValue> params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
							log.info(LoggerUtil.spaceFormat(2, "Max fitness = {}"), 
									params.getMaxFitness().getValue() instanceof Integer?
									String.format("%d", params.getMaxFitness().getValue().intValue()):
									String.format("%.4f", params.getMaxFitness().getValue().doubleValue())
							);
						}
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
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

	@Override
	public Object[] exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Object[]) componentArray[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}