package featureSelection.tester.procedure.heuristic.compactedDecisionTable.original;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
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
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.CompactedDesicionTableStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.algorithm.alg.compactedDecisionTable.original.CompactedDecisionTableHashAlgorithm;
import featureSelection.repository.algorithm.alg.compactedDecisionTable.original.CompactedDecisionTableOriginalHashAlgorithmUtils;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.compactedTable.InstanceBasedCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.equivalenceClass.EquivalenceClassCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import featureSelection.repository.support.calculation.alg.CompactedDecisionTableCalculation;
import featureSelection.repository.support.shrink.compactedDecisionTable.original.Shrink4CompactedDecisionTable;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.compactedDecisionTable.original.procedure.CoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.compactedDecisionTable.original.procedure.ReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.compactedDecisionTable.original.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Compacted Decision Table</strong> Feature Selection.
 * <p>
 * Original article:
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0950705115002312">
 * "Compacted decision tables based attribute reduction"</a> by Wei Wei, Junhong Wang,
 * Jiye Liang, Xin Mi, Chuangyin Dang.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 8
 * {@link ProcedureComponent}s, refer to
 * steps: 
 * <ul>
 * 	<li>
 * 		<strong>Initiate</strong>:
 * 		<p>Initiate {@link Shrink4CompactedDecisionTable} and {@link Calculation}
 * 			instances.
 * 	</li>
 * 	<li>
 * 		<strong>Compact universe and get a Compacted Table</strong>:
 * 		<p>Compact universe instances into a Compacted Decision Table.
 * 	</li>
 * 	<li>
 * 		<strong>Get global positive region and negative region</strong>:
 * 		<p>Calculate global positive region and negative region (Item {@link Collection}s)
 * 			bases on the previous Compacted Table by C.
 * 	</li>
 * 	<li>
 * 		<strong>Call sig to calculate Sig(C)</strong>:
 * 		<p>Calculate the global significance bases on the previous Compacted Table by C
 * 			and its Positive/Negative region.
 * 	</li>
 * 	<li>
 * 		<strong>Get Core</strong>:
 * 		<p>Calculate Core.
 * 		<p><code>CoreProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Calculate red.sig</strong>:
 * 		<p>Calculate the significance of the reduct(i.e. Core).
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>:
 * 		<p>Loop and search for the most significant attribute and add as an attribute of
 * 			the reduct until reaching exit condition.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>:
 * 		<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>ReductInspectionProcedureContainer</code>
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SHRINK_INSTANCE_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_QR_EXEC_CORE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * </ul>
 * 
 * @see CompactedDecisionTableCalculation
 * @see CompactedDecisionTableHashAlgorithm
 * @see CoreProcedureContainer
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see ReductInspectionProcedureContainer
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class CompactedDecisionTableHeuristicQRTester<Sig extends Number, DN extends DecisionNumber>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				HashSearchStrategy,
				StatisticsCalculated,
				CompactedDesicionTableStrategy,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public CompactedDecisionTableHeuristicQRTester(
			ProcedureParameters paramaters, boolean logOn
	) {
		super(logOn? log: null, paramaters);
		this.logOn = logOn;
		
		statistics = new Statistics();
		report = new HashMap<>();
	}
	
	@Override
	public String shortName() {
		return "QR-CT"+
				"("+ProcedureUtils.ShortName.calculation(getParameters())+")"+
				"("+ ProcedureUtils.ShortName.byCore(getParameters())+")";
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
			// 1. Initiate
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("1. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Class<? extends CompactedDecisionTableCalculation<Sig>> calculationClass =
								(Class<? extends CompactedDecisionTableCalculation<Sig>>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						return new Object[] {
							new Shrink4CompactedDecisionTable<>(),
							calculationClass.newInstance(),
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Shrink4CompactedDecisionTable<DN> shrinkInstance =
								(Shrink4CompactedDecisionTable<DN>)
								result[r++];
						CompactedDecisionTableCalculation<Sig> calculation = 
								(CompactedDecisionTableCalculation<Sig>) 
								result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE, shrinkInstance);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
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
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate"),
			// 2. Compact universe and get a Compacted Table
			new TimeCountedProcedureComponent<Collection<InstanceBasedCompactedTableRecord<DN>>> (
					ComponentTags.TAG_COMPACT,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("2. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_DECISION_NUMBER_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Class<DN> decisionNumberClass =
								(Class<DN>) parameters[p++];
						List<Instance> instances =
								(List<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return CompactedDecisionTableHashAlgorithm
									.Basic
									.instance2CompactedTable(
											decisionNumberClass,
											instances,
											attributes
									);
					}, 
					(component, compactedTable) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("compactedTable", compactedTable);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> Instances =
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder()
								.loadCurrentInfo(Instances, false)
								.setCompressedInstanceNumber(compactedTable.size())
								.setExecutedRecordNumberNumber(compactedTable.size(), InstanceBasedCompactedTableRecord.class)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								compactedTable.size(), 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Compact universe and get a Compacted Table"),
			// 3. Get global positive region and negative region
			new TimeCountedProcedureComponent<Set<InstanceBasedCompactedTableRecord<DN>>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("compactedTable"),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<InstanceBasedCompactedTableRecord<DN>> compactedTable =
								(Collection<InstanceBasedCompactedTableRecord<DN>>) parameters[0];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return CompactedDecisionTableHashAlgorithm
								.Basic
								.globalPositiveNNegativeRegion(compactedTable);
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("positiveNNegative", result);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<?> compactedTable =
								getParameters().get("compactedTable");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								compactedTable.size(), 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
			}.setDescription("Get global positive region and negative region"),
			// 4. Call sig to calculate Sig(C)
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("4. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("compactedTable"),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						CompactedDecisionTableCalculation<Sig> calculation = 
								(CompactedDecisionTableCalculation<Sig>) 
								parameters[p++];
						Collection<InstanceBasedCompactedTableRecord<DN>> compactedTable =
								(Collection<InstanceBasedCompactedTableRecord<DN>>)
								parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return (Sig) calculation.calculate(compactedTable, attributes.length, instances.size())
												.getResult();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("globalSig", result);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "globalSig = {}"),
									result
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<?> compactedTable =
								getParameters().get("compactedTable");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								compactedTable.size(), 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Call sig to calculate Sig(C)"),
			// 5. Get Core
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_QR_EXEC_CORE),
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						if (parameters[0]!=null && (Boolean) parameters[0]) {
							return (Collection<Integer>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
						}else {
							return new HashSet<>();
						}
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<?> compactedTable = getParameters().get("compactedTable");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								compactedTable.size(), 
								result.size()
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
				}.setDescription("Get Core")
				.setSubProcedureContainer(
					"CoreProcedureContainer", 
					new CoreProcedureContainer<>(getParameters(), logOn)
				),
			// 6. Calculate red.sig.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("6. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("compactedTable"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						Collection<InstanceBasedCompactedTableRecord<DN>> compactedTable =
								(Collection<InstanceBasedCompactedTableRecord<DN>>)
								parameters[p++];
						CompactedDecisionTableCalculation<Sig> calculation = 
								(CompactedDecisionTableCalculation<Sig>) 
								parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// newU = equivalenceClassCompactedTable(U’, gPOS, gNEG, Red)
						Collection<EquivalenceClassCompactedTableRecord<DN>> equClasses =
							CompactedDecisionTableHashAlgorithm
								.Basic
								.equivalenceClassOfCompactedTable(
										compactedTable, 
										red.isEmpty()? null: new IntegerCollectionIterator(red)
								);
						// * Calculate red.sig.
						return new Object[] {
							(Sig) calculation.calculate(equClasses, red.size(), instances.size())
											.getResult(),
							equClasses
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Sig redSig = (Sig) result[r++];
						Collection<EquivalenceClassCompactedTableRecord<DN>> equClasses =
								(Collection<EquivalenceClassCompactedTableRecord<DN>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("redSig", redSig);
						getParameters().setNonRoot("equClasses", equClasses);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "sig(red) = {}"),
									result[0]
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_POS_HISTORY]
						List<Sig> posIncrement = (List<Sig>) statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (posIncrement==null)	statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY, posIncrement = new LinkedList<>());
						posIncrement.add(redSig);
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<?> compactedTable = getParameters().get("compactedTable");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								compactedTable.size(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate red.sig"),
			// 7. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("7. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						return (Collection<Integer>) component.getSubProcedureContainers().values().iterator().next().exec();
					}, 
					(component, red) -> {
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "|Reduct Candidate| = {}"), red.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								red
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<EquivalenceClassCompactedTableRecord<DN>> equClasses = getParameters().get("equClasses");
						int currentEquClassSize =
								CompactedDecisionTableOriginalHashAlgorithmUtils
										.compactedRecordSizeOfEquivalenceClassRecords(
												equClasses
										);
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								currentEquClassSize, 
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
						return shortName()+" | 7. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sig loop")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer<Sig, DN>(getParameters(), logOn)
				),
			// 8. Inspection.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						if (logOn) {
							log.info("8. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
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
						return shortName()+" | 8. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection")
				.setSubProcedureContainer(
					"ReductInspectionProcedureContainer",
					new ReductInspectionProcedureContainer<Sig, DN>(getParameters(), logOn)
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