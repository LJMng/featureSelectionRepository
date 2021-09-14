package featureSelection.tester.procedure.opt.genetic.procedure.common.gaInitiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.ArrayUtils;
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
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.gaInitiate.GeneticAlgorithmInitiateProcedureContainer4IPNECStreaming;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.original.gaInitiate.GeneticAlgorithmInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Initialization of <strong>Genetic Algorithm</strong>.
 * Procedure contains 1 {@link ProcedureComponent}, referring to 1 step:
 * <ul>
 *  <li>
 *  	<strong>GA basic initialization</strong>
 *  	<p>Creating a {@link GenerationRecord} and calculating max fitness if needed.
 *  </li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * </ul>
 * 
 * @see GeneticAlgorithmInitiateProcedureContainer4REC
 * @see GeneticAlgorithmInitiateProcedureContainer4IPNECStreaming
 * @see GeneticAlgorithmInitiate4AsitKDasAlgsProcedureContainer
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Comparable} extended fitness value.
 */
@Slf4j
public class GeneticAlgorithmInitiateProcedureContainer<Cal extends FeatureImportance<Sig>,
														Sig extends Number, 
														CollectionItem,
														Chr extends Chromosome<?>,
														FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	protected boolean logOn;
	@Getter protected Statistics statistics;
	@Getter protected Map<String, Map<String, Object>> report;
	protected Collection<String> reportKeys;

	public GeneticAlgorithmInitiateProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new ArrayList<>(1);
	}

	@Override
	public String shortName() {
		return "GA Initialization";
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
			// 1. GA basic initialization.
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
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								instances
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						Class<Cal> calculationClass =
								(Class<Cal>) parameters[p++];
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> gaParams = 
								(ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue>) 
								parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Cal calculation = calculationClass.newInstance();
						ReductionAlgorithm<Cal, Sig, CollectionItem, Chr, FValue> redAlg =
								gaParams.getReductionAlgorithm();

						// Initiate max fitness for ReductionParameters.
						if (gaParams.getMaxFitness()==null || gaParams.getMaxFitness().getValue()==null) {
							int[] attributes = ArrayUtils.initIncrementalValueIntArray(gaParams.getChromosomeLength(), 1, 1);
							gaParams.setMaxFitness(
								redAlg.calculateFitness(calculation, collectionList, attributes)
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
						int r = 0;
						Cal calculation = (Cal) result[r++];
						ReductionAlgorithm<Cal, Sig, CollectionItem, Chr, FValue> redAlg = 
								(ReductionAlgorithm<Cal, Sig, CollectionItem, Chr, FValue>) 
								result[r++];
						GenerationRecord<Chr, FValue> geneRecord = (GenerationRecord<Chr, FValue>) result[r++];
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							ReductionParameters<?, ?, ?, ?, ?> gaParams =
									getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
							log.info(LoggerUtil.spaceFormat(2, "Max fitness = {}"), 
									gaParams.getMaxFitness().getValue() instanceof Double?
									String.format("%.4f", gaParams.getMaxFitness().getValue()):
									gaParams.getMaxFitness().getValue()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// set PARAMETER_SIG_CALCULATION_INSTANCE
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						// set PARAMETER_OPTIMIZATION_ALGORITHM
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM, redAlg);
						// set PARAMETER_OPTIMIZATION_GENERATION_RECORD
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> universeInstances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder().loadCurrentInfo(universeInstances)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("GA basic initialization"),
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