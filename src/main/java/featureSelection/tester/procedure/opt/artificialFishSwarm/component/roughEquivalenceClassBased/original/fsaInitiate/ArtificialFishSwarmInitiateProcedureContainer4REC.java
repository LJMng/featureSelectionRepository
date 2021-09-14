package featureSelection.tester.procedure.opt.artificialFishSwarm.component.roughEquivalenceClassBased.original.fsaInitiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.func.ArtificialFishSwarm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.artificialFishSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.fsaInitiate.ArtificialFishSwarmInitiateProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Initialization of <strong>Artificial Fish Swarm Algorithm</strong>. Besides initializations in 
 * {@link ArtificialFishSwarmInitiateProcedureContainer}, also generate {@link EquivalenceClass}es
 * as {@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}
 * for collection items in execution.
 * <p>
 * Here are the steps in this {@link DefaultProcedureContainer}:
 * <ul>
 *  <li>
 *  	<strong>Get the Equivalent Class</strong>
 *  	<p>Compute and acquire the {@link EquivalenceClass}es..
 *  </li>
 *  <li>
 *  	<strong>FSA basic initialization</strong>
 *  	<p>Creating a {@link GenerationRecord} and calculating max fitness if needed
 *  </li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * </ul>
 * 
 * @see ArtificialFishSwarmInitiateProcedureContainer
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <FV>
 * 		Type of fitness value.
 * @param <PosiValue>
 * 		Type of position value for {@link Position}.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 */
@Slf4j
public class ArtificialFishSwarmInitiateProcedureContainer4REC<Cal extends FeatureImportance<Sig>,
															Sig extends Number, 
															FV extends FitnessValue<? extends Number>,
															PosiValue, 
															Posi extends Position<PosiValue>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public ArtificialFishSwarmInitiateProcedureContainer4REC(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new ArrayList<>(2);
	}

	@Override
	public String shortName() {
		return "initialization";
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
			// 1. Get the Equivalent Class.
			new TimeCountedProcedureComponent<Collection<EquivalenceClass>>(
					ComponentTags.TAG_COMPACT,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return RoughEquivalenceClassBasedAlgorithm
								.Basic
								.equivalenceClass(
										instances,
										new IntegerArrayIterator(attributes)
								);
					}, 
					(component, equClasses) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS, equClasses);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_COMPRESSED, true);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> universeInstances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder
								.newBuilder()
								.loadCurrentInfo(universeInstances, false)
								.setCompressedInstanceNumber(equClasses.size())
								.setExecutedRecordNumberNumber(equClasses.size(), EquivalenceClass.class)
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
				}.setDescription("Get the Equivalent Class"),
			// 2. FSA basic initialization.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Class<Cal> calculationClass =
								(Class<Cal>) parameters[p++];
						ReductionParameters params =
								(ReductionParameters) parameters[p++];
						Collection<EquivalenceClass> collectionList =
								(Collection<EquivalenceClass>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Cal calculation = calculationClass.newInstance();
						ReductionAlgorithm<Cal, Sig, EquivalenceClass> redAlg = params.getReductionAlgorithm();
						// Initiate generation record :
						GenerationRecord<Posi, Sig> generRecord =
								ArtificialFishSwarm.initGenerationRecord(
										calculation,
										collectionList,
										params, 
										attributes.length
								);
						return new Object[] {
								calculation,
								redAlg,
								generRecord
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Cal calculation = (Cal) result[r++];
						ReductionAlgorithm<Cal, Sig, EquivalenceClass> redAlg = (ReductionAlgorithm<Cal, Sig, EquivalenceClass>) result[r++];
						GenerationRecord<Posi, FV> geneRecord = (GenerationRecord<Posi, FV>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM, redAlg);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						int[] attributes = getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
						getParameters().setNonRoot("positionLength", attributes.length);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(
										report,
										(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("FSA basic initialization"),
		};
	}

	@Override
	public Object[] exec() throws Exception {
		Object result = null;
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
			result = each.exec();
		}
		return (Object[]) result;
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}