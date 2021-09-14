package featureSelection.tester.procedure.heuristic.classic.sequential;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.ClassicStrategy;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.SequentialSearchStrategy;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.classic.sequential.procedure.CoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.classic.sequential.procedure.ReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.classic.sequential.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Quick Reduct - Classic reduction</strong> using
 * <code>Sequential Search</code> when searching is involved.
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 6
 * {@link ProcedureComponent}s, refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Initiate</strong>:
 * 		<p>Calculate the equivalence classes induced by D: <i>U/D</i>
 * 	</li>
 * 	<li>
 * 		<strong>Calculate Global Sig</strong>:
 * 		<p>Calculate the global significance using all conditional attributes.
 * 	</li>
 * 	<li>
 * 		<strong>Get Core</strong>:
 * 		<p>Calculate Core.
 * 		<p><code>CoreProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>:
 * 		<p>Loop and search for the most significant attribute and add as an attribute
 * 			of the reduct until reaching exit condition.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Check redundancy</strong>:
 * 		<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>ReductInspectionProcedureContainer</code>
 * 	</li>
 * </ul>
 * <p>
 * The following parameters are required to be set in {@link ProcedureParameters}:
 * <ul>
 * 	<li>{@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_QR_EXEC_CORE}</li>
 * </ul>
 * 
 * @see ClassicSequentialCalculation
 * @see ClassicAttributeReductionSequentialAlgorithm
 * @see CoreProcedureContainer
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see ReductInspectionProcedureContainer
 * 
 * @param <Sig>
 * 		Type of Feature(subset) significance.
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class ClassicAttributeReductionSequentialAlgorithmHeuristicQRTester<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				ClassicStrategy,
				SequentialSearchStrategy,
				StatisticsCalculated,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public ClassicAttributeReductionSequentialAlgorithmHeuristicQRTester(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "QR-Classic(Sequential)";
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
			// 1. Initiate.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Class<? extends ClassicSequentialCalculation<Sig>> calculationClass = 
								(Class<? extends ClassicSequentialCalculation<Sig>>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ClassicSequentialCalculation<Sig> calculation = calculationClass.newInstance();
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<List<Instance>> decClasses =
								ClassicAttributeReductionSequentialAlgorithm
									.Basic
									.equivalenceClassOfDecisionAttribute(instances);
						return new Object[] {
								decClasses,
								calculation
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<List<Instance>> decClasses = (Collection<List<Instance>>) result[r++];
						ClassicSequentialCalculation<Sig> calculation = (ClassicSequentialCalculation<Sig>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("decClasses", decClasses);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> Instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder().loadCurrentInfo(Instances)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate"),
			// 2. Calculate Global Sig.
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("decClasses"),
							});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						ClassicSequentialCalculation<Sig> calculation =
								(ClassicSequentialCalculation<Sig>) 
								parameters[p++];
						Collection<List<Instance>> decClasses =
								(Collection<List<Instance>>)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.calculate(
									instances,
									new IntegerArrayIterator(attributes),
									decClasses
								).getResult();
					}, 
					(component, globalSig) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("globalSig", globalSig);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "globalPos = {}"),
									globalSig
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate Global Sig"),
			// 3. Get Core 
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}
					},
					(component, parameters) -> {
						Boolean byCore = getParameters().get(ParameterConstants.PARAMETER_QR_EXEC_CORE);
						return byCore!=null && byCore?
								(Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
											.exec():
								new HashSet<>();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get Core")
				.setSubProcedureContainer(
					"CoreProcedureContainer", 
					new CoreProcedureContainer<>(this.getParameters(), logOn)
				),
			// 4. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn){
							log.info("4. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
							component.getSubProcedureContainers().values().iterator().next()
						});
					}, 
					(component, parameters)->{
						return (Collection<Integer>) ((ProcedureContainer<?>) parameters[0]).exec();
					}, 
					(component, red) -> {
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|Reduct Candidate| = {}"),
									red.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, red);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
					}
			) {
				@Override public void init() {}
			
				@Override public String staticsName() {
					return shortName()+" | 4. of "+getComponents().size()+".";
				}
			}.setDescription("Sig loop.")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer<Sig>(getParameters(), logOn)
				),
			// 5. Check redundancy
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("5. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					},
					(component, red) -> {
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"),
									red.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT, red);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection")
				.setSubProcedureContainer(
					"ReductInspectionProcedureContainer", 
					new ReductInspectionProcedureContainer<Sig>(getParameters(), logOn)
				),
		};
	}

	@Override
	public long getTime() {
		return getComponents().stream()
					.map(comp->ProcedureUtils.Time.sumProcedureComponentTimes(comp))
					.reduce(Long::sum).orElse(0L);
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return getComponents().stream().map(ProcedureComponent::getDescription).toArray(String[]::new);
	}
}