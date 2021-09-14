package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.inspect;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
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
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.core.RecursionBasedCoreExecutionAction4IPNEC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Inspection for <strong>(Simple) Nested Equivalence Classes based (S-NEC)</strong> Feature
 * Selection. This procedure contains 2 {@link ProcedureComponent}2:
 * <ul>
 * 	<li>
 * 		<strong>Initiate parameters</strong>
 * 		<p>Initiate parameters for inspection, including {@link AttrProcessStrategyParams},
 * 	        {@link AttrProcessStrategy4Comb#initiate(IntegerIterator)}
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>
 * 		<p>Use {@link RecursionBasedCoreExecutionAction4IPNEC} to execute inspection.
 * 	</li>
 * </ul>
 *
 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition.Core.RecursionBased#compute(
 *      AttrProcessStrategy4Comb, AttributeProcessStrategy, Collection,
 *      Shrink4RECBoundaryClassSetStays)
 * @see RecursionBasedCoreExecutionAction4IPNEC
 *
 * @author Benjamin_L
 */
@Slf4j
public class RecursionBasedInspectionProcedureContainer4IPNEC
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public RecursionBasedInspectionProcedureContainer4IPNEC(ProcedureParameters parameters, boolean logOn) {
		super(logOn? null: log, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Inspect(IP-NEC)";
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
			// 1. Initiate parameters.
			new TimeCountedProcedureComponent<AttrProcessStrategy4Comb>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get("inspectAttributeProcessCapacityCalculator"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
						});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						CapacityCalculator inspectAttributeProcessCapacityCalculator = (CapacityCalculator) parameters[p++];
						Collection<Integer> red = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						AttrProcessStrategyParams inspectAttributeProcessStrategyParams =
							new AttrProcessStrategyParams()
								.set(AttrProcessStrategy4Comb.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
										inspectAttributeProcessCapacityCalculator);
						return new AttrProcessStrategy4Comb(
									inspectAttributeProcessStrategyParams
								).initiate(new IntegerCollectionIterator(red));
					}, 
					(component, inspectAttributeProcessStrategy) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("inspectAttributeProcessStrategy", inspectAttributeProcessStrategy);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = component.getDescription();
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
														
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate parameters"),
			// 2. Inspection.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								localParameters.get("inspectAttributeProcessStrategy"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						Collection<EquivalenceClass> equClasses = (Collection<EquivalenceClass>) parameters[p++];
						AttrProcessStrategy4Comb inspectAttributeProcessStrategy = 
								(AttrProcessStrategy4Comb) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						long lastExecTime = 0;
						boolean recursion = true;
						RecursionBasedInspectionExecutionAction4IPNEC execution = null;
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						for (int i=1; recursion; i++) {
							TimerUtils.timePause((TimeCounted) component);
							
							execution = 
								new RecursionBasedInspectionExecutionAction4IPNEC(
										inspectAttributeProcessStrategy, 
										equClasses
								);
							
							TimerUtils.timeContinue((TimeCounted) component);
							recursion = execution.exec(component, getParameters());
							TimerUtils.timePause((TimeCounted) component);
						
							long totalExecTime = ((TimeCountedProcedureComponent<?>) component).getTime();
							long recursionTime = totalExecTime - lastExecTime;
							lastExecTime = totalExecTime;
							/* ------------------------------------------------------------------------------ */
							// Report
							String reportMark = "Recursion ".intern()+i;
							reportKeys.add(reportMark);
							//	[REPORT_EXECUTION_TIME]
							ProcedureUtils
									.Report
									.ExecutionTime
									.save(report, reportMark, recursionTime);
							/* ------------------------------------------------------------------------------ */
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return execution.getReduct();
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, red);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						Collection<Instance> universes = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								universes.size(), 
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
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection"),
		};
	}

	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each : components)	this.getComponents().add(each);
		for (int i=0; i<2; i++)							components[i].exec();
		return getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION);
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	public String reportMark(String componentDesc) {
		return "Recursion["+componentDesc+"]";
	}
}