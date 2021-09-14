package featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import common.utils.LoggerUtil;
import common.utils.RandomUtils;
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
import featureSelection.repository.entity.opt.artificialFishSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.follow.FishFollowActionFactory;
import featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.follow.FishFollowActionInterf;
import featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.swarm.FishSwarmActionFactory;
import featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.swarm.FishSwarmActionInterf;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Artificial Fish Swarm Algorithm</strong> loop executions. Mainly, this contains 
 * {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Generation loop procedure controller</strong>
 * 		<p>To control the generation loop, if <code>procedure[2]</code> returns true, reaching max convergence, 
 * 		break the loop. Otherwise, loop until reaching max iteration. 
 * 	</li>
 * 	<li>
 * 		<strong>Generate fish group</strong>
 * 		<p>Generate a new group of fishes for optimum reducts searching.
 * 	</li>
 * 	<li>
 * 		<strong>Fish group update controller</strong>
 * 		<p>To control fishes to search and update, containing actions: swarming, following, searching.
 * 	</li>
 * 	<li>
 * 		<strong>Fish group update</strong>
 * 		<p>To control fishes to update.
 * 	</li>
 * 	<li>
 * 		<strong>Fish action controller</strong>
 * 		<p>To control fish perform actions: swarming, following, searching.
 * 		<p><code>FishSwarmActionFactory</code>
 * 		<p><code>FishFollowActionFactory</code>
 * 	</li>
 * 	<li>
 * 		<strong>Search action</strong>
 * 		<p>To control a fish to perform searching action.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link DefaultProcedureContainer}(only!), the following parameters are used in 
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_RANDOM_INSTANCE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_ALGORITHM}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see FishSwarmActionFactory#newAction(FeatureImportance, Collection, Fish, Fish[], ReductionParameters)
 * @see FishFollowActionFactory#newAction(FeatureImportance, Collection, Fish, Fish[], ReductionParameters)
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <FV>
 * 		Type of fitness value.
 * @param <PosiValue>
 * 		Type of position value for {@link Position}.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 */
@Slf4j
public class DefaultGenerationLoopProcedureContainer<Cal extends FeatureImportance<Sig>,
													Sig extends Number, 
													CollectionItem,
													FV extends FitnessValue<? extends Number>,
													PosiValue, 
													Posi extends Position<PosiValue>>
	extends DefaultProcedureContainer<GenerationRecord<Posi, Sig>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;

	public DefaultGenerationLoopProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "FSA Generation loops";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings({ "unchecked", "unused"})
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Generation loop procedure controller.
			new TimeCountedProcedureComponent<GenerationRecord<Posi, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						ReductionParameters params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
							});
						
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "params.iteration={}"), params.getIteration());
						}
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters params =
								(ReductionParameters) parameters[p++];
						GenerationRecord<Posi, Sig> geneRecord =
								(GenerationRecord<Posi, Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						ProcedureComponent<Boolean> comp2 = (ProcedureComponent<Boolean>) getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						while (geneRecord.getGeneration() < params.getIteration()) {
							// iter++
							geneRecord.nextGeneration();
							TimerUtils.timePause((TimeCounted) component);
							comp1.exec();
							comp2.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return geneRecord;
					}, 
					(component, geneRecord) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_OPTIMIZATION_EXIT_ITERATION]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_ITERATION,
							geneRecord.getGeneration()
						);
						//	[STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_FITNESS,
							geneRecord.getBestFeatureSignificance()
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(report, (TimeCountedProcedureComponent<?>) component);
						//	[REPORT_OPTIMIZATION_EXIT_ITERATION]
						ProcedureUtils
							.Report
							.saveItem(
									report, 
									component.getDescription(),
									ReportConstants.Procedure.REPORT_OPTIMIZATION_EXIT_ITERATION,
									geneRecord.getGeneration()
							);
						//	[REPORT_OPTIMIZATION_EXIT_ITERATION]
						ProcedureUtils
							.Report
							.saveItem(
									report, 
									component.getDescription(),
									ReportConstants.Procedure.REPORT_OPTIMIZATION_EXIT_FITNESS,
									geneRecord.getBestFeatureSignificance()
							);
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Generation loop procedure controller"),
			// 2. Generate fish group.
			new TimeCountedProcedureComponent<Fish<Posi>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_RANDOM_INSTANCE),
						});
						if (logOn) {
							ReductionParameters params = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
							GenerationRecord<Posi, Sig> geneRecord = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
							log.info(LoggerUtil.spaceFormat(2, "generation={}/{}"), 
									geneRecord.getGeneration(), params.getIteration()
							);
						}
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters params =
								(ReductionParameters) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Random random =
								(Random) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return  params.getFishGroupUpdateAlgorithm()
										.generateFishGroup(
											params.getGroupSize(), 
											attributes.length,
											params, 
											random
										);
					}, 
					(component, fishGroup) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("fishGroup", fishGroup);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						GenerationRecord<Posi, Sig> generRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_OPTIMIZATION_CURRENT_ITERATION]
						ProcedureUtils.Report
									.saveItem(report, 
											reportMark,
											ReportConstants.Procedure.REPORT_OPTIMIZATION_CURRENT_ITERATION,
											generRecord.getGeneration()
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Generate fish group"),
			// 3. Fish group update controller.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get("fishGroup"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation =
								(Cal) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						Fish<Posi>[] fishGroup =
								(Fish<Posi>[]) parameters[p++];
						ReductionParameters params =
								(ReductionParameters) parameters[p++];
						GenerationRecord<Posi, Sig> generRecord =
								(GenerationRecord<Posi, Sig>) parameters[p++];
						ReductionAlgorithm<Cal, Sig, CollectionItem> redAlg =
								(ReductionAlgorithm<Cal, Sig, CollectionItem>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Boolean> comp3 = (ProcedureComponent<Boolean>) getComponents().get(3);
						/* ------------------------------------------------------------------------------ */
						int lastExit = 0;
						if (!localParameters.containsKey("fitnesses")) {
							localParameters.put("fitnesses", new Fitness[fishGroup.length]);
						}
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Loop and do update.
						int fishExitCount = 0;
						Sig lastBestSig = null;
						UpdateLoop:
						for (int loop=1;;loop++) {
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("innerLoop", loop);
							
							if (logOn) {
								int count=0;
								for (Fish<?> f: fishGroup)	if (f.isExited())	count++;
								if (lastExit!=count || loop % 50 == 1) {
									lastExit = count;
									log.info(LoggerUtil.spaceFormat(2, "*. Inner Loop {}, {}/{} fishes has exited"), 
											loop, count, fishGroup.length);
								}
							}
							
							comp3.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							
							// Loop until all fishes exit or reach max update iteration.
							if (params.getMaxFishUpdateIteration()>0 && 
								params.getMaxFishUpdateIteration()<=loop
							) {
								if (logOn) {
									log.info(LoggerUtil.spaceFormat(2, "*. Reaching max update iteration({}), break."), 
											params.getMaxFishUpdateIteration()
									);
								}
								break;
							}else {
								fishExitCount = 0;
								for (int i=0; i<fishGroup.length; i++) {
									if (params.getMaxFishExit()>0) {
										if (fishGroup[i].isExited())	fishExitCount++;
										if (params.getMaxFishExit()<=fishExitCount) {
											if (logOn) {
												log.info(LoggerUtil.spaceFormat(2, "*. Break @ Inner Loop {}, {}/{} fishes has exited"), 
														loop, fishExitCount, fishGroup.length);
											}
											break;
										}
										else if (i==fishGroup.length-1)	continue UpdateLoop;
									}else {
										if (!fishGroup[i].isExited())	continue UpdateLoop;
									}
								}
								break;
							}
						}
						return false;
					}, 
					(component, breakMark) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						ReductionParameters params = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						GenerationRecord<Posi, Sig> geneRecord = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						Collection<CollectionItem> collectionItems = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS);
						Fish<Posi>[] fishGroup = getParameters().get("fishGroup");
						Fitness<Posi, FV>[] fitnesses = (Fitness<Posi, FV>[]) localParameters.get("fitnesses");
						//	[STATISTIC_ITERATION_INFOS]
						getParameters().setNonRoot(
							StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
							ProcedureUtils.Statistics.IterationInfos.pushInfoOfIteration(
								statistics.getData(),
								params.getIteration(),
								geneRecord.getGeneration(),
								collectionItems, 
								Arrays.stream(fishGroup).map(Fish::getPosition).toArray(Position[]::new),
								Arrays.stream(fishGroup).map(f->f.getPosition().getAttributes()).iterator(),
								Arrays.stream(fitnesses).map(f->f==null? null: f.getFitnessValue().getFitnessValue()).toArray(Number[]::new)
							)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMarkWithLoop();
						reportKeys.add(reportMark);
						GenerationRecord<?, ?> generRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_OPTIMIZATION_CURRENT_ITERATION]
						ProcedureUtils.Report
									.saveItem(report, 
											reportMark, 
											ReportConstants.Procedure.REPORT_OPTIMIZATION_CURRENT_ITERATION,
											generRecord.getGeneration()
						);
						//	[REPORT_OPTIMIZATION_CURRENT_BEST_FITNESS_GROUP_SIZE]
						ProcedureUtils.Report
									.saveItem(report, 
											reportMark,
											ReportConstants.Procedure.REPORT_OPTIMIZATION_CURRENT_BEST_FITNESS_GROUP_SIZE,
											generRecord.getBestFitnessPosition().size()
						);
						//	[REPORT_OPTIMIZATION_CURRENT_BEST_FITNESS_GROUP_SIZE]
						ProcedureUtils.Report
									.saveItem(report, 
											reportMark,
											ReportConstants.Procedure.REPORT_OPTIMIZATION_CURRENT_MAX_FITNESS,
											generRecord.getBestFeatureSignificance()
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Fish group update controller"),
			// 4. Fish group update.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
//						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "4. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get("fishGroup"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Fish<Posi>[] fishGroup =
								(Fish<Posi>[]) parameters[p++];
						ReductionParameters params =
								(ReductionParameters) parameters[p++];
						GenerationRecord<Posi, Sig> generRecord =
								(GenerationRecord<Posi, Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Object[]> comp4 = (ProcedureComponent<Object[]>) getComponents().get(4);
						/* ------------------------------------------------------------------------------ */
						Fitness<Posi, FV>[] fitnesses = (Fitness<Posi, FV>[]) localParameters.get("fitnesses");
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Loop fishes without exit mark.
						int fishExited = 0;
						Fitness<Posi, FV> bestFitness;
						for (int i=0; i<fishGroup.length; i++) {
							if (params.getMaxFishExit()>0 && fishExited>=params.getMaxFishExit()) {
								break;
							}else if (!fishGroup[i].isExited()) {
								TimerUtils.timePause((TimeCounted) component);
								localParameters.put("fishIndex", i);
								
//								log.info(LoggerUtil.spaceFormat(3, "fish[{}]:"), i);
								
								Object[] result = comp4.exec();
								boolean exit = (boolean) result[0];
								fitnesses[i] = bestFitness = (Fitness<Posi, FV>) result[1];
								TimerUtils.timeContinue((TimeCounted) component);
								
//								log.info(LoggerUtil.spaceFormat(3, "fish[{}]: {}"), i, bestFitness.getFitnessValue());
								
								if (exit) {
									TimerUtils.timePause((TimeCounted) component);
									if (logOn) {
										int innerLoop = (int) localParameters.get("innerLoop");
										log.info(LoggerUtil.spaceFormat(3, "fish[{}] exit! @ Inner Loop {}"),
												i, innerLoop);
									}
									TimerUtils.timeContinue((TimeCounted) component);
									
									fishGroup[i].exit();
									if (params.getMaxFishExit()>0)	fishExited++;
									// update global best
									generRecord.updateBestFeatureSignificance(
											bestFitness.getPosition(), 
											(Sig) bestFitness.getFitnessValue().getDependency()
									);
								}
							}
						}
						return true;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMarkWithLoop();
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
					return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Fish group update"),
			// 5. Fish action controller.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "5. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get("fishGroup"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								localParameters.get("fishIndex"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation =
								(Cal) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						Fish<Posi>[] fishGroup =
								(Fish<Posi>[]) parameters[p++];
						ReductionParameters params =
								(ReductionParameters) parameters[p++];
						GenerationRecord<Posi, Sig> generRecord =
								(GenerationRecord<Posi, Sig>) parameters[p++];
						int fishIndex =
								(int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Object[]> comp5 = (ProcedureComponent<Object[]>) getComponents().get(5);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Loop fishes without exit mark.
						Fitness<Posi, FV> bestFitness;
						
//						log.info(LoggerUtil.spaceFormat(3, "current posi: {}"), Arrays.toString(fishGroup[fishIndex].getPosition().getAttributes()));
//						log.info(LoggerUtil.spaceFormat(3, "current posi: {}"), fishGroup[fishIndex].getPosition().getAttributes().length);
						
						//	searchPos = search()
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("fish", fishGroup[fishIndex]);
						Posi searchPos = (Posi) comp5.exec()[0];
						
						//	swarmPos = swarm()
						FishSwarmActionInterf<Posi> swarmAction =
								FishSwarmActionFactory.newAction(
										calculation, 
										collectionList, 
										fishGroup[fishIndex], 
										fishGroup, 
										params
								);
						TimerUtils.timeContinue((TimeCounted) component);
								
						Posi swarmPos = (Posi) swarmAction.exec(component, getParameters());
						//log.info(LoggerUtil.spaceFormat(4, "Swarm |pos|: {}"), swarmPos==null?null: swarmPos.attributes().length);
						
						if (swarmPos==null) {
							TimerUtils.timePause((TimeCounted) component);
							swarmPos = (Posi) comp5.exec()[0];
							TimerUtils.timeContinue((TimeCounted) component);
						}
								
						TimerUtils.timePause((TimeCounted) component);
						//	followPos  = follow()
						FishFollowActionInterf<Posi> followAction =
								FishFollowActionFactory.newAction(
										calculation, 
										collectionList, 
										fishGroup[fishIndex], 
										fishGroup, 
										params
								);
						TimerUtils.timeContinue((TimeCounted) component);
								
						Posi followPos = (Posi) followAction.exec(component, getParameters());
						//log.info(LoggerUtil.spaceFormat(4, "Follow |pos|: {}"), followPos==null?null: followPos.attributes().length);
						
						if (followPos==null) {
							TimerUtils.timePause((TimeCounted) component);
							followPos = (Posi) comp5.exec()[0];
							TimerUtils.timeContinue((TimeCounted) component);
						}
						//	currentFish.pos = maxFitness(searchPos, swarmPos, followPos)
						bestFitness = params.getFitnessAlgorthm()
											.findBestFitness(
												params.getFitnessAlgorthm()
													.calculateFitness(
														params.getReductionAlgorithm(),
														searchPos.getAttributes(),
														params.getReductionAlgorithm()
																.dependency(
																	calculation, 
																	collectionList, 
																	searchPos.getAttributes()
														)
													),
												params.getFitnessAlgorthm()
													.calculateFitness(
														params.getReductionAlgorithm(),
														swarmPos.getAttributes(),
														params.getReductionAlgorithm()
																.dependency(
																	calculation, 
																	collectionList, 
																	swarmPos.getAttributes()
														)
													),
												params.getFitnessAlgorthm()
													.calculateFitness(
														params.getReductionAlgorithm(),
														followPos.getAttributes(),
														params.getReductionAlgorithm()
																.dependency(
																	calculation, 
																	collectionList, 
																	followPos.getAttributes()
														)
													)
											);
						fishGroup[fishIndex].setPosition(bestFitness.getPosition());
						
						return new Object[] {
								Double.compare(
									FastMath.abs(
										generRecord.getGlobalDependency()
										-
										bestFitness.getFitnessValue().getDependency().doubleValue()
									),
									sigDeviation.doubleValue()
								)<=0,
								bestFitness,
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean exit = (boolean) result[r++];
						Fitness<Posi, FV> bestFitness = (Fitness<Posi, FV>) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						int fishIndex = (int) localParameters.get("fishIndex");
						String reportMark = reportMarkWithLoop(fishIndex);
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
					return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Fish action controller"),
			// 6. Search action.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						ReductionParameters params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "6. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								localParameters.get("fish"),
								getParameters().get("positionLength"),
								params.getTryNumbers(),
								params.getFitnessAlgorthm(),
								params.getReductionAlgorithm(),
								getParameters().get(ParameterConstants.PARAMETER_RANDOM_INSTANCE)
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int calculationCount = 0;
						/* ------------------------------------------------------------------------------ */
						int pi=0;
						Cal calculation =
								(Cal) parameters[pi++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[pi++];
						Fish<Posi> fish =
								(Fish<Posi>) parameters[pi++];
						int positionLength =
								(int) parameters[pi++];
						int tryNumbers =
								(int) parameters[pi++];
						FitnessAlgorithm<Cal, Sig, FV, Posi> fitnessAlgorithm =
								(FitnessAlgorithm<Cal, Sig, FV, Posi>) 
								parameters[pi++];
						ReductionAlgorithm<Cal, Sig, CollectionItem> redAlg =
								(ReductionAlgorithm<Cal, Sig, CollectionItem>) parameters[pi++];
						Random random =
								(Random) parameters[pi++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// i=0, nextPos = POS
						// Rt = Rk, Rp = Rk
						int[] attributes = fish.getPosition().getAttributes();
						// * If user has set the param tryNumbers, follow user's will.
						if (tryNumbers>0)	tryNumbers = Math.min(positionLength-attributes.length, tryNumbers);
						// * If user hasn't set the param tryNumbers. Set it automatically.
						else 				tryNumbers = positionLength-attributes.length;
						if (tryNumbers==0)	return new Object[] {	null, calculationCount	};
						
						Posi posi = (Posi) fish.getPosition().clone();

						// extract attribute candidates.
						int[] extraAttributes = new int[positionLength-attributes.length];
						for (int ext=0, p=1; p<=positionLength && ext<extraAttributes.length; p++)
							if (!posi.containsAttribute(p))	extraAttributes[ext++] = p;
						
						Fitness<Posi, FV> currentFishFitness =
								fitnessAlgorithm.calculateFitness(
										redAlg, 
										fish.getPosition().getAttributes(),
										redAlg.dependency(
											calculation, 
											collectionList, 
											fish.getPosition().getAttributes()
										)
								);
						
						TimerUtils.timePause((TimeCounted) component);
						calculationCount++;
						TimerUtils.timeContinue((TimeCounted) component);
						
						// Loop and add position value until fitnessCount(nextPos) > currentFish.fitness or i>= tryNumbers
						int[] examAttributes = Arrays.copyOf(attributes, attributes.length+1);
						RandomUtils.ShuffleArrayIterator shuffledAttrIterator =
								new RandomUtils.ShuffleArrayIterator(extraAttributes, random);
						for (int i=0; i<tryNumbers && shuffledAttrIterator.hasNext(); i++) {
							// Select a feature a[k] in C-Rp randomly. Rt = {Rk U a[k]}. (Rt=nextPos)
							examAttributes[examAttributes.length-1] = shuffledAttrIterator.next();
							
							// if fitnessCount(nextPos) > currentFish.fitness, break.
							Fitness<Posi, FV> updatedFitness = 
									fitnessAlgorithm.calculateFitness(
											redAlg, 
											examAttributes,
											redAlg.dependency(
												calculation, 
												collectionList,
												examAttributes
											)
									);
							
							TimerUtils.timePause((TimeCounted) component);
							calculationCount++;
							TimerUtils.timeContinue((TimeCounted) component);
							
							if (fitnessAlgorithm.compareFitnessValue(
									updatedFitness.getFitnessValue(), 
									currentFishFitness.getFitnessValue()
								)>0
							) {
								break;
							}
						}
						// If	attribute with greater fitness was found, add into posi as nextPos
						// else	add the last attribute in "extraAttributes" into posi as nextPos
						// then	return nextPos
						if (examAttributes[examAttributes.length-1]==0) {
							if (extraAttributes[extraAttributes.length-1]<=0)
								throw new RuntimeException("invalid attribute : "+extraAttributes[extraAttributes.length-1]);
							posi.addAttributeInPosition(extraAttributes[extraAttributes.length-1]);
						}else {
							if (examAttributes[examAttributes.length-1]<=0)
								throw new RuntimeException("invalid attribute : "+examAttributes[examAttributes.length-1]);
							posi.addAttributeInPosition(examAttributes[examAttributes.length-1]);
						}
						return new Object[] {
								posi, 
								calculationCount,
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Posi posi = (Posi) result[r++];
						int calculationCount = (int) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						int fishIndex = (int) localParameters.get("fishIndex");
						String reportMark = reportMarkWithLoop(fishIndex);
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
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Search action"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenerationRecord<Posi, Sig> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (GenerationRecord<Posi, Sig>) componentArray[0].exec();
	}

	private String reportMark() {
		GenerationRecord<Posi, Sig> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
		return "Generation "+geneRecord.getGeneration();
	}
	
	private String reportMarkWithLoop() {
		GenerationRecord<Posi, Sig> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
		int innerLoop = (int) localParameters.get("innerLoop");
		return "Generation "+geneRecord.getGeneration()+" Inner Loop["+innerLoop+"]";
	}

	private String reportMarkWithLoop(int fishIndex) {
		GenerationRecord<Posi, Sig> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
		int innerLoop = (int) localParameters.get("innerLoop");
		return "Generation "+geneRecord.getGeneration()+" Inner Loop["+innerLoop+"]"+" Fish["+fishIndex+"]";
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
