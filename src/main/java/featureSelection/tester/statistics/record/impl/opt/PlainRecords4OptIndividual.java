package featureSelection.tester.statistics.record.impl.opt;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.RecordFieldInfo;

import java.util.*;

public abstract class PlainRecords4OptIndividual extends PlainRecords4Optimization {

	protected PlainRecord[] records;

	public PlainRecords4OptIndividual(Collection<RecordFieldInfo> titles) {
		super(titles);
	}

	/* ---------------------------------------------------------------------------------- */

	protected abstract void load(
			String summaryID, DatasetInfo datasetInfo, int[] attributes,
			int times, int currentTime, int randomSeed,
			ProcedureParameters parameters, String testName, Statistics statistics,
			Map<String, Long> componentTagTimeMap, ProcedureContainer<?> container
	);

	/* ---------------------------------------------------------------------------------- */

	protected void initReductInfo4Individual(Statistics statistics, String timeID) {
		@SuppressWarnings("unchecked")
		Map<IntArrayKey, Collection<OptimizationReduct>> reductMap =
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP);

		if (reductMap.values().stream().anyMatch(c -> !c.isEmpty())) {
			int totalCandidates =
					reductMap.values().stream().mapToInt(
							c -> (int) c.stream()
									.map(or -> new IntArrayKey(or.getRedsCodingBeforeInspection().getAttributes()))
									.distinct()
									.count()
					).sum();

			records = new PlainRecord[totalCandidates];

			int attrLength, reductID = 0, candidateID = 0;
			long minAttrSize, sumAttrSize, sumRedundantAttrSize;
			for (Map.Entry<IntArrayKey, Collection<OptimizationReduct>> entry : reductMap.entrySet()) {
				minAttrSize = Long.MAX_VALUE;
				sumAttrSize = 0;
				sumRedundantAttrSize = 0;
				for (OptimizationReduct optReduct : entry.getValue()) {
					attrLength = optReduct.getRedsCodingBeforeInspection().getAttributes().length;
					if (attrLength < minAttrSize) minAttrSize = attrLength;
					sumAttrSize += attrLength;
					sumRedundantAttrSize += optReduct.countRedundant();
				}

				reductID++;
				final Collection<IntArrayKey> writtenB4Inspect = new HashSet<>(
						(int) entry.getValue().stream()
								.map(or -> new IntArrayKey(or.getRedsCodingBeforeInspection().getAttributes()))
								.distinct()
								.count()
				);
				for (OptimizationReduct optReduct : entry.getValue()) {
					/* ----------------------------------------------------------------------------------- */
					IntArrayKey key = new IntArrayKey(optReduct.getRedsCodingBeforeInspection().getAttributes());
					if (writtenB4Inspect.contains(key)) continue;
					else writtenB4Inspect.add(key);
					key = null;
					/* ----------------------------------------------------------------------------------- */
					records[candidateID] = new PlainRecord(getTitles());
					/* ----------------------------------------------------------------------------------- */
					// RUN_TIME_ID
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.RUN_TIME_ID,
							() -> timeID,
							null
					);
					// REDUCT_ID
					final int finalReductId = reductID;
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_ID,
							() -> finalReductId,
							null
					);
					// REDUCT_NUMBER
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_NUMBER,
							() -> totalCandidates,
							null
					);
					// REDUCT_DISTINCT_NUMBER
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_DISTINCT_NUMBER,
							() -> reductMap.size(),
							null
					);
					// REDUCT_CODE_NUMBER
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CODE_NUMBER,
							() -> entry.getValue().size(),
							null
					);
					/* ----------------------------------------------------------------------------------- */
					// REDUCT_NO
					final int finalCandidateId = candidateID;
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_NO,
							() -> finalCandidateId + 1,
							null
					);
					// SUM_REDUCT_CANDIDATE_NUMBER
					final long finalSumAttrSize = sumAttrSize;
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_CANDIDATE_NUMBER,
							() -> finalSumAttrSize,
							null
					);
					// SUM_REDUCT_CANDIDATE_REDUNDANT_NUMBER
					final long finalSumRedundantAttrSize = sumRedundantAttrSize;
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_CANDIDATE_HAS_REDUNDANT_NUMBER,
							() -> finalSumRedundantAttrSize,
							null
					);
					/* ----------------------------------------------------------------------------------- */
					// MIN_CANDIDATE_ATTRIBUTE_LENGTH
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.MIN_CANDIDATE_ATTRIBUTE_LENGTH,
							() -> "-",
							null
					);
					// MAX_CANDIDATE_ATTRIBUTE_LENGTH
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.MAX_CANDIDATE_ATTRIBUTE_LENGTH,
							() -> "-",
							null
					);
					// AVG_CANDIDATE_ATTRIBUTE_LENGTH
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.AVG_CANDIDATE_ATTRIBUTE_LENGTH,
							() -> "-",
							null
					);
					// SUM_CANDIDATE_ATTRIBUTE_LENGTH
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_CANDIDATE_ATTRIBUTE_LENGTH,
							() -> "-",
							null
					);
					// MIN_REDUCT_LENGTH
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.MIN_REDUCT_LENGTH,
							() -> "-",
							null
					);
					// MAX_REDUCT_LENGTH
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.MAX_REDUCT_LENGTH,
							() -> "-",
							null
					);
					// AVG_REDUCT_LENGTH
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.AVG_REDUCT_LENGTH,
							() -> "-",
							null
					);
					// SUM_REDUCT_LENGTH
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_LENGTH,
							() -> "-",
							null
					);
					/* ----------------------------------------------------------------------------------- */
					// LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY,
							() -> optReduct.getRedsCodingBeforeInspection().encodedValuesToString(),
							null
					);
					// REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY,
							() -> optReduct.getRedsAfterInspection().size(),
							null
					);
					// REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY
					records[candidateID].set(
							titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY,
							() -> optReduct.getRedsAfterInspection(),
							null
					);
					/* ----------------------------------------------------------------------------------- */
					candidateID++;
				}
			}
		} else {
			records = new PlainRecord[1];

			int candidateID = 0;
			records[candidateID] = new PlainRecord(getTitles());
			/* ----------------------------------------------------------------------------------- */
			// RUN_TIME_ID
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.RUN_TIME_ID,
					() -> timeID,
					null
			);
			// REDUCT_ID
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_ID,
					() -> "-",
					null
			);
			// REDUCT_NUMBER
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_NUMBER,
					() -> reductMap.size(),
					null
			);
			// REDUCT_DISTINCT_NUMBER
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_DISTINCT_NUMBER,
					() -> reductMap.size(),
					null
			);
			// REDUCT_CODE_NUMBER
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CODE_NUMBER,
					() -> "-",
					null
			);
			/* ----------------------------------------------------------------------------------- */
			// REDUCT_NO
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_NO,
					() -> "-",
					null
			);
			/* ----------------------------------------------------------------------------------- */
			// SUM_REDUCT_CANDIDATE_NUMBER
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_CANDIDATE_NUMBER,
					() -> "-",
					null
			);
			// SUM_REDUCT_CANDIDATE_REDUNDANT_NUMBER
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_CANDIDATE_HAS_REDUNDANT_NUMBER,
					() -> "-",
					null
			);
			/* ----------------------------------------------------------------------------------- */
			// MIN_CANDIDATE_ATTRIBUTE_LENGTH
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.MIN_CANDIDATE_ATTRIBUTE_LENGTH,
					() -> "-",
					null
			);
			// MAX_CANDIDATE_ATTRIBUTE_LENGTH
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.MAX_CANDIDATE_ATTRIBUTE_LENGTH,
					() -> "-",
					null
			);
			// AVG_CANDIDATE_ATTRIBUTE_LENGTH
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.AVG_CANDIDATE_ATTRIBUTE_LENGTH,
					() -> "-",
					null
			);
			// SUM_CANDIDATE_ATTRIBUTE_LENGTH
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_CANDIDATE_ATTRIBUTE_LENGTH,
					() -> "-",
					null
			);
			// MIN_REDUCT_LENGTH
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.MIN_REDUCT_LENGTH,
					() -> "-",
					null
			);
			// MAX_REDUCT_LENGTH
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.MAX_REDUCT_LENGTH,
					() -> "-",
					null
			);
			// AVG_REDUCT_LENGTH
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.AVG_REDUCT_LENGTH,
					() -> "-",
					null
			);
			// SUM_REDUCT_LENGTH
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_LENGTH,
					() -> "-",
					null
			);
			/* ----------------------------------------------------------------------------------- */
			// LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY,
					() -> "-",
					null
			);
			// REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY,
					() -> "-",
					null
			);
			// REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY
			records[candidateID].set(
					titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY,
					() -> "-",
					null
			);
			/* ----------------------------------------------------------------------------------- */
		}
	}

	/* ---------------------------------------------------------------------------------- */

}
