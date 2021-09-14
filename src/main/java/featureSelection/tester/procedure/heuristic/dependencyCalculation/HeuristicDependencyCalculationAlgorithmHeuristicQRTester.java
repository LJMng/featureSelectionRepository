package featureSelection.tester.procedure.heuristic.dependencyCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.support.alg.dependencyCalculation.HeuristicDependencyCalculationStrategy;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.basic.support.searchStrategy.SequentialSearchStrategy;
import featureSelection.repository.algorithm.alg.dependencyCalculation.HeuristicDependencyCalculationAlgorithm;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.heuristicDependencyCalculation.FeatureImportance4HeuristicDependencyCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.dependencyCalculation.procedure.CoreProcedureContainer4HDC;
import featureSelection.tester.procedure.heuristic.dependencyCalculation.procedure.ReductInspectionProcedureContainer4HDC;
import featureSelection.tester.procedure.heuristic.dependencyCalculation.procedure.SignificantAttributeSeekingLoopProcedureContainer4HDC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Heuristic Dependency Calculation (HDC)</strong> Feature Selection.
 * <p>
 * Original paper:
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0031320318301432">
 * "A heuristic based dependency calculation technique for rough set theory"</a> by
 * Muhammad Summair Raza, Usman Qamar.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 4 {@link ProcedureComponent}s,
 * refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Get dep(C)</strong>:
 * 		<p>Calculate the global dependency of U induced by <code>C</code>: <code>dep(C)</code>.
 * 	</li>
 * 	<li>
 * 		<strong>Get Core</strong>:
 * 		<p>Obtain the Core .
 * 		<p><code>CoreProcedureContainer4HDC</code>
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>:
 * 		<p>Loop and search for the most significant attribute and add into the reduct until reaching
 * 	    	exit condition.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer4HDC</code>
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>:
 *	 	<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>ReductInspectionProcedureContainer4HDC</code>
 * 	</li>
 * <ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_QR_EXEC_CORE}</li>
 * </ul>
 *
 * @see FeatureImportance4HeuristicDependencyCalculation
 * @see HeuristicDependencyCalculationAlgorithm
 * @see CoreProcedureContainer4HDC
 * @see SignificantAttributeSeekingLoopProcedureContainer4HDC
 * @see ReductInspectionProcedureContainer4HDC
 *
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class HeuristicDependencyCalculationAlgorithmHeuristicQRTester<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				SequentialSearchStrategy, HashSearchStrategy,
				StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>,
				HeuristicDependencyCalculationStrategy,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;

	public HeuristicDependencyCalculationAlgorithmHeuristicQRTester(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		String searchStrategy;
		Class<?> calculationClass = getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS);
		if (HashSearchStrategy.class.isAssignableFrom(calculationClass))
			searchStrategy = "(Hash)";
		else if (SequentialSearchStrategy.class.isAssignableFrom(calculationClass))
			searchStrategy = "(Seq.)";
		else
			searchStrategy = null;
		return "QR-HDC"+(searchStrategy==null?"": searchStrategy)+
				"("+ ProcedureUtils.ShortName.calculation(getParameters())+")"+
				"("+ ProcedureUtils.ShortName.byCore(getParameters())+")";
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
			// 1. Count decision values.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(),
					(component) -> {
						if (logOn){
							log.info("1. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						List<Instance> instances = (List<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						return HeuristicDependencyCalculationAlgorithm
								.Basic
								.decisionValues(instances);
					},
					(component, decisionValues) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(1, "|Decision values|={}"),
									decisionValues.size()
							);
						}
						this.getParameters().setNonRoot("decisionValues", decisionValues);
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
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Count decision values"),
			// 2. Get dep(C)
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_SIG,
					this.getParameters(),
					(component) -> {
						if (logOn){
							log.info("2. "+component.getDescription());
						}
						FeatureImportance4HeuristicDependencyCalculation<Sig> calculation;
						Class<? extends FeatureImportance4HeuristicDependencyCalculation<Sig>> calculationClass =
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS);
						getParameters().setNonRoot(
								ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE,
								calculation = calculationClass.newInstance()
						);
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								calculation,
								getParameters().get("decisionValues"),
						});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						List<Instance> instances =
								(List<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						FeatureImportance4HeuristicDependencyCalculation<Sig> calculation =
								(FeatureImportance4HeuristicDependencyCalculation<Sig>)
								parameters[p++];
						Collection<Integer> decisionValues =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						return calculation.calculate(instances, decisionValues, new IntegerArrayIterator(attributes), parameters)
											.getResult();
					},
					(component, globalSig) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Sig(global)={}"),
									globalSig
							);
						}
						this.getParameters().setNonRoot("globalDependency", globalSig);
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
						ProcedureUtils.Report.ExecutionTime.save(
								report,
								(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get dep(C)"),
			// 3. Get Core
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(),
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}

						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes = (int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Boolean byCore = getParameters().get(ParameterConstants.PARAMETER_QR_EXEC_CORE);
						return byCore!=null && byCore?
								(Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec():
								new HashSet<>(attributes.length);
					},
					(component, core) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, core);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(),
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(),
								0,
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get Core")
				.setSubProcedureContainer(
					"CoreProcedureContainer",
					new CoreProcedureContainer4HDC<Sig>(getParameters(), logOn)
				),
			// 4. Sig loop
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(),
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}
					},
					(component, parameters) -> {
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					},
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|Reduct Candidate| = {}"),
									result.size()
							);
						}
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								result
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(),
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(),
								0,
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sig loop")
				.setSubProcedureContainer(
					"significantAttributeSeekingLoopProcedureContainer",
					new SignificantAttributeSeekingLoopProcedureContainer4HDC<Sig>(getParameters(), logOn)
				),
			// 5. Inspection
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(),
					(component) -> {
						if (logOn){
							log.info("4. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
						});
					},
					(component, parameters) -> {
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					},
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"),
									red.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, red);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								red
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(),
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(),
								0,
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long componentTime = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), componentTime);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection")
				.setSubProcedureContainer(
					"ReductInspectionProcedureContainer",
					new ReductInspectionProcedureContainer4HDC<Sig>(getParameters(), logOn)
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