package featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import common.utils.LoggerUtil;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.ChromosomeCrossAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation.ChromosomeMutation;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.ExitInfo;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMark;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Genetic Algorithm</strong> loop executions. 
 * <p>
 * >> "best fitness == max fitness" <strong>NOT</strong> guaranteed. Not equal if exiting by 
 * reaching max convergence or max iteration.
 * <p>
 * Mainly, this {@link ProcedureContainer} contains the following {@link ProcedureComponent}s:
 * <ul>
 *  <li>
 *  	<strong>Generation loop procedure controller</strong>:
 *  	<p>Different from {@link DefaultGenerationLoopProcedureContainer}
 *  	<p>To control the generation loop, exit loop if
 *  	<p><strong>1)</strong> at least 1 reduct has been found (i.e. global best fitness equals to
 *      	<code>{@link ReductionParameters#getMaxFitness()}</code>);
 *  	<p><strong>2)</strong> if reaching max convergence/max iteration.
 *  	<p>Otherwise, loop until reaching exit condition.
 *   <p>
 *   >> {@link ReductionParameters#getMaxDistinctBestFitness()} <strong>ignored</strong>.
 *  </li>
 *  <li>
 *  	<strong>Calculate fitnesses of chromosomes</strong>
 *  	<p>Use {@link FeatureImportance} in {@link ProcedureParameters} to calculate fitnesses.
 *  </li>
 *  <li>
 *  	<strong>Update generation record best fitnesses</strong>
 *  </li>
 *  <li>
 *  	<strong>Check if reaching max convergence: true/false</strong>
 *  </li>
 *  <li>
 *  	<strong>Crossing-overs</strong>
 *  	<p>Use {@link ChromosomeCrossAlgorithm} to perform cross-overs.
 *  </li>
 *  <li>
 *  	<strong>Mutations</strong>
 *  	<p>Use {@link ChromosomeMutation} to perform mutations.
 *  </li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
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
 * @see DefaultGenerationLoopProcedureContainer
 * @see GlobalBestPrioritizedGenerationLoopProcedureContainer
 * @see FeatureImportance
 * @see ChromosomeCrossAlgorithm
 * @see ChromosomeMutation
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Comparable} extended fitness value.
 */
@Slf4j
public class ReductPrioritizedGenerationLoopProcedureContainer<Cal extends FeatureImportance<Sig>,
																Sig extends Number, 
																CollectionItem,
																Chr extends Chromosome<?>,
																FValue extends FitnessValue<?>>
	extends DefaultGenerationLoopProcedureContainer<Cal, Sig, CollectionItem, Chr, FValue>
{
	public ReductPrioritizedGenerationLoopProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(parameters, logOn);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Generation loop procedure controller.
			new TimeCountedProcedureComponent<GenerationRecord<Chr, FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int componentSize = this.getComponents().size();
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params =
								(ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue>) 
								parameters[p++];
						GenerationRecord<Chr, FValue> geneRecord =
								(GenerationRecord<Chr, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						boolean exit, convergence = false;
						do {
							// Mark the current generation.
							geneRecord.nextGeneration();

							TimerUtils.timePause((TimeCounted) component);
							// execute components:
							//  comp[1]: Calculate fitnesses of chromosomes
							//  comp[2]: Update generation record best fitnesses
							//  comp[3]: Check if break for reaching max convergence
							for (int i=1; i<componentSize; i++) {
								if (i==3) {
									if ((Boolean) this.getComponents().get(i).exec()) {
										TimerUtils.timeContinue((TimeCounted) component);
										return geneRecord;
									}
								}else {
									this.getComponents().get(i).exec();
								}
							}
							TimerUtils.timeContinue((TimeCounted) component);
							
							exit =  // if any satisfied:
									//  dependency satisfied => exit.
									params.getReductionAlgorithm().compareMaxFitness(params.getMaxFitness(), geneRecord)<=0 || 
									//	    reaching max convergence => exit
									(convergence || 
									//	    reaching max iteration => exit
									 geneRecord.getGeneration() >= params.getIterateNum()
									);
						}while (!exit);
						return geneRecord;
					}, 
					(component, geneRecord) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						//	[STATISTIC_EXIT_REASON]
						if (statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON)==null) {
							String exitReason = geneRecord.getGeneration()>=params.getIterateNum()?
												ExitInfo.EXIT_MARK_ITERATION:
												ExitInfo.EXIT_MARK_REACH_MAX_FITNESS;
							statistics.put(
								StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON,
								exitReason
							);
						}
						//	[STATISTIC_OPTIMIZATION_EXIT_ITERATION]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_ITERATION,
							geneRecord.getGeneration()
						);
						//	[STATISTIC_OPTIMIZATION_EXIT_FITNESS]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_FITNESS,
							geneRecord.getBestFitness().getValue()
						);
						//	[STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE,
							geneRecord.getConvergenceCount()
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Generation loop procedure controller"),
			// 2. Calculate fitnesses of chromosomes.
			new TimeCountedProcedureComponent<Fitness<Chr, FValue>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get("chromosome"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionAlgorithm<Cal, Sig, CollectionItem, Chr, FValue> redAlg =
								(ReductionAlgorithm<Cal, Sig, CollectionItem, Chr, FValue>)
								parameters[p++];
						Cal calculation =
								(Cal) parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						Chr[] chromosome =
								(Chr[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return redAlg.calculateFitness(calculation, collectionList, chromosome);
					}, 
					(component, chromosomeFitness) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("chromosomeFitness", chromosomeFitness);
						/* ------------------------------------------------------------------------------ */
						GenerationRecord<Chr, FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(1, "Generation {}"), geneRecord.getGeneration());
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						Collection<CollectionItem> collectionItems = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS);
						Chr[] chromosome = getParameters().get("chromosome");
						
						getParameters().setNonRoot(
							StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
							ProcedureUtils.Statistics.IterationInfos.pushInfoOfIteration(
									statistics.getData(),
									params.getIterateNum(), geneRecord.getGeneration(), collectionItems, 
									chromosome, 
									Arrays.stream(chromosomeFitness).map(f->f.getChromosome().getAttributes()).iterator(), 
									Arrays.stream(chromosomeFitness).map(f->f.getFitnessValue().getValue()).toArray(Number[]::new)
							)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Calculate fitnesses of chromosomes"),
			// 3. Update generation record best fitnesses.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
								localParameters.get("chromosomeFitness"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionAlgorithm<Cal, Sig, CollectionItem, Chr, FValue> redAlg =
								(ReductionAlgorithm<Cal, Sig, CollectionItem, Chr, FValue>)
								parameters[p++];
						Fitness<Chr, FValue>[] chromosomeFitness =
								(Fitness<Chr, FValue>[]) parameters[p++];
						GenerationRecord<Chr, FValue> geneRecord =
								(GenerationRecord<Chr, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> bestFitnessUpdated = new LinkedList<>();
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						boolean resetConvergenceCount = false;
						// Loop over chromosomes and update best fitness.
						for (int i=0; i<chromosomeFitness.length; i++) {
							if (chromosomeFitness[i]==null)	continue;

							if (geneRecord.getBestFitness()==null) {
								// Initiate best fitness
								geneRecord.updateBestFitness(chromosomeFitness[i]);
								
								TimerUtils.timePause((TimeCounted) component);
								// Mark updated best fitness with the fitness of chromosome[i]
								bestFitnessUpdated.add(i);
								TimerUtils.timeContinue((TimeCounted) component);
							}else {
								int bestFitnessCmp = redAlg.compareBestFitness(chromosomeFitness[i], geneRecord);
								// not the best, skip
								if (bestFitnessCmp < 0) {				
									continue;
								// the best, update (1st of the best)
								}else if (bestFitnessCmp > 0) {
									geneRecord.updateBestFitness(chromosomeFitness[i]);
									resetConvergenceCount = true;
									
									TimerUtils.timePause((TimeCounted) component);
									// Reset & mark updated best fitness with the fitness of
									// chromosome[i]
									if (!bestFitnessUpdated.isEmpty())	bestFitnessUpdated.clear();
									bestFitnessUpdated.add(i);
									TimerUtils.timeContinue((TimeCounted) component);
								// one of the best, add
								}else {
									geneRecord.addBestFitness(chromosomeFitness[i]);	
									
									TimerUtils.timePause((TimeCounted) component);
									// Mark updated best fitness with the fitness of chromosome[i]
									bestFitnessUpdated.add(i);
									TimerUtils.timeContinue((TimeCounted) component);
								}
							}
						}
						return new Object[] {
								resetConvergenceCount,
								bestFitnessUpdated
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean resetConvergenceCount = (boolean) result[r++];
						Collection<Integer> bestFitnessUpdated = (Collection<Integer>) result[r++];
						/* ------------------------------------------------------------------------------ */
						localParameters.put("resetConvergenceCount", resetConvergenceCount);
						/* ------------------------------------------------------------------------------ */
						GenerationRecord<Chr, FValue> geneRecord = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						
						if (logOn) {
							if (geneRecord.getBestFitness()!=null && 
								geneRecord.getBestFitness().getValue() instanceof Double
							) {
								log.info(LoggerUtil.spaceFormat(2, "target = {} | best fitness = {}"), 
										params.getMaxFitness()==null || params.getMaxFitness().getValue()==null?
										"null":
										String.format("%.12f", params.getMaxFitness().getValue().doubleValue()),
										geneRecord.getBestFitness()==null || geneRecord.getBestFitness().getValue()==null?
										"null":
										String.format("%.12f", geneRecord.getBestFitness().getValue().doubleValue()));
							}else {
								log.info(LoggerUtil.spaceFormat(2, "target = {} | best fitness = {}"), 
										params.getMaxFitness()==null || params.getMaxFitness().getValue()==null?
										"null":
										params.getMaxFitness().getValue().intValue(),
										geneRecord.getBestFitness()==null || geneRecord.getBestFitness().getValue()==null?
										"null":
										geneRecord.getBestFitness().getValue().intValue());
							}
							log.info(LoggerUtil.spaceFormat(2, "x{} chromosome(s), distinct = {}/{}"), 
									geneRecord.getFitness().size(),
									geneRecord.getDistinctBestFitnessCount(),
									params.getMaxDistinctBestFitness()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						if (!bestFitnessUpdated.isEmpty()) {
							if (resetConvergenceCount)	SupremeMark.resetGlobalBestCounter();
							List<BasicIterationInfo4Optimization<?>> iterInfos =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
							BasicIterationInfo4Optimization<?> iterInfo = iterInfos.get(iterInfos.size()-1);
							for (int index: bestFitnessUpdated) {
								iterInfo.getOptimizationEntityBasicInfo()[index]
										.setSupremeMark(SupremeMark.nextGlobalBest());
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Update generation record best fitnesses"),
			// 4. Check if reaching max convergence: true/false.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "4. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								localParameters.get("resetConvergenceCount"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<Chr, FValue> geneRecord =
								(GenerationRecord<Chr, FValue>) parameters[p++];
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params = 
								(ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue>)
								parameters[p++];
						boolean resetConvergenceCount =
								(boolean) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// if the best value remains unchanged for <code>convergenceLimit</code> times,
						//	considered converged.
						if (geneRecord.getConvergenceCount() >= params.getConvergenceLimit()) {
							return true;
						// else if the best value remains unchanged, count <code>convergenceCount</code>
						}else if (!resetConvergenceCount) {
							geneRecord.countConvergence();
						// else reset <code>convergenceCount</code> for best value having changed.
						}else {
							geneRecord.resetConvergence();
						}
						return false;
					}, 
					(component, breakLoop) -> {
						/* ------------------------------------------------------------------------------ */
						GenerationRecord<Chr, FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "convergence = {}"), geneRecord.getConvergenceCount());
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						ProcedureUtils.Statistics.IterationInfos
									.updateConvergenceInfo(
										statistics.getData(), geneRecord.getConvergenceCount()
									);
						//	[STATISTIC_OPTIMIZATION_EXIT_REASON]
						if (breakLoop) {
							statistics.put(
								StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON,
								ExitInfo.EXIT_MARK_CONVERGENCE
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if break for reaching max convergence"),
			// 5. Crossing-overs.
			new TimeCountedProcedureComponent<Chr[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "5. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								localParameters.get("chromosomeFitness"),
								getParameters().get(ParameterConstants.PARAMETER_RANDOM_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Fitness<Chr, FValue>[] chromosomeFitness =
								(Fitness<Chr, FValue>[]) parameters[p++];
						Random random =
								(Random) parameters[p++];
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params = 
								(ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue>)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return params.getCrossAlgorithm()
									.crossChromosomes(chromosomeFitness, params, random);
					}, 
					(component, chromosome) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("chromosome", chromosome);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
											
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Crossing-overs"),
			// 6. Mutations.
			new TimeCountedProcedureComponent<Chr[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "6. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get("chromosome"),
								getParameters().get(ParameterConstants.PARAMETER_RANDOM_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Chr[] chromosome =
								(Chr[]) parameters[p++];
						Random random =
								(Random) parameters[p++];
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params = 
								(ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue>)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Perform gene mutations
						for (int i=0; i<chromosome.length; i++) {
							if (chromosome[i]!=null) {
								params.getMutationAlgorithm()
										.mutate(chromosome[i], random);
							}
						}
						return chromosome;
					}, 
					(component, chromosome) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("chromosome", chromosome);
						if (logOn)	LoggerUtil.printLine(log, "-", 70);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
											
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Mutations"),
		};
	}
}