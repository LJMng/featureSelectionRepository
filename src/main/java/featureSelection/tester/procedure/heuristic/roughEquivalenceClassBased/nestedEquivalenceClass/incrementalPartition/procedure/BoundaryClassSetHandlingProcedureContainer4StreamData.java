package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4CombInReverse;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import featureSelection.repository.entity.alg.rec.nestedEC.MostSignificanceResult;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;
import featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.result.ReductionResult;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Boundary {@link ClassSet} for <strong>Incremental Partition Nested Equivalence Classes based
 * (IP-NEC)</strong> Feature Selection. This {@link DefaultProcedureContainer} is executed with
 * {@link ClassSetType#BOUNDARY} {@link NestedEquivalenceClass}es only for stream data computations.
 * <p>
 * This procedure contains 3 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Controller</strong>
 * 		<p>Control the computation of obtaining core.
 * 	</li>
 * 	<li>
 * 		<strong>Seek the current most significant attribute</strong>
 * 		<p>Loop and search for attributes with max. significance value in attributes outside of
 *  * 		reduct, and add into reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>
 * 		<p>Use IP-NEC strategy to inspect reduct.
 * 	</li>
 * </ul>
 *
 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition.Core.RecursionBased#compute(
 *      AttrProcessStrategy4Comb, AttributeProcessStrategy, Collection,
 *      Shrink4RECBoundaryClassSetStays)
 *
 * @author Benjamin_L
 */
@Slf4j
public class BoundaryClassSetHandlingProcedureContainer4StreamData<Sig extends Number>
	extends DefaultProcedureContainer<ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	private int loopCount = 0;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public BoundaryClassSetHandlingProcedureContainer4StreamData(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Boundary class set handling";
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
			new TimeCountedProcedureComponent<ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get("attributes left"),
								getParameters().get("nestedEquClasses"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						int[] attributesLeft =
								(int[]) parameters[p++];
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses =
								(Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig>> comp1 =
								(ProcedureComponent<MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig>>)
								getComponents().get(1);
						ProcedureComponent<Collection<Integer>> comp2 =
								(ProcedureComponent<Collection<Integer>>)
								getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						boolean exit = false;
						Collection<Integer> reduct = new HashSet<>(attributesLeft.length);
						
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("reduct", reduct);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						TimerUtils.timeContinue((TimeCounted) component);
						
						// Loop until no 0-NEC is left under the partitioning of reduct(i.e. exit=true).
						Sig redSig = null;
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClassValues = nestedEquClasses.values();
						MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig> sigSearchResult;
						while (!exit) {
							TimerUtils.timePause((TimeCounted) component);
							loopCount++;
							localParameters.put("nestedEquClassesValues", nestedEquClassValues);
							sigSearchResult = comp1.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							
							reduct.add(sigSearchResult.getSignificantAttribute());
							nestedEquClassValues = sigSearchResult.getNestedEquClasses();
							exit = sigSearchResult.isEmptyBoundary();
							redSig = sigSearchResult.getSignificance();
						};
						
						TimerUtils.timePause((TimeCounted) component);
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						Collection<Integer> fullReduct = new ArrayList<>(reduct.size()+previousReduct.size());
						fullReduct.addAll(previousReduct);
						fullReduct.addAll(reduct);
						Collections.sort((List<Integer>) fullReduct);
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, fullReduct);
						reduct = comp2.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						
						return new ReductionResult<>(nestedEquClassValues, reduct, redSig);
					}, 
					(component, result)->{
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
			// 2. Seek the current most significant attribute.
			new TimeCountedProcedureComponent<MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}."), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("attributes left"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								localParameters.get("nestedEquClassesValues"),
								localParameters.get("reduct")
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributesLeft = (int[]) parameters[p++];
						NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation =
								(NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation = (Sig) parameters[p++];
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClassesValues =
								(Collection<NestedEquivalenceClass<EquivalenceClass>>) parameters[p++];
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// maxSig=0, newRed=null
						int sigAttr = -1;
						Sig maxSig = null;
						// Go through a in P
						NestedEquivalenceClassesInfo<Collection<NestedEquivalenceClass<EquivalenceClass>>> sigResult = null;
						for (int a=0; a<attributesLeft.length; a++) {
							if (reduct.contains(attributesLeft[a]))	continue;
							// red = red U a
							// NEC, sig, flag = NEC(NEC, P)
							calculation.incrementalCalculate(new IntegerArrayIterator(new int[] {attributesLeft[a]}), nestedEquClassesValues);
							// if sig>maxSig, mark
							if (maxSig==null||calculation.value1IsBetter(calculation.getResult(), maxSig, sigDeviation)) {
								sigAttr = attributesLeft[a];
								maxSig = calculation.getResult();
								sigResult = calculation.getNecInfoWithCollection();
							}
						}
						// return NEC, newRed, sig, flag
						return new MostSignificanceResult<>(
									sigResult.getNestedEquClasses(),
									sigAttr,
									maxSig,
									sigResult.isEmptyBoundaryClass()
								);
					}, 
					(component, sig) -> {
						/* ------------------------------------------------------------------------------ */
						int sigAttr = sig.getSignificantAttribute();
						Sig maxSig = sig.getSignificance();
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("sig", sig);
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "sig = {}"), sig.getSignificance());
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "+ attribute = {} | significance = {}"), 
									sigAttr, maxSig);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH, 1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Map<?, ?>) getParameters().get("arrivedEquivalenceClasses")).size(),
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY,
								sigAttr
						);
						//	[REPORT_SIG_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								maxSig
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek the current most significant attribute"),
			// 3. Inspection.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3/{}. {}."), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("inspectAttributeProcessCapacityCalculator"),
								getParameters().get("nestedEquClasses"),
								new HashSet<>((Collection<Integer>) localParameters.get("reduct")),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						CapacityCalculator inspectAttributeProcessCapacityCalculator =
								(CapacityCalculator) parameters[p++];
						Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses =
								(Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>) parameters[p++];
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Inspect redundancy and return.
						AttrProcessStrategyParams inspectAttributeProcessStrategyParams = 
								new AttrProcessStrategyParams()
									.set(AttrProcessStrategy4Comb.PARAMETER_EXAM_CAPACITY_CALCULATOR,
											inspectAttributeProcessCapacityCalculator
									);
						reduct = new LinkedList<>(
							NestedEquivalenceClassBasedAlgorithm
								.IncrementalPartition
								.Inspection
								.computeNestedEquivalenceClasses(
									new AttrProcessStrategy4CombInReverse(inspectAttributeProcessStrategyParams)
										.initiate(new IntegerCollectionIterator(reduct)),
									nestedEquClasses.values()
								)
						);
						return reduct;
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "|Reduct inspected|={}"), reduct.size());
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Map<?, ?>) getParameters().get("arrivedEquivalenceClasses")).size(),
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig> exec() throws Exception {
		Object obj;
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
		obj = comps[0].exec();
		// Statistics
		//	[Sig loop times]
		statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES, loopCount);
		return (ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>) obj;
	}

	public String reportMark() {
		return "Loop["+loopCount+"]";
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}