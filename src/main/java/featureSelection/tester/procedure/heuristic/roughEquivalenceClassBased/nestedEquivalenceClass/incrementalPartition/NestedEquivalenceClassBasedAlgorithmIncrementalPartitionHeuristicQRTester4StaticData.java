package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.roughEquivalenceClassBased.nestedEquivalenceClass.IncrementalPartitionNECBasedStrategy;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.MostSignificanceResult;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;
import featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.result.ReductionResult;
import featureSelection.repository.entity.alg.rec.nestedEC.reductionResult.ReductionResult4Static;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.core.CoreProcedureContainer4IPNEC;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.core.RecursionBasedCoreProcedureContainer4IPNEC;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.inspect.InspectionProcedureContainer4IPNEC;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.inspect.RecursionBasedInspectionProcedureContainer4IPNEC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Incremental Partition Nested Equivalence Classes based (IP-NEC)</strong>
 * Feature Selection.
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 6
 * {@link ProcedureComponent}s, refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Initiate Equivalence Classes</strong>:
 * 		<p>Calculate the global equivalence classes induced by C: <i>U/C</i>
 * 	</li>
 * 	<li>
 * 		<strong>Get Core</strong>: 
 * 		<p>Calculate Core. 
 * 		<p><code>CoreProcedureContainer4IPNEC</code>
 * 		<p><code>RecursionBasedCoreProcedureContainer4IPNEC</code>
 * 	</li>
 * 	<li>
 * 		<strong>Preparations for sig loop</strong>: 
 * 		<p>Preparations before the execution of significant attributes searching loop. Mostly, if
 * 	    	core has been obtained and it is not empty, equivalence classes are partitioned using
 * 	    	the core. Otherwise, execute the 1st loop of the significant attribute searching.
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>: 
 * 		<p>Loop and search for the most significant attribute and add as an attribute of the reduct until 
 * 			reaching exit condition.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Reduct Inspection</strong>: 
 * 		<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>InspectionProcedureContainer4IPNEC</code>
 * 		<p><code>RecursionBasedInspectionProcedureContainer4IPNEC</code>
 * 	</li>
 * 	<li>
 * 	    <strong>Nested Equivalence Class loading</strong>:
 * 	    <p>Load Nested Equivalence classes for future streaming data computations.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_DEVIATION}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_QR_EXEC_CORE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_PROCEDURE_EXIT_MARK}</li>
 * 	<li>equClasses</li>
 * 	<li>nestedEquClasses</li>
 * 	<li>reductionResult4Static</li>
 * </ul>
 *
 * @see CoreProcedureContainer4IPNEC
 * @see RecursionBasedCoreProcedureContainer4IPNEC
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see InspectionProcedureContainer4IPNEC
 * @see RecursionBasedInspectionProcedureContainer4IPNEC
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StaticData<Sig extends Number>
	extends SelectiveComponentsProcedureContainer<ReductionResult4Static<Collection<Integer>, Sig>>
	implements TimeSum,
				StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>,
				HashSearchStrategy,
				IncrementalPartitionNECBasedStrategy,
				QuickReductHeuristicReductStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private Collection<String> reportKeys;
	private String[] componentExecOrder;
	
	public NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StaticData(ProcedureParameters parameters, boolean logOn) {
		super(logOn, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "QR-IP-NEC";
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
	public void initDefaultComponents(boolean logOn) {
		ProcedureComponent<?>[] componentArray = new ProcedureComponent<?>[] {
			// 1. Initiate Equivalence Classes.
			new TimeCountedProcedureComponent<Collection<EquivalenceClass>>(
					ComponentTags.TAG_COMPACT,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES)
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances = (Collection<Instance>) parameters[p++];
						int[] attributes = (int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return NestedEquivalenceClassBasedAlgorithm
								.Basic
								.equivalenceClass(instances, new IntegerArrayIterator(attributes))
								.values();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("equClasses", result);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> universeInstances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder()
								.loadCurrentInfo(universeInstances, false)
								.setCompressedInstanceNumber(result.size())
								.setExecutedRecordNumberNumber(result.size(), EquivalenceClass.class)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								result.size(), 
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
				}.setDescription("Initiate Equivalence Classes"),
			// 2. Get Core.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_QR_EXEC_CORE),
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Boolean byCore = (Boolean) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						return byCore!=null && byCore?
								(Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec():
								null;//*/
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						if (reduct!=null) {
							getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								0
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get Core")
				.setSubProcedureContainer(
					"CoreProcedureContainer", 
					//new CoreProcedureContainer4IPNEC(getParameters(), logOn)
					new RecursionBasedCoreProcedureContainer4IPNEC(getParameters(), logOn)
				),
			// 3. Preparations for sig loop.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
						});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes =
								(int[]) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						Class<? extends NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig>> calculationClass =
								(Class<? extends NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig>>)
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						boolean exit;
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses = null;
						NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation =
								calculationClass.newInstance();
						if (reduct==null||reduct.isEmpty()) {
							// Do the searching of the 1st most significant attribute and use it to obtain NECs.
							MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig> result =
								NestedEquivalenceClassBasedAlgorithm
									.IncrementalPartition
									.mostSignificantAttribute(equClasses, attributes, calculation, sigDeviation);
							exit = result.isEmptyBoundary();
							nestedEquClasses = result.getNestedEquClasses();
							reduct = new HashSet<>(attributes.length);
							reduct.add(result.getSignificantAttribute());
						}else {
							// Use core to partition equivalence classes.
							NestedEquivalenceClassesInfo<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>> nestedEquClassesInfo =
									NestedEquivalenceClassBasedAlgorithm
										.Basic
										.nestedEquivalenceClass(equClasses, new IntegerCollectionIterator(reduct));
							// If no 0-NEC exists in NECs, exit.
							exit = nestedEquClassesInfo.isEmptyBoundaryClass();
							// If not exit, set nested equivalence classes.
							nestedEquClasses = nestedEquClassesInfo.getNestedEquClasses().values();
						}
						return new Object[] {
								exit, reduct, nestedEquClasses, calculation
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean exit = (boolean) result[r++];
						Collection<Integer> reduct = (Collection<Integer>) result[r++];
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses =
								(Collection<NestedEquivalenceClass<EquivalenceClass>>) result[r++];
						NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation =
								(NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_PROCEDURE_EXIT_MARK, exit);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						getParameters().setNonRoot("nestedEquClasses", nestedEquClasses);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Preparations for sig loop"),
			// 4. Sig loop.
			new TimeCountedProcedureComponent<ReductionResult4Static<Collection<Integer>, Sig>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("4. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PROCEDURE_EXIT_MARK),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("equClasses"),
								getParameters().get("nestedEquClasses"),
						});
					}, 
					false, (component, parameters)->{
						int p=0;
						/* ------------------------------------------------------------------------------ */
						boolean exit =
								(boolean) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation =
								(NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig>)
								parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses =
								(Collection<NestedEquivalenceClass<EquivalenceClass>>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Sig redSig;
						if (!exit || reduct.isEmpty()) {
							TimerUtils.timePause((TimeCounted) component);
							ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig> result =
									(ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec();
							reduct = result.getReduct();
							redSig = result.getReductSig();
							nestedEquClasses = result.getWrappedInstances();
							TimerUtils.timeContinue((TimeCounted) component);
						}else {
							TimerUtils.timePause((TimeCounted) component);
							redSig = calculation.calculate(new IntegerCollectionIterator(reduct), equClasses)
												.getResult();
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return new ReductionResult4Static<>(nestedEquClasses, reduct, redSig);
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("reductionResult4Static", result);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result.getReduct());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result.getReduct());
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "|Reduct Candidate| = {}"), result.getReduct().size());
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, result.getReduct());
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								result.getReduct().size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+".";
					}
				}.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer<>(getParameters(), logOn)
				).setDescription("Sig loop"),
			// 5. Reduct Inspection.
			new ProcedureComponent<ReductionResult4Static<Collection<Integer>, Sig>>(
					ComponentTags.TAG_CHECK,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("5. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("reductionResult4Static"),
						});
					}, 
					(component, parameters)->{
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionResult4Static<Collection<Integer>, Sig> result = 
								(ReductionResult4Static<Collection<Integer>, Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						if (result.getReduct().size()>1) {
							result.setReduct(
									(Collection<Integer>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec()
								);
						}
						return result;
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[PARAMETER_REDUCT_LIST_AFTER_INSPECTATION]
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, result.getReduct());
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"), result.getReduct().size());
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT, result.getReduct());
						/* ------------------------------------------------------------------------------ */
						// Report
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								result.getReduct().size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+".";
					}
				}.setSubProcedureContainer(
					"ReductInspectionProcedureContainer", 
//					new InspectionProcedureContainer4IPNEC(getParameters(), logOn)
					new RecursionBasedInspectionProcedureContainer4IPNEC(getParameters(), logOn)
				).setDescription("Reduct Inspection"),//*/
			// 6. Nested Equivalence Class loading.
			new TimeCountedProcedureComponent<ReductionResult4Static<Collection<Integer>, Sig>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("6. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								getParameters().get("reductionResult4Static"),
						});
					}, 
					false, (component, parameters)->{
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						ReductionResult4Static<Collection<Integer>, Sig> result = 
								(ReductionResult4Static<Collection<Integer>, Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						result.setNestedEquClasses(
							NestedEquivalenceClassBasedAlgorithm
								.Basic
								.nestedEquivalenceClass(equClasses, new IntegerCollectionIterator(result.getReduct()))
								.getNestedEquClasses()
								.values()
						);
						return result;
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								equClasses.size(), 
								result.getReduct().size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+".";
					}
				}.setDescription("Nested Equivalence Class loading"),//*/
		};
		//	Component order.
		componentExecOrder = new String[componentArray.length];
		for (int i=0; i<componentArray.length; i++) {
			this.setComponent(componentArray[i].getDescription(), componentArray[i]);
			componentExecOrder[i] = componentArray[i].getDescription();
		}
	}
	
	@Override
	public long getTime() {
		return getComponents().stream()
				.map(comp-> ProcedureUtils.Time.sumProcedureComponentTimes(comp))
				.reduce(Long::sum).orElse(0L);
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
	
	@Override
	public String[] componentsExecOrder() {
		return componentExecOrder;
	}

	@SuppressWarnings("unused")
	private void redWithBoundaries(Collection<EquivalenceClass> equClasses, Collection<Integer> red) throws Exception {
		if (RoughEquivalenceClassBasedUtils
				.Validation
				.redWithBoundaries4EquivalenceClasses(equClasses, new IntegerCollectionIterator(red))
		) {
			throw new Exception("0-NEC exists with reduct: "+red);
		}
	}
}