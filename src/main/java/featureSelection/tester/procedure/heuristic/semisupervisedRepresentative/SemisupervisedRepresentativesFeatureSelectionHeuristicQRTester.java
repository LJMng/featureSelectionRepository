package featureSelection.tester.procedure.heuristic.semisupervisedRepresentative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
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
import featureSelection.basic.support.alg.SemisupervisedRepresentativeStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.repository.algorithm.alg.semisupervisedRepresentative.SemisupervisedRepresentativeAlgorithm;
import featureSelection.repository.entity.alg.semisupervisedRepresentative.calculationPack.SemisupervisedRepresentativeCalculations4EntropyBased;
import featureSelection.repository.entity.alg.semisupervisedRepresentative.calculationPack.cache.InformationEntropyCache;
import featureSelection.repository.entity.alg.semisupervisedRepresentative.graph.DirectedAcyclicGraghWeightedEdge;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.semisupervisedRepresentative.procedure.DirectedAcyclicGraphConstruction;
import featureSelection.tester.procedure.heuristic.semisupervisedRepresentative.procedure.RelevantFeatureSelectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.semisupervisedRepresentative.procedure.RepresentativeFeatureSelection;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Semi-supervised Representative Feature Selection</strong> Heuristic
 * Quick Reduct Feature Selection.
 * <p>
 * Original paper:
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0031320316302242">"An efficient
 * semi-supervised representatives feature selection algorithm based on information theory"</a> by
 * Yintong Wang, Jiandong Wang, Hao Liao, Haiyan Chen.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 6 {@link ProcedureComponent}s, refer
 * to steps:
 * <ul>
 * 	<li>
 * 		<strong>Pre-initialization</strong>:
 * 		<p>Some pre-initializations for Feature Selection. Creating instance of {@link Calculation}
 * 	    	and wrap labeled {@link Instance}s, unlabeled {@link Instance}s into a collection.
 * 	    	Initiate trade-off(&beta;)= if it is <code>null</code>: &beta; = 1 - (N/(N+M))^1/2,
 * 			where N=|labeled U|, M=|unlabeled U|
 * 	</li>
 * 	<li>
 * 		<strong>Decision entropy calculation</strong>:
 * 		<p>Calculate entropy <i>H(D)</i> for global usage in the future.
 * 	</li>
 * 	<li>>> Part 1: Irrelevant Feature Removal.</li>
 * 	<li>
 * 		<strong>Irrelevant Feature Removal</strong>:
 * 		<p>Referring to the original paper, this step removes irrelevant features (i.e. select relevance
 * 	    	features) bases on the parameter "feature relevance threshold"(i.e. &alpha; in the paper).
 * 		<p><code>RelevantFeatureSelectionProcedureContainer</code>,
 * 	</li>
 * 	<li>
 * 		<strong>F1-Relevance based feature descending sorting</strong>:
 * 		<p>Sort relevant features by F1-Relevance calculated in descending order.
 * 	</li>
 * 	<li>>> Part 2: Directed Acyclic Graph Construction and Partition</li>
 * 	<li>
 * 		<strong>Directed Acyclic Graph Construction and Partition</strong>:
 * 		<p>Construct a Directed Acyclic Graph to cluster relevant features into groups.
 * 		<p><code>DirectedAcyclicGraphConstruction</code>
 * 	</li>
 * 	<li>>> Part 3: Representative Feature Selection</li>
 * 	<li>
 * 		<strong>Representative Feature Selection</strong>:
 * 		<p>Select the feature with max. F1-Rel value as the representative of the group it is in and
 * 	    	as an attribute of the final reduct.
 * 		<p><code>RepresentativeFeatureSelection</code>
 *	 </li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_LABELED_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNLABELED_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>relevantFeatureF1Rels</li>
 * </ul>
 * <p>
 * <strong>Extra parameters</strong> beside standard parameters(e.g. attributes, etc.).
 * <ul>
 * 	<li><strong>featureRelevanceThreshold</strong>: <code>double</code>, [0, 1], optional
 * 		<p>A threshold for feature relevance, marked as <strong>&alpha;</strong>.
 * 			Features with F-Rel value greater than &alpha; are considered relevant and
 * 			reserved, otherwise are removed.
 * 	</li>
 * 	<li><strong>tradeOff</strong>: <code>double</code>, [0, 1]
 * 		<p>Superivsed/Semi-supervised/Un-supervised trade-off, marked as <strong>&beta;</strong>.
 * 		<p><strong>supervise</strong> only:    &beta; = <strong>0</strong>; 
 * 		<p><strong>un-supervise</strong> only: &beta; = <strong>1</strong>; 
 * 		<p><strong>semi-supervise</strong>:    &beta; = <strong>(0, 1)</strong>.
 * 	</li>
 * </ul>
 * 
 * @see RelevantFeatureSelectionProcedureContainer
 * @see DirectedAcyclicGraphConstruction
 * @see RepresentativeFeatureSelection
 * 
 * @author Benjamin_L
 */
@Slf4j
public class SemisupervisedRepresentativesFeatureSelectionHeuristicQRTester 
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				QuickReductHeuristicReductStrategy,
				SemisupervisedRepresentativeStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public SemisupervisedRepresentativesFeatureSelectionHeuristicQRTester(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
//		Double featureRelevanceThreshold = getParameters().get("featureRelevanceThreshold");
//		Double tradeOff = getParameters().get("tradeOff");
//		return "QR-SRFS("+
//					(featureRelevanceThreshold!=null? String.format("α=%.6f", featureRelevanceThreshold): "") +
//					(featureRelevanceThreshold!=null && tradeOff!=null? ", ":"") +
//					(tradeOff!=null?"β="+String.format("%.3f", tradeOff): "") +
//				")";
		return "QR-SRFS";
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
			// 1. Pre-initialization
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("1. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<Instance> labeledInstances =
								getParameters().get(ParameterConstants.PARAMETER_LABELED_UNIVERSE_INSTANCES);
						Collection<Instance> unlabeledInstances =
								getParameters().get(ParameterConstants.PARAMETER_UNLABELED_UNIVERSE_INSTANCES);

						Collection<Instance> allInstances =
								new ArrayList<>(
										labeledInstances.size()+unlabeledInstances.size()
								);
						allInstances.addAll(labeledInstances);
						allInstances.addAll(unlabeledInstances);
						
						// Set &beta;
						if (getParameters().get("tradeOff")==null) {
							// auto-set trade-off(&beta;) = 1 - (N/(N+M))^1/2,
							// where N=|labeled U|, M=|unlabeled U|
							double tradeOff =
									1 - FastMath.sqrt(labeledInstances.size() / (double) allInstances.size());
							getParameters().setNonRoot("tradeOff", tradeOff);
						}
						
						Class<? extends Calculation<?>> calculationClass =
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS);
						/* ------------------------------------------------------------------------------ */
						return new Object[] {
								allInstances,
								calculationClass.newInstance()
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Instance> allInstances = (Collection<Instance>) result[r++];
						Calculation<?> calculation = (Calculation<?>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, allInstances);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
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
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Pre-initialization"),
			// 2. Decision entropy calculation and cache initialization.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("2. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_LABELED_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> labeledInstances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						SemisupervisedRepresentativeCalculations4EntropyBased calculation =
								(SemisupervisedRepresentativeCalculations4EntropyBased)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Obtain equivalence classes induced by decision attribute: U/D, calculate
						//  entropy H(D).
						Collection<Collection<Instance>> decEquClasses =
								SemisupervisedRepresentativeAlgorithm
									.Basic
									.equivalenceClass(
											labeledInstances,
											new IntegerArrayIterator(0)
									).values();
						double decisionInfoEntropy = 
								calculation.getInfoEntropyCalculation()
											.calculate(decEquClasses, labeledInstances.size())
											.getResult().doubleValue();	
						/* ------------------------------------------------------------------------------ */
						return new Object[] {
								decisionInfoEntropy,
								new InformationEntropyCache(attributes.length)
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						double decisionInfoEntropy = (double) result[r++];
						InformationEntropyCache infoEntropyCache = (InformationEntropyCache) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("decisionInfoEntropy", decisionInfoEntropy);
						getParameters().setNonRoot("infoEntropyCache", infoEntropyCache);
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(1, "H(D) = {}"),
									String.format("%.8f", decisionInfoEntropy)
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Decision entropy calculation and cache initialization"),
			// >> Part 1: Irrelevant Feature Removal.
			// 3. Irrelevant Feature Removal
			new ProcedureComponent<Map<Integer, Double>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						return (Map<Integer, Double>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, relevantFeatureF1Rels) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("relevantFeatureF1Rels", relevantFeatureF1Rels);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Irrelevant Feature Removal")
				.setSubProcedureContainer(
					"RelevantFeatureSelectionProcedureContainer",
					new RelevantFeatureSelectionProcedureContainer(getParameters(), logOn)
				),
			// 4. F1-Relevance based feature descending sorting
			new TimeCountedProcedureComponent<Integer[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("4. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("relevantFeatureF1Rels"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Map<Integer, Double> relevantFeatureF1Rels =
								(Map<Integer, Double>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return SemisupervisedRepresentativeAlgorithm
								.descendingSortRelevantFeatureByF1Relevance(
										relevantFeatureF1Rels
									);
					}, 
					(component, relevantFeatures) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("relevantFeatures", relevantFeatures);
//						if (logOn) {
//							log.info(LoggerUtil.spaceFormat(1, "Sorted Relevant Features: {}"), 
//									Arrays.toString(relevantFeatures)
//							);
//							Map<Integer, Double> relevantFeatureF1Rels = getParameters().get("relevantFeatureF1Rels");
//							log.info(LoggerUtil.spaceFormat(1, "Sorted Relevant Features: {}"), 
//									Arrays.stream(relevantFeatures).map(f->{
//										return f+":"+String.format("%.4f", relevantFeatureF1Rels.get(f));
//									}).collect(Collectors.toList())
//							);
//						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								Arrays.asList(relevantFeatures)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("F1-Relevance based feature descending sorting"),
			// >> Part 2: Directed Acyclic Graph Construction and Partition
			// 5. Directed Acyclic Graph Construction and Partition
			new ProcedureComponent<Collection<DirectedAcyclicGraghWeightedEdge<Integer, Double>>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("5. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						return (Collection<DirectedAcyclicGraghWeightedEdge<Integer, Double>>[])
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, relevanceFeatures) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
											
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Directed Acyclic Graph Construction and Partition")
				.setSubProcedureContainer(
					"DirectedAcyclicGraphConstruction", 
					new DirectedAcyclicGraphConstruction(getParameters(), logOn)
				),
			// >> Part 3: Representative Feature Selection
			// 6. Representative Feature Selection
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("6. "+component.getDescription());
					}, 
					(component, parameters) -> {
						int[] representatives = 
								(int[])
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
						return Arrays.stream(representatives).boxed().collect(Collectors.toList());
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, reduct);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								reduct
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
											
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Representative Feature Selection")
				.setSubProcedureContainer(
					"RepresentativeFeatureSelection", 
					new RepresentativeFeatureSelection(getParameters(), logOn)
				),
		};
	}
	
	@Override
	public long getTime() {
		return getComponents().stream()
					.map(comp -> ProcedureUtils.Time.sumProcedureComponentTimes(comp))
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