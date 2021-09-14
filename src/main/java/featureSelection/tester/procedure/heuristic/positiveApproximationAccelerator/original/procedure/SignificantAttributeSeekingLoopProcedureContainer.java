package featureSelection.tester.procedure.heuristic.positiveApproximationAccelerator.original.procedure;

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
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalUtils;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.MostSignificantAttributeResult;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.repository.support.shrink.positiveApproximationAccelerator.original.Shrink4PositiveApproximationAccelerator;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching for <strong>Positive Approximation Accelerator (ACC)</strong>
 * Feature Selection.
 * <p>
 * This procedure contains 5 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Check if sig(red)==sig(C), if does, break : true-continue, false-break</strong>
 * 		<p>Check if significance of current reduct equals to the global significance(i.e.
 * 			<code>sig(red)==sig(C)</code>). Return <code>true</code> if it does.
 * 	</li>
 * 	<li>
 * 		<strong>filter positive/negative regions</strong>
 * 		<p>Filter positive regions and negative regions to streamline
 * 			{@link Instance}s by {@link Shrink4PositiveApproximationAccelerator#
 * 			streamline(Collection)}.
 * 	</li>
 * 	<li>
 * 		<strong>Seek sig attribute</strong>
 * 		<p>Search for the attribute with the max. significance value in the rest of the
 * 			attributes(i.e. attributes outside the reduct), and return as an attribute
 * 			of the reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Add sig attribute into red</strong>
 * 		<p>Add the most significant attribute into the reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Update equivalence class</strong>
 * 		<p>Use the reduct to partition {@link Instance} {@link Collection} for update.
 * 	</li>
 * </ul>
 * <p>
 * Loop is controlled by {@link #exec()}.
 * 
 * @see Shrink4PositiveApproximationAccelerator
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
			// 1. Check if break the loop: true-break / false-continue.
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
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
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
						Sig sigDeviation =
								(Sig) parameters[p++];
						FeatureImportance<Sig> calculation =
								(FeatureImportance<Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// true-break / false-continue
						return !calculation.value1IsBetter(globalSig, redSig, sigDeviation);
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(2, "globalSig = {} | redSig = {}"),
									getParameters().get("globalSig"),
									getParameters().get("redSig")
							);
							log.info(
									LoggerUtil.spaceFormat(2, "deviation = {}"),
									getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION).toString()
							);
						}
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
				}.setDescription("Check if break the loop."),
			// 2. Filter positive/negative regions.
			new TimeCountedProcedureComponent<Object[]>(
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
						
						Collection<EquivalenceClass> equClasses = getParameters().get("equClasses");
						localParameters.put("equClassesSize", equClasses.size());
						
						component.setLocalParameters(new Object[] {
								equClasses,
								getParameters().get(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						Shrink4PositiveApproximationAccelerator shrinkInstance =
								(Shrink4PositiveApproximationAccelerator) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						int[] removeInfo = shrinkInstance.shrink(equClasses);
						return new Object[] {
								equClasses,
								removeInfo[0],
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<EquivalenceClass> equClasses = (Collection<EquivalenceClass>) result[r++];
						int removeIns = (int) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("equClasses", equClasses);
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_UNIEVRSE_REMOVED]
						List<Integer> removedUniverse =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED);
						if (removedUniverse==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED,
									removedUniverse = new LinkedList<>());
						}
						removedUniverse.add(removeIns);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						Collection<Integer> red = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						//	[DatasetRealTimeInfo]
						int currentUniverseSize =
								PositiveApproximationAcceleratorOriginalUtils
										.universeSize(equClasses);
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								currentUniverseSize, 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_UNIVERSE_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_UNIVERSE_POS_REMOVE_HISTORY,
								removeIns
						);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "- {} instance(s)"),
									removeIns
							);
						}
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Filter positive/negative regions"),
			// 3. Seek sig attribute.
			new TimeCountedProcedureComponent<MostSignificantAttributeResult<Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						PositiveApproximationAcceleratorCalculation<Sig> calculation =
								(PositiveApproximationAcceleratorCalculation<Sig>) 
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return PositiveApproximationAcceleratorOriginalAlgorithm
									.mostSignificantAttribute(
											equClasses, 
											red, 
											attributes, 
											instances.size(),
											calculation, 
											sigDeviation
									);
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						MostSignificantAttributeResult<Sig> sig = result;
						getParameters().setNonRoot("sig", sig);
						getParameters().setNonRoot("redSig", sig.getMaxSig());
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "+ attribute {} | pos = {}"), 
									sig.getAttribute(), 
									sig.getMaxSig()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_POS_HISTORY]
						List<Sig> increment =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (increment==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									increment = new LinkedList<>()
							);
						}
						increment.add(sig.getMaxSig());
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
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_POSITIVE_INCREMENT_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark, 
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								sig.getMaxSig()
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
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek sig attribute"),
			// 4. Add sig attribute into red.
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
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
									getParameters().get("sig"),
							});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> red = (Collection<Integer>) parameters[0];
						MostSignificantAttributeResult<Sig> sig = (MostSignificantAttributeResult<Sig>) parameters[1];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (sig.getAttribute()==-1)	
							throw new IllegalStateException("Abnormal most significant attribute result : "+sig.getAttribute());
						else			
							red.add(sig.getAttribute());
						return red;
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark(),
								(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Add sig attribute into red"),
			// 5. Update equivalence class.
			new TimeCountedProcedureComponent<Collection<EquivalenceClass>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 5/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("sig"),
							});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						MostSignificantAttributeResult<Sig> sig = (MostSignificantAttributeResult<Sig>) parameters[0];
						/* ------------------------------------------------------------------------------ */
						return sig.getEquClasses();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("equClasses", result);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark(),
								(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}	
				
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update equivalence class"),
		};
	}

	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps) {
			this.getComponents().add(each);
			//reportKeys.add(each.getDescription());
		}
		ComponentLoop:
		while (true) {
			loopCount++;
			for (int i=0; i<this.getComponents().size(); i++) {
				if (i==0) {
					if (((Boolean) comps[i].exec()))	break ComponentLoop;
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

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
	
	private String reportMark() {
		return "Loop["+loopCount+"]";
	}
}