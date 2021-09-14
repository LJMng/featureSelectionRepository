package featureSelection.tester.procedure.heuristic.discernibilityView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
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
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.TengDiscernibilityViewStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.shrink.ShrinkInstance;
import featureSelection.repository.algorithm.alg.discernibilityView.TengDiscernibilityViewAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4TengDiscernibilityView;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.discernibilityView.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Forward Attribute Reduction from the Discernibility
 * View(FAR-DV)</strong>.
 * <p>
 * Original paper:
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0020025515005605">"Efficient
 * attribute reduction from the viewpoint of discernibility"</a> by Shu-Hua Teng,
 * Min Lu, A-Feng Yang, Jun Zhang, Yongjian Nian, Mi He.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 3
 * {@link ProcedureComponent}s, refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Compute Discernibility Degree of DIS(D/C)</strong>:
 * 		<p>Compute the Relative Discernibility Degree of <i>D</i> relate to <i>C</i> where <i>D</i>
 * 	    	is the decision attribute of {@link Instance} and <i>C</i> is all conditional
 * 			attributes of {@link Instance}s. Refer to step 1 in the original paper.
 * 	</li>
 * 	<li>
 * 		<strong>Initializations</strong>:
 * 		<p>Prepare for executions below, referring to step 2 in the original
 * 			paper and some initializations for step 3 and step 5.
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>:
 * 		<p>Loop and search for the most significant attribute as an attribute of the
 * 			final reduct until reaching the exit criteria and return the reduct.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SHRINK_INSTANCE_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_QR_EXEC_CORE}</li>
 * </ul>
 *
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 *
 * @see SignificantAttributeSeekingLoopProcedureContainer
 *
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class TengDiscernibilityViewHeuristicQRTester<Sig extends Number>
		extends DefaultProcedureContainer<Collection<Integer>>
		implements TimeSum,
					StatisticsCalculated,
					ReportMapGenerated<String, Map<String, Object>>,
					TengDiscernibilityViewStrategy,
					QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public TengDiscernibilityViewHeuristicQRTester(
			ProcedureParameters paramaters, boolean logOn
	) {
		super(logOn? log: null, paramaters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}
	
	@Override
	public String shortName() {
		return "QR-Discernibility View"+
				"("+ ProcedureUtils.ShortName.calculation(getParameters())+")";
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
			// 1. Compute Discernibility Degree of DIS(D/C)
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("1. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get(ParameterConstants.PARAMETER_SHRINK_INSTANCE_CLASS),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Class<? extends FeatureImportance4TengDiscernibilityView<Sig>> calculationClass =
								(Class<? extends FeatureImportance4TengDiscernibilityView<Sig>>) 
								parameters[p++];
						Class<? extends ShrinkInstance<?, ?, ?>> streamlineClass =
								(Class<? extends ShrinkInstance<?, ?, ?>>)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						FeatureImportance4TengDiscernibilityView<Sig> calculation = calculationClass.newInstance();
						//	U/C
						Collection<Collection<Instance>> globalEquClasses =
								TengDiscernibilityViewAlgorithm
									.Basic
									.equivalenceClass(
											instances,
											new IntegerArrayIterator(attributes)
									).values();
						//	(U/C)/D
						Collection<Collection<Instance>> gainGlobalEquClasses =
								TengDiscernibilityViewAlgorithm
									.Basic
									.gainEquivalenceClass(
											globalEquClasses,
											new IntegerArrayIterator(0)
									);
						//	|DIS(D/C)|
						Sig disOfDRelate2C = calculation.calculate(globalEquClasses, gainGlobalEquClasses)
														.getResult();
						return new Object[] {
								calculation,
								streamlineClass.newInstance(),
								globalEquClasses,
								disOfDRelate2C,
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Calculation<Sig> calculation = (Calculation<Sig>) result[r++];
						ShrinkInstance<?, ?, ?> shrinkInstance = (ShrinkInstance<?, ?, ?>) result[r++];
						Collection<Collection<Instance>> globalEquClasses = (Collection<Collection<Instance>>) result[r++];
						Sig disOfDRelate2C = (Sig) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE, shrinkInstance);
						getParameters().setNonRoot("globalEquClasses", globalEquClasses);
						getParameters().setNonRoot("disOfDRelate2C", disOfDRelate2C);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(1, "|DIS(D/C)| = {}"),
									disOfDRelate2C
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> Instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder().loadCurrentInfo(Instances)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
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
				}.setDescription("Compute Discernibility Degree of DIS(D/C)"),
			// 2. Initializations.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("2. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Step 2: j=1, U[j] = U, U' = {}, A[j] = C, A' = {}, red = {}
						Collection<Integer> red = new LinkedList<>();
						Collection<Integer> removedAttributes = new HashSet<>(attributes.length);

						// * Initiate for step 3.
						//		U/red = { U }, for red = {}
						Collection<Collection<Instance>> redEquClasses = new ArrayList<>(1);
						redEquClasses.add(instances);
						
						// * Initiate for step 5.
						//		U/D
						Collection<Collection<Instance>> decEquClasses =
								TengDiscernibilityViewAlgorithm
									.Basic
									.equivalenceClass(
											instances,
											new IntegerArrayIterator(0)
									).values();
						
						return new Object[] {
								red,
								removedAttributes,
								redEquClasses,
								decEquClasses
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> red = (Collection<Integer>) result[r++];
						Collection<Integer> removedAttributes = (Collection<Integer>) result[r++];
						Collection<Collection<Instance>> redEquClasses = (Collection<Collection<Instance>>) result[r++];
						Collection<Collection<Instance>> decEquClasses = (Collection<Collection<Instance>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						getParameters().setNonRoot("removedAttributes", removedAttributes);
						getParameters().setNonRoot("redEquClasses", redEquClasses);
						getParameters().setNonRoot("decEquClasses", decEquClasses);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(getReport(), (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initializations"),
			// 3. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								reduct
						);
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								reduct
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								reduct.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sig loop")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer<>(getParameters(), logOn)
				),
		};
	}
	
	public long getTime() {
		return getComponents().stream()
				.map(comp->ProcedureUtils.Time.sumProcedureComponentTimes(comp))
				.reduce(Long::sum).orElse(0L);
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return getComponents().stream().map(ProcedureComponent::getDescription).toArray(String[]::new);
	}
}