package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching for <strong>Quick Reduct - Rough Equivalence Class based
 * extension: Simple Counting(Real-time)</strong> Feature Selection. This procedure contains 3
 * {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Check if sig(red)==sig(C), if does, break : true-continue, false-break</strong>
 * 		<p>Check if significance of current reduct equals to the global significance(i.e.
 * 			<code>sig(red)==sig(C)</code>). Return <code>true</code> if it does.
 * 	</li>
 * 	<li>
 * 		<strong>Seek the most significant current attribute</strong>
 * 		<p>Search for the attribute with the max. significance value in the rest of the
 * 			attributes(i.e. attributes outside of the reduct), and return as an attribute
 * 			of the reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Update reduct and significance</strong>
 * 		<p>Add the most significant attribute into the reduct and update current reduct's
 * 			significance value.
 * 	</li>
 * </ul>
 * <p>
 * Loop is controlled by {@link #exec()}.
 *
 * @param <Sig>
 *     Type of feature (subset) significance.
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
	
	public SignificantAttributeSeekingLoopProcedureContainer(
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
			// 1. Check if break loop : true-continue, false-break.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}"),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("globalSig"),
								getParameters().get("redSig"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Sig globalSig =
								(Sig) parameters[p++];
						Sig redSig =
								(Sig) parameters[p++];
						FeatureImportance<Sig> calculation =
								(FeatureImportance<Sig>) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.value1IsBetter(globalSig, redSig, sigDeviation);
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(2, "globalSig = {} | redSig = {}"),
									String.format((getParameters().get("globalSig") instanceof Double?"%.20f":"%d"), ((Number) getParameters().get("globalSig"))),
									String.format((getParameters().get("redSig") instanceof Double?"%.20f":"%d"), ((Number) getParameters().get("redSig")))
							);
							if (result==null || !result.booleanValue()) {
								log.info(LoggerUtil.spaceFormat(2, "break!"));
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_SIG_HISTORY]
						List<Sig> posHistory =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (posHistory==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									posHistory = new LinkedList<>()
							);
						}
						posHistory.add(getParameters().get("redSig"));
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if break loop"),
			// 2. Seek the current most significant attribute.
			new TimeCountedProcedureComponent<Number[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Initiate
						int sigAttr = -1;
						Sig maxSig = null, subSig;
						// Go through examined attributes
						int[] examAttr = new int[red.size()+1];	
						int r=0;	for (int v : red)	examAttr[r++] = v;
						for (int i=0; i<attributes.length; i++) {
							if (red.contains(attributes[i]))	continue;
							// get incremental dividings.
							examAttr[examAttr.length-1] = attributes[i];
							subSig = calculation.calculate(equClasses, new IntegerArrayIterator(examAttr), instances.size())
												.getResult();
							if (calculation.value1IsBetter(subSig, maxSig, sigDeviation) || sigAttr==-1) {
								// If x(|type 1|) > maxPos, a* = a or haven't found a sig
								maxSig = subSig;
								sigAttr = attributes[i];
							}
						}
						// Return signifivant package
						return new Number[] {sigAttr, maxSig};
					}, 
					(component, sig) -> {
						/* ------------------------------------------------------------------------------ */
						int sigAttr = sig[0].intValue();
						Sig maxSig = (Sig) sig[1];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("sig", sig);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(2, "+ attribute = {} | significance = {}"),
									sigAttr, maxSig
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
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
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY,
								sigAttr
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek the current most significant attribute"),
			// 3. Update reduct and significance.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}"),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("sig"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Number[] sig =
								(Number[]) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Add the most significant attribute into reduct.
						reduct.add(sig[0].intValue());
						return new Object[] {
								reduct,
								sig[1]
						};
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> red = (Collection<Integer>) result[r++];
						Sig redSig = (Sig) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						getParameters().setNonRoot("redSig", redSig);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "sig = {}"),
									redSig
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_SIG_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark, 
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								redSig
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update reduct and significance"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		localParameters.put("lastUniverseSize", getParameters().get("lastUniverseSize"));
		localParameters.put("lastEquClassSize", getParameters().get("lastEquClassSize"));
		
		if (Integer.compare(
			((Collection<Integer>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size(), 
			((int[]) getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES)).length
			)!=0
		) {
			ProcedureComponent<?>[] comps = initComponents();
			for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
			
			SigLoop:
			while (true) {
				loopCount++;
				for (int i=0; i<comps.length; i++) {
					if (i==0) {
						if (!(Boolean) comps[i].exec())	break SigLoop;
					}else {
						comps[i].exec();
					}
				}
			}
			
			// Statistics
			//	[Sig loop times]
			statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES, loopCount);
		}
		return this.getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
	}

	public String reportMark() {
		return "Loop["+loopCount+"]";
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}