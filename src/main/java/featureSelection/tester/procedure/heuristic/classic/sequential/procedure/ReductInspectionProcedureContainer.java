package featureSelection.tester.procedure.heuristic.classic.sequential.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
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
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct inspection for <strong>Quick Reduct - Classic reduction</strong> using
 * <code>Sequential Search</code> when searching is involved.
 * <p>
 * This procedure contains 2 ProcedureComponents refer to steps: 
 * <ul>
 * 	<li>
 * 		<strong>Inspectation procedure controller</strong>
 * 		<p>Control loops over attributes of reduct, and calculates their outer
 * 			significance. If removing the attribute from the feature subset doesn't have
 * 			any effect on the significance(i.e <code>sig(P-{a})==sig(P)</code>), it is
 * 			redundant and removed instantly.
 * 	 <p>Notice that the length of reduct in loops could change if redundant attributes
 * 	 	are found and removed.
 * 	</li>
 * 	<li>
 * 		<strong>Inspect reduct redundancy</strong>
 * 		<p>Calculate the inner significance of the attribute and determine if it is a Core
 * 			attribute.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link DefaultProcedureContainer}(only!), the following parameters are used in 
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_DEVIATION}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * </ul>
 * 
 * @param <Sig>
 * 		Type of Feature(subset) significance.
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ReductInspectionProcedureContainer<Sig extends Number> 
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	private int loopCount;
	
	private Map<String, Object> localParameters;
	
	public ReductInspectionProcedureContainer(
			ProcedureParameters parameters, boolean logOn
	) {
		super(parameters);
		this.logOn = logOn;
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
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "1/{}. {}"),
									getComponents().size(), component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								new HashSet<>(getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						ClassicSequentialCalculation<Sig> calculation = 
								(ClassicSequentialCalculation<Sig>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Loop over attributes in reduct
						Collection<List<Instance>> decEqu =
								ClassicAttributeReductionSequentialAlgorithm
									.Basic
									.equivalenceClassOfDecisionAttribute(instances);
						Sig globalSig = calculation.calculate(instances, new IntegerCollectionIterator(red), decEqu)
													.getResult();
						Integer[] redCopy = red.toArray(new Integer[red.size()]);
						for (int attr: redCopy) {
							TimerUtils.timePause((TimeCounted) component);
							
							loopCount++;
							localParameters.put("globalSig", globalSig);
							localParameters.put("decEqu", decEqu);
							localParameters.put("examAttr", attr);
							localParameters.put("red", red);
							
							this.getComponents().get(1).exec();
							
							red = (Collection<Integer>) localParameters.get("red");
							
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return red;
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
				}.setDescription("Inspectation procedure controller"),
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
								localParameters.get("globalSig"),
								localParameters.get("decEqu"),
								localParameters.get("examAttr"),
								localParameters.get("red"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsRedundant = true;
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						ClassicSequentialCalculation<Sig> calculation =
								(ClassicSequentialCalculation<Sig>) 
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						Collection<List<Instance>> decEqu =
								(Collection<List<Instance>>) parameters[p++];
						int examAttr =
								(int) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						red.remove(examAttr);
						Sig examSig = calculation.calculate(instances, new IntegerCollectionIterator(red), decEqu)
												.getResult();
						if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)) {
							red.add(examAttr);
							
							TimerUtils.timePause((TimeCounted) component);
							attributeIsRedundant = false;
							TimerUtils.timeContinue((TimeCounted) component);
						}
						
						return new Object[] {
							red,
							examAttr,
							attributeIsRedundant,
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> red = (Collection<Integer>) result[r++];
						int examAttr = (int) result[r++];
						boolean attributeIsRedundant = (boolean) result[r++];
						/* ------------------------------------------------------------------------------ */
						localParameters.put("red", red);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = "Loop["+loopCount+"] Attr["+examAttr+"]";
						reportKeys.add(reportMark);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
							);
						//	[REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE,
								examAttr
						);
						//	[REPORT_INSPECT_ATTRIBUTE_REDUNDANT]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_ATTRIBUTE_REDUNDANT,
								attributeIsRedundant
						);
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