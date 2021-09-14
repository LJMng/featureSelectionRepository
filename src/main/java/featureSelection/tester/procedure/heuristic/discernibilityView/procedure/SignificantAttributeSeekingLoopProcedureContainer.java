package featureSelection.tester.procedure.heuristic.discernibilityView.procedure;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.algorithm.alg.discernibilityView.TengDiscernibilityViewAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4TengDiscernibilityView;
import featureSelection.repository.support.shrink.discernibilityView.Shrink4TengDiscernibilityView;
import featureSelection.repository.support.shrink.discernibilityView.ShrinkInput4TengDiscernibilityView;
import featureSelection.repository.support.shrink.discernibilityView.ShrinkRemovableCriteria4TengDiscernibilityView;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Significant attributes seeking loops for <strong>Forward Attribute Reduction from the
 * Discernibility View (FAR-DV)</strong>.
 * <p>
 * Original paper:
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0020025515005605">"Efficient
 * attribute reduction from the viewpoint of discernibility"</a> by Shu-Hua Teng,
 * Min Lu, A-Feng Yang, Jun Zhang, Yongjian Nian, Mi He.
 * <p>
 * This procedure contains 6 {@link ProcedureComponent}:
 * <ul>
 * 	<li>
 * 		<strong>Loop controller</strong>
 * 		<p>Control loop to seek significant attributes for rounds.
 * 	</li>
 * 	<li>
 * 		<strong>Compute DIS(D/red)</strong>
 * 		<p>Compute the Relative Discernibility Degree of D related to the updated
 * 			reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Check if |DIS(D/red)| = |DIS(D/C)|, exit if it does.</strong>
 * 		<p>Check if |DIS(D/red)| = |DIS(D/C)|, if it does, a reduct is found and break
 * 			loop. Otherwise, continue to seek.
 * 	</li>
 * 	<li>
 * 		<strong>Check for feature redundancy</strong>
 * 		<p>Check redundancy of a feature by SIG<sup>outer</sup>:
 * 			SIG<sup>outer</sup>(a[t], red, D, U[j]) =
 * 			|DIS(D/red)| - |DIS(D/red∪{a[t]})|.
 * 	</li>
 * 	<li>
 * 		<strong>Search for the most significant attribute</strong>
 * 		<p>Search for the attribute with the max. SIG<sup>outer</sup> value. If
 * 			multiple attributes' SIG<sup>outer</sup> are maximum and the same, select
 * 			the one with the min. |DIS(a)|.
 * 	</li>
 * 	<li>
 *	 	<strong>Universe streamline</strong>
 * 		<p>Streamline universe instances by removing dispensable ones.
 * 	</li>
 * </ul>
 * 
 * @see Shrink4TengDiscernibilityView#removAble(ShrinkRemovableCriteria4TengDiscernibilityView)
 * @see TengDiscernibilityViewAlgorithm#mostSignificantAttribute(int, int[], Collection,
 * 		Collection, Number, FeatureImportance4TengDiscernibilityView)
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
	private List<String> reportKeys;
	
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
			// 1. Loop controller
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						Collection<Instance> instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						localParameters.put("insSize", instances.size());
						
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("removedAttributes"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes =
								(int[]) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> removedAttributes =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						ProcedureComponent<Boolean> comp2 = (ProcedureComponent<Boolean>) getComponents().get(2);
						ProcedureComponent<Boolean> comp3 = (ProcedureComponent<Boolean>) getComponents().get(3);
						ProcedureComponent<?> comp4 = getComponents().get(4);
						ProcedureComponent<?> comp5 = getComponents().get(5);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						do {
							TimerUtils.timePause((TimeCounted) component);
							
							loopCount++;
							
							// Step 3: Loop over a[t] in A[j]
							//			compute SIG<sup>outer</sup>(a[t], red, D, U<sup>j</sup>)
							//			if SIG<sup>outer</sup> = 0
							//				A' = A' U {a[t]}
							//	Compute DIS(D/red).
							comp1.exec();
							// Exit for |DIS(D/red)| = |DIS(D/C)|
							if (comp2.exec()) {
								TimerUtils.timeContinue((TimeCounted) component);
								break;
							}

							TimerUtils.timeContinue((TimeCounted) component);
							
							// Check for redundant attributes.
							for (int attr: attributes) {
								// a[t] in A' (i.e. not in A[j]), skip
								if (removedAttributes.contains(attr))	continue;
								
								TimerUtils.timePause((TimeCounted) component);
								localParameters.put("attr", attr);
								if (comp3.exec())	removedAttributes.add(attr);
								
								TimerUtils.timeContinue((TimeCounted) component);
							}

							TimerUtils.timePause((TimeCounted) component);
							// Report
							//	[current attribute size]
							ProcedureUtils.Report.saveItem(
									getReport(), reportMark(), 
									"attributes 4 search", 
									attributes.length-removedAttributes.size()
							);
							
							// Step 4. Search for the most significant attribute.
							comp4.exec();
							// Step 5. Streamline universe.
							comp5.exec();
							
							TimerUtils.timeContinue((TimeCounted) component);
							// next round
						} while(true);
						/* ------------------------------------------------------------------------------ */
						return reduct;
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[Sig loop times]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES,
								loopCount
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(getReport(), (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Loop controller"),
			// 2. Compute DIS(D/red)
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}"), loopCount,
									getComponents().size(), component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("redEquClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						@SuppressWarnings("unused")
						Collection<Collection<Instance>> redEquClasses =
								(Collection<Collection<Instance>>) parameters[p++];
						FeatureImportance4TengDiscernibilityView<Sig> calculation =
								(FeatureImportance4TengDiscernibilityView<Sig>) parameters[p++];
						Collection<Instance> instances = (Collection<Instance>) parameters[p++];
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						//	Compute DIS(D/red).
						//		(U/red)
						Collection<Collection<Instance>> redEquClassesUsingAllInstances =
								TengDiscernibilityViewAlgorithm
									.Basic
									.equivalenceClass(instances, new IntegerCollectionIterator(reduct))
									.values();
						//		(U/red)/D
						Collection<Collection<Instance>> gainRedEquClassesByDec =
								TengDiscernibilityViewAlgorithm
									.Basic
									.gainEquivalenceClass(
											redEquClassesUsingAllInstances,
											new IntegerArrayIterator(0)
									);
						//		DIS(D/red)
						Sig relativeDisDegreeOfDec2Red =
								calculation.calculate(
										// U/red
										redEquClassesUsingAllInstances,
										// (U/red)/D
										gainRedEquClassesByDec
								).getResult();
						return relativeDisDegreeOfDec2Red;
					}, 
					(component, relativeDisDegreeOfDec2Red)->{
						/* ------------------------------------------------------------------------------ */
						localParameters.put("relativeDisDegreeOfDec2Red", relativeDisDegreeOfDec2Red);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "DIS(D/red) = {}"),
									relativeDisDegreeOfDec2Red
							);
							
							Sig disOfDRelate2C = getParameters().get("disOfDRelate2C");
							log.info(LoggerUtil.spaceFormat(2, "DIS(D/C)   = {}"), 
									disOfDRelate2C instanceof Integer?
											disOfDRelate2C:
											String.format("%d", disOfDRelate2C)
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_SIG_HISTORY]
						Collection<Sig> sigHistory =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (sigHistory==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									sigHistory=new LinkedList<>()
							);
						}
						sigHistory.add(relativeDisDegreeOfDec2Red);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Compute DIS(D/red)"),
			// 3. Check if |DIS(D/red)| = |DIS(D/C)|, exit if it does.
			new TimeCountedProcedureComponent<Boolean>(
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
								getParameters().get("disOfDRelate2C"),
								localParameters.get("relativeDisDegreeOfDec2Red"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Sig disOfDRelate2C =
								(Sig) parameters[p++];
						Sig relativeDisDegreeOfDec2Red =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return Double.compare(
									// DIS(D/red)
									relativeDisDegreeOfDec2Red.doubleValue(), 
									// DIS(D/C)
									disOfDRelate2C.doubleValue()
								)==0;
					}, 
					(component, breakMark)->{
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							if (breakMark!=null && breakMark) {
								log.info(LoggerUtil.spaceFormat(2, "break!"));
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
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
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if |DIS(D/red)| = |DIS(D/C)|, exit if it does"),
			// 4. Check for feature redundancy.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
//						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 4/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("redEquClasses"),
								localParameters.get("relativeDisDegreeOfDec2Red"),
								localParameters.get("attr"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						FeatureImportance4TengDiscernibilityView<Sig> calculation = 
								(FeatureImportance4TengDiscernibilityView<Sig>)
										parameters[p++];
						Collection<Collection<Instance>> redEquClasses =
								(Collection<Collection<Instance>>) parameters[p++];
						
						Sig relativeDisDegreeOfDec2Red = (Sig) parameters[p++];
						int attr = (int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// SIG<sup>outer</sup>(a[t], red, D, U[j]) = |DIS(D/red)| - |DIS(D/red∪{a[i]})|
						Sig outerSig = calculation.calculateOuterSignificance(
										//	U[j]/red
										redEquClasses, 
										//	|DIS(D/red)|
										relativeDisDegreeOfDec2Red, 
										//	a[i]
										new IntegerArrayIterator(attr), 
										//	D
										new IntegerArrayIterator(0)
									).getResult();
						// 	if SIG<sup>outer</sup> = 0, A' = A' U {a[t]}
						return Double.compare(outerSig.doubleValue(), 0)==0;
					}, 
					(component, redundant)->{
						/* ------------------------------------------------------------------------------ */
						if (logOn && redundant) {
							int attr = (int) localParameters.get("attr");
							Collection<Integer> removedAttributes = getParameters().get("removedAttributes");
							log.info(LoggerUtil.spaceFormat(2, "- attr {}. {} attribute(s) have been removed in total."),
									attr, removedAttributes.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check for feature redundancy"),
			// 5. Search for the most significant attribute.
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 5/{}. {}"),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								localParameters.get("insSize"),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("removedAttributes"),
								getParameters().get("redEquClasses"),
								localParameters.get("relativeDisDegreeOfDec2Red"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						FeatureImportance4TengDiscernibilityView<Sig> calculation = 
								(FeatureImportance4TengDiscernibilityView<Sig>)
								parameters[p++];
						int insSize =
								(int) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> removedAttributes =
								(Collection<Integer>) parameters[p++];
						Collection<Collection<Instance>> redEquClasses =
								(Collection<Collection<Instance>>) parameters[p++];
						Sig relativeDisDegreeOfDec2Red =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int sigAttribute =
								TengDiscernibilityViewAlgorithm
										.mostSignificantAttribute(
												// U[j]
												insSize,
												// A
												attributes,
												// A'
												removedAttributes,
												// U[j]/red
												redEquClasses,
												// Dis(D|red)
												relativeDisDegreeOfDec2Red,
												calculation
										);
						// A' = A' U a[k].
						reduct.add(sigAttribute);
						removedAttributes.add(sigAttribute);
						return sigAttribute;
					}, 
					(component, sigAttr)->{
						/* ------------------------------------------------------------------------------ */
						localParameters.put("sigAttr", sigAttr);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							Collection<Integer> red = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
							log.info(LoggerUtil.spaceFormat(2, "+ attr {}. {} reduct attribute(s) in total"),
									sigAttr, red.size()
							);
						}
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
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Search for the most significant attribute"),
			// 6. Universe instance streamline.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 6/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE),
								getParameters().get("globalEquClasses"),
								getParameters().get("decEquClasses"),
								getParameters().get("redEquClasses"),
								localParameters.get("sigAttr"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Shrink4TengDiscernibilityView shrinkInstance =
								(Shrink4TengDiscernibilityView)
								parameters[p++];
						Collection<Collection<Instance>> globalEquClasses =
								(Collection<Collection<Instance>>)
								parameters[p++];
						Collection<Collection<Instance>> decEquClasses =
								(Collection<Collection<Instance>>)
								parameters[p++];
						Collection<Collection<Instance>> redEquClasses =
								(Collection<Collection<Instance>>)
								parameters[p++];
						int sigAttr =
								(int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						redEquClasses = 
								TengDiscernibilityViewAlgorithm
									.Basic
									.gainEquivalenceClass(
											redEquClasses,
											new IntegerArrayIterator(sigAttr)
									);
						return new Object[] {
								shrinkInstance.shrink(
									new ShrinkInput4TengDiscernibilityView(
											globalEquClasses,
											decEquClasses,
											redEquClasses
									)
								), 
								redEquClasses
						};
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						int r=0;
						int removedInsSize = (int) result[r++];
						Collection<Collection<Instance>> redEquClasses = (Collection<Collection<Instance>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("redEquClasses", redEquClasses);
						/* ------------------------------------------------------------------------------ */
						int insSize = (int) localParameters.get("insSize");
						insSize -= removedInsSize;
						localParameters.put("insSize", insSize);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "- {} Universe(s), {} remains."), 
									removedInsSize, insSize
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_UNIEVRSE_REMOVED]
						Collection<Integer> instanceRemoveHistory =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED);
						if (instanceRemoveHistory==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED,
									instanceRemoveHistory = new LinkedList<>()
							);
						}
						instanceRemoveHistory.add(removedInsSize);
						/* ------------------------------------------------------------------------------ */
						// Report
						String remark = reportMark();
						//	[REPORT_UNIVERSE_SPECIAL_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								getReport(), remark,
								ReportConstants.Procedure.REPORT_UNIVERSE_SPECIAL_REMOVE_HISTORY,
								removedInsSize
						);
						//	[REPORT_CURRENT_UNIVERSE_SIZE]
						ProcedureUtils.Report.saveItem(
								getReport(), remark,
								ReportConstants.Procedure.REPORT_CURRENT_UNIVERSE_SIZE,
								insSize
						);
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Universe instance streamline"),
		};	
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	getComponents().add(each);
		return (Collection<Integer>) comps[0].exec();
	}

	public String reportMark() {
		return "Loop["+loopCount+"]";
	}
	
	/**
	 * Save executed time of {@link Component} at the current round.
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