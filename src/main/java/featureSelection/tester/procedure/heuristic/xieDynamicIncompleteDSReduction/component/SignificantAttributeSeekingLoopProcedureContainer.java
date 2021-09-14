package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
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
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.alg.xieDynamicIncompleteDSReduction.DynamicIncompleteDecisionSystemReductionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.MostSignificantAttributeResult;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReduction;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching for <strong>Xie Dynamic In-complete Decision System Reduction(DIDS)
 * </strong> Feature Selection.
 * <p>
 * This procedure contains 2 ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Sig seeking controller</strong>
 * 		<p>Control the process of seeking for reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Seek significant attribute</strong>
 * 		<p>Search for the attribute with the max. significance value in the rest of the attributes(i.e. 
 * 			attributes outside of the reduct), and return as an attribute of the reduct.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class SignificantAttributeSeekingLoopProcedureContainer<Sig extends Number> 
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	private int loopCount = 0;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public SignificantAttributeSeekingLoopProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Sig seeking loop";
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
			// 1. Sig seeking controller.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("globalSig"),
								getParameters().get("redSig"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						Sig sigDeviation = (Sig) parameters[p++];
						FeatureImportance<Sig> calculation = (FeatureImportance<Sig>) parameters[p++];
						Sig globalSig = (Sig) parameters[p++];
						Sig redSig = (Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<MostSignificantAttributeResult<Sig>> comp1 =
								(ProcedureComponent<MostSignificantAttributeResult<Sig>>) 
								this.getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						MostSignificantAttributeResult<Sig> sig;
						while (calculation.value1IsBetter(globalSig, redSig, sigDeviation)) {
							
							TimerUtils.timePause((TimeCounted) component);
							loopCount++;
							sig = comp1.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							
							reduct.add(sig.getAttribute());
							redSig = sig.getSignificance();
						}
						
						return reduct;
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().set(true, ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[Sig loop times]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES, loopCount);
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, reduct);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											component.getDescription(), 
											(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sig seeking controller"),
			// 2. Seek significant attribute.
			new TimeCountedProcedureComponent<MostSignificantAttributeResult<Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4Attributes"),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances = (Collection<Instance>) parameters[p++];
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						int[] attributes = (int[]) parameters[p++];
						FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation =
								(FeatureImportance4XieDynamicIncompleteDSReduction<Sig>) 
								parameters[p++];
						Sig sigDeviation = (Sig) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4Attributes =
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return DynamicIncompleteDecisionSystemReductionAlgorithm
									.mostSignificantAttribute(
										instances, attributes, reduct,
										toleranceClassObtainer, completeData4Attributes,
										calculation, sigDeviation
									);
					}, 
					(component, sig) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(1, "Loop {} | + attribute {} | sig = {}"),
									loopCount, sig.getAttribute(), sig.getSignificance()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_POS_HISTORY]
						List<Sig> increment = (List<Sig>) statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (increment==null)	statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY, increment = new LinkedList<>());
						increment.add(sig.getSignificance());
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
						Collection<Integer> reduct = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH,
							reduct.size()+1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_POSITIVE_INCREMENT_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								sig.getSignificance()
						);
						//	[REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY,
								sig.getAttribute()
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek significant attribute"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps) {
			this.getComponents().add(each);
		}
		return (Collection<Integer>) comps[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
	
	private String reportMark() {
		return "Loop["+loopCount+"]";
	}
}