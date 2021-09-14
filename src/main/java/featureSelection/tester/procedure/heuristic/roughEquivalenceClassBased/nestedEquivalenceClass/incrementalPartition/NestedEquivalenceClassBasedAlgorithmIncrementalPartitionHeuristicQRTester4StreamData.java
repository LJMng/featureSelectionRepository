package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
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
import featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.result.ReductionResult;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMerger;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMergerParameters;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedStreamingDataCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.BoundaryClassSetHandlingProcedureContainer4StreamData;
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
 * This is a {@link DefaultProcedureContainer}. Procedure contains 6 {@link ProcedureComponent}s, refer
 * to steps:
 * <ul>
 * 	<li>
 * 		<strong>Initiate Equivalence Classes</strong>:
 * 		<p>Calculate the global equivalence classes induced by C: <i>U/C</i>
 * 	</li>
 * 	<li>
 * 		<strong>Get Core</strong>: 
 * 		<p>Calculate Core. 
 * 		<p><code>CoreProcedureContainer4IPNECTailormade</code>
 * 		<p><code>RecursionBasedCoreProcedureContainer4IPNEC</code>
 * 	</li>
 * 	<li>
 * 		<strong>Preparations for sig loop</strong>: 
 * 		<p>Preparations before the execution of significant attributes searching loop. Mostly, if core has been
 * 			calculated and is not empty, equivalence classes are further partitioned using the core, otherwise,
 * 			execute the 1st loop of the significant attribute searching.
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
public class NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StreamData
		<Sig extends Number, MergeParams extends NestedEquivalenceClassesMergerParameters>
	extends DefaultProcedureContainer<ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>
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
	private boolean logOn;
	
	public NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StreamData(ProcedureParameters paramaters, boolean logOn) {
		super(logOn? log: null, paramaters);
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		this.logOn = logOn;
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
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Controller.
			new TimeCountedProcedureComponent<ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Object previousNestedEquClasses = parameters[p++];
						Collection<Integer> previousReduct = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						ProcedureComponent<?> comp2 = getComponents().get(2);
						ProcedureComponent<?> comp3 = getComponents().get(3);
						ProcedureComponent<ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>> comp5 =
								(ProcedureComponent<ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>)
								getComponents().get(5);
						/* ------------------------------------------------------------------------------ */
						// if the previous nested equivalence classes are wrapped in collection, transform
						//	into a map.
						if (previousNestedEquClasses instanceof Collection) {
							TimerUtils.timeStart((TimeCounted) component);
							Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClassValues =
									(Collection<NestedEquivalenceClass<EquivalenceClass>>) previousNestedEquClasses;
							
							Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses = new HashMap<>(nestedEquClassValues.size());
							nestedEquClassValues.stream().forEach(nec->{
								int[] keyValue = new int[previousReduct.size()];
								Iterator<Integer> preivousReductIterator = previousReduct.iterator();
								for (int i=0; i<keyValue.length; i++)
									keyValue[i] = preivousReductIterator.next();
								nestedEquClasses.put(new IntArrayKey(keyValue), nec);
							});
							TimerUtils.timePause((TimeCounted) component);
							
							getParameters().setNonRoot(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM, nestedEquClasses);
						}
						/* ------------------------------------------------------------------------------ */
						comp1.exec();
						comp2.exec();
						comp3.exec();
						ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig> result =
								comp5.exec();
						
						if (previousNestedEquClasses instanceof Collection)
							TimerUtils.timeContinue((TimeCounted) component);
						else
							TimerUtils.timeStart((TimeCounted) component);
						
						return result;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<EquivalenceClass> equClasses = new LinkedList<>();
						result.getWrappedInstances().values().forEach(nec->equClasses.addAll(nec.getEquClasses().values()));
						redWithBoundaries(equClasses, result.getReduct());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Controller"),
			// 2. Initiate Equivalence Classes.
			new TimeCountedProcedureComponent<Map<IntArrayKey, EquivalenceClass>>(
					ComponentTags.TAG_COMPACT,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES)
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<Instance> instances = (Collection<Instance>) parameters[0];
						int[] attributes = (int[]) parameters[1];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return NestedEquivalenceClassBasedAlgorithm
								.Basic
								.equivalenceClass(instances, new IntegerArrayIterator(attributes));
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("arrivedEquivalenceClasses", result);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> universeInstances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder()
								.loadCurrentInfo(universeInstances, false)
								.setCompressedInstanceNumber(result.size())
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
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate Equivalence Classes"),
			// 3. Update (Nested) Equivalence Classes for arrived data.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT_SIG),
								getParameters().get(ParameterConstants.PARAMETER_IP_NEC_DYNAMIC_DATA_EQUIVALENCE_CLASS_MERGER),
								getParameters().get("arrivedEquivalenceClasses"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes = (int[]) parameters[p++];
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousReductNestedEquClasses =
								(Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>) parameters[p++];
						Class<? extends NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams>> calculationClass =
								(Class<? extends NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams>>)
								parameters[p++];
						Collection<Integer> previousReduct = (Collection<Integer>) parameters[p++];
						Sig previousReductSig = (Sig) parameters[p++];
						NestedEquivalenceClassesMerger<MergeParams, NestedEquivalenceClass<EquivalenceClass>> merger =
								(NestedEquivalenceClassesMerger<MergeParams, NestedEquivalenceClass<EquivalenceClass>>)
								parameters[p++];
						Map<IntArrayKey, EquivalenceClass> arrivedEquivalenceClasses =
								(Map<IntArrayKey, EquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams> calculation =
								calculationClass.newInstance();
						// Update Nested Equivalence Classes for arrived data.
						calculation.update4Arrived(
								new NestedEquivalenceClassBasedStreamingDataCalculation
										.Update4ArrivedInputs<Sig, MergeParams>(
											attributes,
											previousReductNestedEquClasses, 
											new IntegerCollectionIterator(previousReduct),
											previousReductSig,
											arrivedEquivalenceClasses,
											merger
										)
						);
						Sig newSig = calculation.getResult();
						return new Object[] {
								newSig, calculation, 
								calculation.getNecInfoWithMap().getNestedEquClasses()
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Sig newSig = (Sig) result[r++];
						NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams> calculation =
								(NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams>)
								result[r++];
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses = (Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("newSig", newSig);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						getParameters().setNonRoot("nestedEquClasses", nestedEquClasses);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						BasicExecutionInstanceInfo.Builder basicExecutionInstanceInfoBuilder = 
							(BasicExecutionInstanceInfo.Builder)
							statistics.get(StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER);
						basicExecutionInstanceInfoBuilder.setExecutedRecordNumberNumber(
							nestedEquClasses.values().stream()
								.mapToInt(NestedEquivalenceClass::getItemSize)
								.sum(),
							EquivalenceClass.class
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Map<?, ?>) getParameters().get("arrivedEquivalenceClasses")).size(),
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update (Nested) Equivalence Classes"),
			// 4. Boundary Class set checking.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get("newSig"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams> calculation =
								(NestedEquivalenceClassBasedStreamingDataCalculation<Sig, MergeParams>)
								parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						Sig newSig =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>> comp4 =
								(ProcedureComponent<ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>)
								getComponents().get(4);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (!calculation.getNecInfoWithMap().isEmptyBoundaryClass()) {
							TimerUtils.timePause((TimeCounted) component);
							ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig> updatedResult =
									comp4.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							return new Object[] {
									true,
									updatedResult.getReduct(),
									updatedResult.getReductSig()
								};
						}else {
							return new Object[] {
									false,
									previousReduct, 
									newSig
								};
						}
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean updated = (boolean) result[r++];
						Collection<Integer> reduct = (Collection<Integer>) result[r++];
						Sig redSig = (Sig) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_PROCEDURE_EXIT_MARK, !updated);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						getParameters().setNonRoot("redSig", redSig);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						if (!updated) {
							// [STATISTIC_RED_BEFORE_INSPECT]
							statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, reduct);
							// [STATISTIC_RED_AFTER_INSPECT]
							statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT, reduct);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Map<?, ?>) getParameters().get("arrivedEquivalenceClasses")).size(),
								0
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Boundary Class set checking"),
			// 5. Boundary Class set exist handling.
			new ProcedureComponent<ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						return (ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("updatedResult", result);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Boundary Class set exist handling")
				.setSubProcedureContainer(
					"BoundaryClassSetHandlingProcedureContainer4StreamData", 
					new BoundaryClassSetHandlingProcedureContainer4StreamData<>(getParameters(), logOn)
				),
			// 6. Nested Equivalence Class map updating.
			new TimeCountedProcedureComponent<ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("6. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT_SIG),
								getParameters().get(ParameterConstants.PARAMETER_PROCEDURE_EXIT_MARK),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("updatedResult"),
								getParameters().get("nestedEquClasses"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousReductNestedEquClasses =
								(Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>)
								parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						Sig previousReductSig =
								(Sig) parameters[p++];
						boolean exit =
								(boolean) parameters[p++];
						Collection<Integer> newReduct =
								(Collection<Integer>) parameters[p++];
						ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig> updatedResult =
								(ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>)
								parameters[p++];
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses =
								(Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (!exit) {
							Collection<Integer> reduct = new ArrayList<>(previousReduct.size()+newReduct.size());
							reduct.addAll(newReduct);
							reduct.addAll(previousReduct);
							Collections.sort((ArrayList<Integer>) reduct);
							
							Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> fullPreviousReductNestedEquClasses =
								new HashMap<>(previousReductNestedEquClasses.size()+nestedEquClasses.size());
							fullPreviousReductNestedEquClasses.putAll(previousReductNestedEquClasses);
							fullPreviousReductNestedEquClasses.putAll(nestedEquClasses);
							
							return new ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>(
									NestedEquivalenceClassBasedAlgorithm
										.IncrementalPartition
										.updateNestedEquivalenceClassKey(
											fullPreviousReductNestedEquClasses.values().stream()
												.mapToInt(NestedEquivalenceClass::getInstanceSize)
												.sum(), 
											new IntegerCollectionIterator(reduct),
											newReduct, 
											fullPreviousReductNestedEquClasses
										), 
									reduct, 
									updatedResult.getReductSig()
								);
						}else {
							return new ReductionResult<>(
									previousReductNestedEquClasses, 
									previousReduct, 
									previousReductSig
								);
						}
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							int universeSize = reduct.getWrappedInstances().values().stream()
												.mapToInt(NestedEquivalenceClass::getInstanceSize)
												.sum();
							log.info(LoggerUtil.spaceFormat(1, "|Updated U| = {}"), universeSize);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT, reduct.getReduct());
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Map<?, ?>) getParameters().get("arrivedEquivalenceClasses")).size(),
								0
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Nested Equivalence Class map updating"),
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
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig> exec() throws Exception {
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each: components)	getComponents().add(each);
		return (ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>) components[0].exec();
	}

	private void redWithBoundaries(Collection<EquivalenceClass> equClasses, Collection<Integer> red) throws Exception {
		if (RoughEquivalenceClassBasedUtils
				.Validation
				.redWithBoundaries4EquivalenceClasses(equClasses, new IntegerCollectionIterator(red))
		) {
			throw new Exception("0-NEC exists with reduct: "+red);
		}
	}
}