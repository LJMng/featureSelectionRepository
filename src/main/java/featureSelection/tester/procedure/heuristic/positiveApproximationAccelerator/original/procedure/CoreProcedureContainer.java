package featureSelection.tester.procedure.heuristic.positiveApproximationAccelerator.original.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
import common.utils.StringUtils;
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
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Core searching for <strong>Positive Approximation Accelerator (ACC)</strong> Feature Selection.
 * This procedure contains 2 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Core procedure controller</strong>
 * 		<p>Control loop over attributes that are not in reduct, and calculate their
 * 			inner significance. If removing the attribute from the feature subset doesn't
 * 			have any effect on the significance(i.e <code>sig(C-{a})==sig(C)</code>), it
 * 			is NOT a core attribute.
 * 	</li>
 * 	<li>
 * 		<strong>Core</strong>
 * 		<p>Calculate the inner significance of the attribute and determine if it is a
 * 			Core attribute.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class CoreProcedureContainer<Sig extends Number> 
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public CoreProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Core";
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
			// 1. Core procedure controller
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int[] attributes = (int[]) parameters[0];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// core = {}
						Collection<Integer> core = new HashSet<>(attributes.length);
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("core", core);
						TimerUtils.timeContinue((TimeCounted) component);
						// Loop over all attributes and check
						int[] examAttributes = new int[attributes.length-1];
						for (int i=0; i<examAttributes.length; i++)
							examAttributes[i] = attributes[i+1];
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("examAttributes", examAttributes);
						TimerUtils.timeContinue((TimeCounted) component);
						for (int i=0; i<attributes.length; i++) {
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("i", i);
							comp1.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return core;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "core={}"),
									StringUtils.numberToString(result, 50, 0)
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						int[] attributes = getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
						//	[STATISTIC_CORE_LIST]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_CORE_LIST,
								(result).toArray(new Integer[result.size()])
						);
						// [STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH]
						ProcedureUtils.Statistics.countInt(
								statistics.getData(),
								StatisticsConstants.Procedure.STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH,
								attributes.length
						);
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
				}.setDescription("Core procedure controller"),
			// 2. Core
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get("globalSig"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								localParameters.get("examAttributes"),
								localParameters.get("i"),
								localParameters.get("core"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsCore = false;
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						PositiveApproximationAcceleratorCalculation<Sig> calculation =
								(PositiveApproximationAcceleratorCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						int[] examAttributes =
								(int[]) parameters[p++];
						int i =
								(int) parameters[p++];
						Collection<Integer> core =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Calculate significance of C-{a}: a.innerSig.
						Collection<EquivalenceClass> roughEquClasses =
							PositiveApproximationAcceleratorOriginalAlgorithm
								.Basic
								.equivalenceClass(
									instances,
									new IntegerArrayIterator(examAttributes)
								);
						Sig sig = calculation.calculate(roughEquClasses, examAttributes.length, instances.size())
											.getResult();
						// if a.innrSig!=C.sig
						if (calculation.value1IsBetter(globalSig, sig, sigDeviation)) {
							core.add(attributes[i]);
							attributeIsCore = true;
						}

						// next attribute.
						if (i<examAttributes.length)
							examAttributes[i] = attributes[i];
						
						return new Object[] {
							attributeIsCore,
							attributes[i],
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean attributeIsCore = (boolean) result[r++];
						int examAttr = (int) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = "Loop["+localParameters.get("i")+"] "+
											"Attr["+result[1]+"]";
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						Collection<Integer> core = (Collection<Integer>) localParameters.get("core");
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_CORE_CURRENT_ATTRIBUTE]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_CORE_CURRENT_ATTRIBUTE,
								examAttr
						);
						//	[REPORT_CORE_INDIVIDUAK_RESULT]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_CORE_INDIVIDUAL_RESULT,
								attributeIsCore
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Core"),
		};	
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each : components)	this.getComponents().add(each);
		return (Collection<Integer>) components[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}