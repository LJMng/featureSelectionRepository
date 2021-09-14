package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

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
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReduction;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;

/**
 * Reduct inspection for <strong>Xie Dynamic In-complete Decision System Reduction(DIDS)</strong> Feature 
 * Selection.
 * This procedure contains 2 ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Inspection procedure controller</strong>
 * 		<p>Control to loop over attributes in reduct to inspect redundant.
 * 	</li>
 * 	<li>
 * 		<strong>Inspect reduct redundancy</strong>
 * 		<p>Check the redundancy of an attribute by calculating its inner significance. Considered redundant if 
 * 			the inner significance is 0 (i.e. sig(red)=sig(red-{a})).
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * 	<li>globalPos</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
public class ReductInspectionProcedureContainer<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	private int loopCount;
	
	private Map<String, Object> localParameters;
	
	public ReductInspectionProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(parameters);
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Inspect reduct";
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
			// 1. Inspection procedure controller
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								new HashSet<>(getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> reduct = (Collection<Integer>) parameters[0];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Object[]> comp1 = (ProcedureComponent<Object[]>) getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Go through a in R
						Integer[] reductCopy = reduct.toArray(new Integer[reduct.size()]);
						
						for (int attr: reductCopy) {
							TimerUtils.timePause((TimeCounted) component);
							loopCount++;
							localParameters.put("attr", attr);
							comp1.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return reduct;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, result);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								result.size()
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
				}.setDescription("Inspection procedure controller"),
			// 2. Inspect reduct redundancy
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("globalSig"),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4Attributes"),
								localParameters.get("attr"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsRedundant = true;
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation =
								(FeatureImportance4XieDynamicIncompleteDSReduction<Sig>) 
								parameters[p++];
						Sig deviation =
								(Sig) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4Attributes =
								(InstancesCollector) parameters[p++];
						int attr =
								(int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						reduct.remove(attr);
						Sig sig = calculation.calculate(
									instances, new IntegerCollectionIterator(reduct),
									toleranceClassObtainer, completeData4Attributes
								).getResult();
						if (calculation.value1IsBetter(globalSig, sig, deviation)) {
							reduct.add(attr);
							attributeIsRedundant = false;
						}
						return new Object[] {
							attr,
							attributeIsRedundant,
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						int attr = (int) result[r++];
						boolean attributeIsRedundant = (boolean) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = "Loop["+loopCount+"] Attr["+result[1]+"]";
						reportKeys.add(reportMark);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE,
								attr
						);
						//	[REPORT_INSPECT_ATTRIBUTE_REDUNDANT]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_ATTRIBUTE_REDUNDANT,
								attributeIsRedundant
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspect reduct redundancy"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
		return (Collection<Integer>) comps[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}