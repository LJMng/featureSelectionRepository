package featureSelection.tester.procedure.heuristic.classic.hash.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import common.utils.StringUtils;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Core searching for <strong>Quick Reduct - Classic reduction</strong> using
 * <code>HashMap</code> when searching is involved.
 * <p>
 * This procedure contains 2 ProcedureComponents refer to steps: 
 * <ul>
 * 	<li>
 * 		<strong>Core procedure controller</strong>
 * 		<p>Control loops over attributes that are not in reduct, and calculates their
 * 			inner significance. If removing the attribute from the feature subset
 * 			doesn't have any effect on the significance(i.e <code>sig(C-{a})==sig(C)
 * 			</code>), it is NOT a core attribute.
 * 	</li>
 * 	<li>
 * 		<strong>Core</strong>
 * 		<p>Calculate the inner significance of the attribute and determine if it is a
 * 			Core attribute.
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
 * </ul>
 * 
 * @param <Sig>
 * 		Type of Feature(subset) significance.
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
			// 1 Core procedure controller
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
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// core = {}
						Collection<Integer> core = new HashSet<>();
						// Loop over conditional attributes
						int[] examAttributes = new int[attributes.length-1];
						for (int i=0; i<examAttributes.length; i++)	examAttributes[i] = attributes[i+1];
						for (int i=0; i<attributes.length; i++) {
							TimerUtils.timePause((TimeCounted) component);
							
							localParameters.put("i", i);
							localParameters.put("examAttributes", examAttributes);
							localParameters.put("core", core);
							
							this.getComponents().get(1).exec();
							
							examAttributes = (int[]) localParameters.get("examAttributes");
							core = (Collection<Integer>) localParameters.get("core");
							
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return core;
					}, 
					(component, core) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, core);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "core={}"),
									StringUtils.numberToString(core, 50, 0)
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						int[] attributes =
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
						//	[STATISTIC_CORE_LIST]
						component.setStatistics(
								StatisticsConstants.Procedure.STATISTIC_CORE_LIST,
								core.toArray(new Integer[core.size()])
						);
						// [STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH]
						ProcedureUtils.Statistics.countInt(
								getStatistics().getData(),
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
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Core procedure controller"),
			// 2 Core
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
								getParameters().get("decClasses"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsCore = false;
						/* ------------------------------------------------------------------------------ */
						int p=0;
						List<Instance> instances =
								(List<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						ClassicHashMapCalculation<Sig> calculation =
								(ClassicHashMapCalculation<Sig>) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						int[] examAttributes =
								(int[]) parameters[p++];
						int i =
								(int) parameters[p++];
						Collection<Integer> core =
								(Collection<Integer>) parameters[p++];
						Map<Integer, Collection<Instance>> decClasses =
								(Map<Integer, Collection<Instance>>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Sig redSig = calculation.calculate(instances, new IntegerArrayIterator(examAttributes), decClasses)
												.getResult(); 
						if (calculation.value1IsBetter(globalSig, redSig, sigDeviation)) {
							core.add(attributes[i]);
							
							TimerUtils.timePause((TimeCounted) component);
							attributeIsCore = true;
							TimerUtils.timeContinue((TimeCounted) component);
						}
						if (i!=attributes.length-1)	examAttributes[i] = attributes[i];						
						return new Object[] {
							core,
							examAttributes, 
							attributeIsCore,
							attributes[i],
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> core = (Collection<Integer>) result[r++];
						int[] examAttributes = (int[]) result[r++];
						boolean attributeIsCore = (boolean) result[r++];
						int attributes_i = (int) result[r++];
						/* ------------------------------------------------------------------------------ */
						localParameters.put("core", core);
						localParameters.put("examAttributes", examAttributes);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = "Loop["+((int) localParameters.get("i") +1)+"] "+
											"Attr["+attributes_i+"]";
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								((Collection<?>) result[0]).size()
						);
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime(reportMark, (TimeCountedProcedureComponent<?>) component);
						//	[REPORT_CORE_CURRENT_ATTRIBUTE]
						ProcedureUtils.Report.saveItem(
								report, reportMark, 
								ReportConstants.Procedure.REPORT_CORE_CURRENT_ATTRIBUTE,
								attributes_i
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

	
	/**
	 * Save executed time of {@link ProcedureComponent} at the current round.
	 * 
	 * @see ProcedureUtils.Report.ExecutionTime#save(Map, String, long...).
	 * 
	 * @param component
	 * 		{@link TimeCountedProcedureComponent} to be saved.
	 */
	private void saveReportExecutedTime(String reportMark, TimeCountedProcedureComponent<?> component) {
		@SuppressWarnings("unchecked")
		Map<String, Long> executedTime = (Map<String, Long>) localParameters.get("executedTime");
		if (executedTime==null)	localParameters.put("executedTime", executedTime = new HashMap<>());
		Long  historyTime = executedTime.get(component.getDescription());
		if (historyTime==null)	historyTime = 0L;
		ProcedureUtils.Report.ExecutionTime.save(report, reportMark, component.getTime() - historyTime);
		executedTime.put(component.getDescription(), component.getTime());
	}
}