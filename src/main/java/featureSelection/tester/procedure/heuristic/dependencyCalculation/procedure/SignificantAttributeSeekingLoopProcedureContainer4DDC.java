package featureSelection.tester.procedure.heuristic.dependencyCalculation.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import featureSelection.repository.algorithm.alg.dependencyCalculation.DirectDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.alg.dependencyCalculation.IncrementalDependencyCalculationAlgorithm;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Significant attributes seeking loops for <strong>Direct Dependency Calculation
 * (DDC)</strong> Feature  Selection. This procedure contains 4
 * {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Calculate current red dependency</strong>
 * 		<p>Calculate current reduct's dependency value: dep(Red).
 * 	</li>
 * 	<li>
 * 		<strong>Check if dep(red)==dep(C), if does, break : true-continue,
 * 				false-break.</strong>
 * 		<p>Check if current reduct dependency equals to global dependency. If it
 * 			does, finished searching and break the loop. Otherwise, continue to seek.
 * 	</li>
 * 	<li>
 * 		<strong>Seek significant attribute</strong>
 * 		<p>Loop over attributes outside current reduct and seek for the most
 * 			significant attribute that has the max. outer significance.
 * 	</li>
 * 	<li>
 * 		<strong>Add significant attribute into red</strong>
 * 		<p>Add the most significant attribute into reduct.
 * 	</li>
 * </ul>
 * <p>
 * Loop is controlled by {@link #exec()}.
 * 
 * @author Benjamin_L
 */
@Slf4j
public class SignificantAttributeSeekingLoopProcedureContainer4DDC<Sig extends Number>
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
	
	public SignificantAttributeSeekingLoopProcedureContainer4DDC(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "QR-IDC - Sig seeking loop";
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
			// 1. Calculate current red dependency.
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> universes = (Collection<Instance>) parameters[p++];
						Collection<Integer> red = (Collection<Integer>) parameters[p++];
						FeatureImportance4DirectDependencyCalculation<Sig> calculation = 
								(FeatureImportance4DirectDependencyCalculation<Sig>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.calculate(universes, new IntegerCollectionIterator(red))
											.getResult();
					}, 
					(component, redDependency)->{
						/* ------------------------------------------------------------------------------ */
						localParameters.put("redDependency", redDependency);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_SIG_HISTORY]
						Collection<Sig> sigHistory =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (sigHistory==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									sigHistory = new LinkedList<>()
							);
						}
						sigHistory.add(redDependency);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						Collection<Integer> red = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate current red dependency"),
			// 2. Check if dep(red)==dep(C), if does, break : true-continue, false-break.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}"), loopCount, getComponents().size(), component.getDescription());
							log.info(LoggerUtil.spaceFormat(2, "deviation = {}"), getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION).toString());
							log.info(LoggerUtil.spaceFormat(2, "dep(C) = {}"), getParameters().get("globalDependency").toString());
							log.info(LoggerUtil.spaceFormat(2, "dep(red) = {}"), localParameters.get("redDependency").toString());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("globalDependency"),
								localParameters.get("redDependency"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Sig globalSig = (Sig) parameters[p++];
						Sig redSig = (Sig) parameters[p++];
						FeatureImportance4DirectDependencyCalculation<Sig> calculation = 
								(FeatureImportance4DirectDependencyCalculation<Sig>) 
								parameters[p++];
						Sig sigDeviation = (Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return !IncrementalDependencyCalculationAlgorithm
								.continueLoopQuickReduct(
									calculation, 
									sigDeviation,
									globalSig, 
									redSig
								);
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if dep(red)==dep(C), if does, break"),
			// 3. Seek significant attribute.
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								localParameters.get("redDependency"),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						Sig redDependency =
								(Sig) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						FeatureImportance4DirectDependencyCalculation<Sig> calculation =
								(FeatureImportance4DirectDependencyCalculation<Sig>) 
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int sig = DirectDependencyCalculationAlgorithm
									.mostSignificantAttribute(
											calculation,
											sigDeviation,
											instances,
											red,
											redDependency,
											attributes
									);
						if (sig==-1)
							throw new IllegalStateException("abnormal most significant attribute result : "+sig);
						return sig;
					}, 
					(component, sig)->{
						/* ------------------------------------------------------------------------------ */
						localParameters.put("sig", sig);
						/* ------------------------------------------------------------------------------ */
						// Statistics
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
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
						//	[REPORT_SIG_HISTORY]
						ProcedureUtils.Report.saveItem(report, reportMark, ReportConstants.Procedure.REPORT_SIG_HISTORY, sig);
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek significant attribute"),
			// 4. Add significant attribute into red.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 4/{}. {}"),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								localParameters.get("sig"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
						});
					}, 
					(component, parameters) -> {
						int sig = (int) parameters[0];
						Collection<Integer> red = (Collection<Integer>) parameters[1];
						red.add(sig);
						return red;
					}, 
					(component, red)->{
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|red| = {}"),
									red.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Add significant attribute into red"),
		};	
	}

	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
		
		SigLoop:
		while (true){
			loopCount++;
			for (int i=0; i<comps.length; i++) {
				if (i==1) {
					if ( ! (Boolean) comps[i].exec())	break SigLoop;
				}else {
					comps[i].exec();
				}
			}
		}
		// Statistics
		//	[Sig loop times]
		statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES, loopCount);
		return this.getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
	}
	
	public String reportMark() {
		return "Loop["+loopCount+"]";
	}
	
	/**
	 * Save executed time of {@link ProcedureComponent} at the current round.
	 * 
	 * @see ProcedureUtils.Report.ExecutionTime#save(Map, String, long...).
	 * 
	 * @param component
	 * 		{@link TimeCountedProcedureComponent} to be saved.
	 */
	private void saveReportExecutedTime(TimeCountedProcedureComponent<?> component) {
		@SuppressWarnings("unchecked")
		Map<String, Long> executedTime = (Map<String, Long>) localParameters.get("executedTime");
		if (executedTime==null)	localParameters.put("executedTime", executedTime = new HashMap<>());
		Long  historyTime = executedTime.get(component.getDescription());
		if (historyTime==null)	historyTime = 0L;
		ProcedureUtils.Report.ExecutionTime.save(report, reportMark(), component.getTime() - historyTime);
		executedTime.put(component.getDescription(), component.getTime());
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}