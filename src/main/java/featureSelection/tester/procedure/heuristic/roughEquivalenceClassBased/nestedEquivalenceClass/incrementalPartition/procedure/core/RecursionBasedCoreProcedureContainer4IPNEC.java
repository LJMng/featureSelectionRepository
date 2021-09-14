package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4CombInReverse;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Core computing for <strong>(Simple) Nested Equivalence Classes based (S-NEC)</strong> Feature
 * Selection. This procedure contains 1 {@link ProcedureComponent}:
 * <ul>
 * 	<li>
 * 		<strong>Core procedure controller</strong>
 * 		<p>Control the computation of obtaining core.
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
public class RecursionBasedCoreProcedureContainer4IPNEC
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private boolean logOn;
	
	public RecursionBasedCoreProcedureContainer4IPNEC(ProcedureParameters parameters, boolean logOn) {
		super(logOn? null: log, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		this.logOn = logOn;
	}

	@Override
	public String shortName() {
		return "Core(IP-NEC)";
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
			// 1. Core controller.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get("coreAttributeProcessStrategyParams"),
								getParameters().get("incPartitionAttributeProcessStrategy"),
								getParameters().get("equClasses"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						int[] attributes = (int[]) parameters[p++];
						AttrProcessStrategyParams coreAttributeProcessStrategyParams = 
								(AttrProcessStrategyParams)
								parameters[p++];
						AttributeProcessStrategy incPartitionAttributeProcessStrategy = 
								(AttributeProcessStrategy) parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						long lastExecTime = 0;
						boolean recursion = true;
						RecursionBasedCoreExecutionAction4IPNEC execution = null;
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> core = new HashSet<>(attributes.length);
						Collection<Integer> attributesFiltered = new LinkedList<>();
						AttrProcessStrategy4Comb coreAttributeProcessStrategy =
								new AttrProcessStrategy4CombInReverse(coreAttributeProcessStrategyParams)
									.initiate(new IntegerArrayIterator(attributes));
						Shrink4RECBoundaryClassSetStays shrinkInstance =
								new Shrink4RECBoundaryClassSetStays();
						for (int i=1; recursion; i++) {
							TimerUtils.timePause((TimeCounted) component);
							
							execution = 
								new RecursionBasedCoreExecutionAction4IPNEC(
										core, attributesFiltered,
										coreAttributeProcessStrategy, 
										incPartitionAttributeProcessStrategy,
										equClasses, 
										shrinkInstance
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
						return execution.getCore();
					}, 
					(component, core) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, core);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|Core|: {} | {}"), core.size(), core);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_CORE_LIST]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_CORE_LIST, core.toArray(new Integer[core.size()]));
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
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Core controller"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		Object obj;
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each : components)	this.getComponents().add(each);
		obj = components[0].exec();
		return (Collection<Integer>) obj;
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	public String reportMark(String componentDesc) {
		return "Recursion["+componentDesc+"]";
	}
}