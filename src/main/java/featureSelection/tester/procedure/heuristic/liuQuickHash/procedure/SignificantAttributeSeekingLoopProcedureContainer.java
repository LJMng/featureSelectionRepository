package featureSelection.tester.procedure.heuristic.liuQuickHash.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.quickHash.LiuQuickHashAlgorithm;
import featureSelection.repository.entity.alg.liuQuickHash.RoughEquivalenceClass;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Significant attributes seeking loops for <strong>Liang incremental entropy Calculation
 * </strong> based Incremental Feature Selection. This is a {@link DefaultProcedureContainer}.
 * Procedure contains 6 {@link ProcedureComponent}s, refer to 2 steps:
 * <ul>
 * 	<li><strong>Seek sig attribute</strong>:
 * 		<p>Search for the attribute with the most significance in attributes outside of
 * 			reduct.
 * 	</li>
 * 	<li><strong>Add sig attribute into red</strong>:
 * 		<p>Add the most significant attribute into reduct.
 * 	</li>
 * </ul>
 */
@Slf4j
public class SignificantAttributeSeekingLoopProcedureContainer
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
			// 1. Seek sig attribute.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get("roughClasses"),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Collection<RoughEquivalenceClass> roughClasses =
								(Collection<RoughEquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int sigValue;
						int  redSig = -1, sigAttr = -1;
						Collection<RoughEquivalenceClass> incRoughClasses, sigRough = null;
						for (int attr : attributes) {
							if (reduct.contains(attr))	continue;
							// Calculate the significance of the attribute
							incRoughClasses =
									LiuQuickHashAlgorithm.incrementalRoughEquivalenceClass(
											roughClasses,
											new IntegerArrayIterator(attr)
									);

							//sigValue = LiuQuickHashAlgorithm.positiveRegion(incRoughClasses);
							sigValue = LiuQuickHashAlgorithm.negativeRegion(incRoughClasses);
							
							if (redSig==-1 || redSig > sigValue) {
								redSig = sigValue;
								sigAttr = attr;
								sigRough = incRoughClasses;
							}
						}
						return new Object[] {
								redSig,
								sigAttr,
								sigRough
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0; 
						int redSig = (int) result[r++];
						int sigAttr = (int) result[r++];
						Collection<RoughEquivalenceClass> incRoughClasses = (Collection<RoughEquivalenceClass>) result[r++];
						/* ------------------------------------------------------------------------------ */
						localParameters.put("redSig", redSig);
						localParameters.put("sigAttr", sigAttr);
						getParameters().set(false, "roughClasses", incRoughClasses);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(2, "+ attribute {}, sig = {}"),
									sigAttr, redSig
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH,
							1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
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
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek sig attribute"),
			// 2. Add sig attribute into red.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}"),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
									localParameters.get("sigAttr"),
							});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						int sigAttr =
								(int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (sigAttr==-1)	
							throw new IllegalStateException("abnormal most significant attribute result : "+sigAttr);
						else			
							reduct.add(sigAttr);
						return reduct;
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
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
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Add sig attribute into red"),
		};
	}

	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
		
		int redSig, unconsGP = getParameters().get("unconsGP");
		
		do {
			loopCount++;
			for (int i=0; i<this.getComponents().size(); i++)
				comps[i].exec();
			redSig = (int) localParameters.get("redSig");
		//} while (redSig!=positiveRegion.size());
		} while (redSig!=unconsGP);
		// Statistics
		//	[Sig loop times]
		statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES, loopCount);
		return getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
	
	private String reportMark() {
		return "Loop["+loopCount+"]";
	}
}