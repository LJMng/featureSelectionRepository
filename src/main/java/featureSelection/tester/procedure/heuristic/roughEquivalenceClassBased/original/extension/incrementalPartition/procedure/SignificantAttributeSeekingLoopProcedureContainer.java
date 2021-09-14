package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
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
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.SignificantAttributeClassPack;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.StatisticResult;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalPartition.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching for <strong>Quick Reduct - Rough Equivalent Class based
 * extension: Incremental Partition</strong> Feature Selection. This procedure contains 3
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
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SHRINK_INSTANCE_INSTANCE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * 	<li>equClasses</li>
 * 	<li>roughClasses</li>
 * 	<li>sigPack</li>
 * </ul>
 * <p>
 * Loop is controlled by {@link #exec()}.
 *
 * @author Benjamin_L
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
	
	public SignificantAttributeSeekingLoopProcedureContainer(ProcedureParameters parameters, boolean logOn) {
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
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Check if sig(red)==sig(C), if does, break : true-continue, false-break.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("roughClasses"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Collection<EquivalenceClass>> roughClasses =
								(Collection<Collection<EquivalenceClass>>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return !roughClasses.isEmpty();
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							if (result==null || !result.booleanValue()) {
								log.info(LoggerUtil.spaceFormat(2, "break!"));
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
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
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}."), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("roughClasses"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE),
						});

						if (localParameters.get("lastUniverseSize")==null || 
							localParameters.get("lastEquClassSize")==null
						) {
							Collection<RoughEquivalenceClassDummy> roughClasses = getParameters().get("roughClasses");
							int instanceSize = 0, equClassSize = 0;
							for (RoughEquivalenceClassDummy roughClass: roughClasses) {
								equClassSize+=roughClass.getItemSize();
								instanceSize+=roughClass.getInstanceSize();
							}
							localParameters.put("lastUniverseSize", instanceSize);
							localParameters.put("lastEquClassSize", equClassSize);
						}
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int[] remove;
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<RoughEquivalenceClassDummy> roughClasses =
								(Collection<RoughEquivalenceClassDummy>) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Shrink4RECBoundaryClassSetStays shrinkInstance =
								(Shrink4RECBoundaryClassSetStays)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int maxPos=0, sigAttr=-1;
						// Loop over attributes outside of reduct.
						StatisticResult<Collection<RoughEquivalenceClassDummy>> statisticResult;
						Collection<RoughEquivalenceClassDummy> maxSigRoughClasses = null;
						for (int i=0; i<attributes.length; i++) {
							if (reduct.contains(attributes[i]))	continue;
							// Calculate the significance of reduct ∪ {a}
							statisticResult =
									RoughEquivalenceClassBasedExtensionAlgorithm
											.IncrementalPartition
											.Basic
											.calculateRoughEquivalenceClassPosPartition(
													roughClasses, attributes[i]
											);
							// Update if needed.
							if (sigAttr==-1 || statisticResult.getPositiveRegion()>maxPos) {
								sigAttr = attributes[i];
								maxSigRoughClasses = statisticResult.getRecord();
								maxPos = statisticResult.getPositiveRegion();
							}
						}
						remove = shrinkInstance.shrink(maxSigRoughClasses);
						return new Object[] {
							new SignificantAttributeClassPack<Integer>(sigAttr, maxSigRoughClasses),
							maxPos,
							remove
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						SignificantAttributeClassPack<Integer> sigPack = (SignificantAttributeClassPack<Integer>) result[r++];
						Integer maxPos = (Integer) result[r++];
						int[] remove = (int[]) result[r++];
						/* ------------------------------------------------------------------------------ */
						Collection<RoughEquivalenceClassDummy> roughClasses = sigPack.getRoughClasses();
						int removePosU = remove[0];
						int removeNegU = remove[1];
						int removePosEqu = remove[2];
						int removeNegEqu = remove[3];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("sigPack", sigPack);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "+ attribute = {}, pos = {}"), 
									sigPack.getSigAttribute(),
									maxPos==null? 0: maxPos.intValue()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						int lastInstanceSize = (int) localParameters.get("lastUniverseSize");
						int lastEquClassSize = (int) localParameters.get("lastEquClassSize");
						int currentInstanceSize = 0, currentEquClassSize = 0;
						for (RoughEquivalenceClassDummy roughClass: roughClasses) {
							currentEquClassSize+=roughClass.getItemSize();
							currentInstanceSize+=roughClass.getInstanceSize();
						}
						localParameters.put("lastUniverseSize", currentInstanceSize);
						localParameters.put("lastEquClassSize", currentEquClassSize);
						
						if (removePosU+removeNegU != lastInstanceSize-currentInstanceSize)
							throw new RuntimeException("Statistics error: removePosU+removeNegU != lastUniverseSize-currentUniverseSize");
						if (removePosEqu+removeNegEqu != lastEquClassSize-currentEquClassSize)
							throw new RuntimeException("Statistics error: removePosEqu+removeNegEqu != lastEquClassSize-currentEquClassSize");
						
						//	[STATISTIC_SIG_HISTORY]
						if (maxPos!=null) {
							List<Integer> posHistory = (List<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
							if (posHistory==null)	statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY, posHistory = new LinkedList<>());
							posHistory.add(maxPos);
						}
						//	[STATISTIC_POS_COMPACTED_UNIVERSE_REMOVED]
						List<Integer> instanceRemoved = (List<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_COMPACTED_INSTANCE_REMOVED);
						if (instanceRemoved==null) {
							statistics.put(StatisticsConstants.Procedure.STATISTIC_POS_COMPACTED_INSTANCE_REMOVED,
											instanceRemoved = new LinkedList<>()
										);
						}
						instanceRemoved.add(removePosEqu);
						//	[STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED]
						instanceRemoved = (List<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED);
						if (instanceRemoved==null) {
							statistics.put(StatisticsConstants.Procedure.STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED,
											instanceRemoved = new LinkedList<>()
										);
						}
						instanceRemoved.add(removeNegEqu);
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH, 1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								currentInstanceSize,
								currentEquClassSize, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY,
								sigPack.getSigAttribute()
						);
						//	[REPORT_UNIVERSE_POS_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_UNIVERSE_POS_REMOVE_HISTORY,
								removePosU
						);
						//	[REPORT_COMPACTED_UNIVERSE_POS_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_COMPACTED_UNIVERSE_POS_REMOVE_HISTORY,
								removePosEqu
						);

						//	[REPORT_UNIVERSE_NEG_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_UNIVERSE_NEG_REMOVE_HISTORY,
								removeNegU
						);
						//	[REPORT_COMPACTED_UNIVERSE_NEG_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_COMPACTED_UNIVERSE_NEG_REMOVE_HISTORY,
								removeNegEqu
						);
						//	[REPORT_SIG_HISTORY]
						if (maxPos!=null) {
							ProcedureUtils.Report.saveItem(
									report, reportMark,
									ReportConstants.Procedure.REPORT_SIG_HISTORY,
									maxPos
							);
						}
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "- {} universe(s)"), lastInstanceSize - currentInstanceSize);
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
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("sigPack"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						SignificantAttributeClassPack<Integer> sigPack =
								(SignificantAttributeClassPack<Integer>) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						reduct.add(sigPack.getSigAttribute());
						return new Object[] {
								reduct,
								sigPack.getRoughClasses(),
						};
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> red = (Collection<Integer>) result[r++];
						Collection<Collection<EquivalenceClass>> roughClasses = (Collection<Collection<EquivalenceClass>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|Reduct| = {}"), red.size());
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						getParameters().setNonRoot("roughClasses", roughClasses);
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
					} else {
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