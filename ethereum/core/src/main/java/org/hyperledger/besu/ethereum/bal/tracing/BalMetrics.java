/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.bal.tracing;

import org.hyperledger.besu.metrics.BesuMetricCategory;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.Counter;
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric;
import org.hyperledger.besu.plugin.services.metrics.OperationTimer;

/**
 * OpenTelemetry metrics for Block-level Access Lists (BAL) as defined
 * in the EIP-7928 BAL OTel specification sections 6.2 and 6.3.
 */
public class BalMetrics {

  // Counter metrics (Section 6.2)
  private final Counter blocksTotal;
  private final Counter txTotal;
  private final Counter balBlocksTotal;
  private final Counter balPrefetchAccounts;
  private final Counter balPrefetchSlots;
  private final Counter balPrefetchCacheHits;
  private final Counter balPrefetchCacheMisses;

  // Histogram metrics (Section 6.3)
  private final LabelledMetric<OperationTimer> blockDuration;
  private final LabelledMetric<OperationTimer> txDuration;
  private final LabelledMetric<OperationTimer> stateRootDuration;
  private final LabelledMetric<OperationTimer> throughputMgasPerSec;
  private final LabelledMetric<OperationTimer> balPrefetchDuration;
  private final LabelledMetric<OperationTimer> balSize;

  /**
   * Creates a new BalMetrics instance.
   *
   * @param metricsSystem The metrics system to register metrics with
   */
  public BalMetrics(final MetricsSystem metricsSystem) {
    // Counter metrics
    blocksTotal =
        metricsSystem
            .createLabelledCounter(
                BesuMetricCategory.ETHEREUM,
                "blocks_total",
                "Total number of blocks processed",
                "chain_id")
            .labels("");

    txTotal =
        metricsSystem
            .createLabelledCounter(
                BesuMetricCategory.ETHEREUM,
                "tx_total",
                "Total number of transactions processed",
                "chain_id")
            .labels("");

    balBlocksTotal =
        metricsSystem
            .createLabelledCounter(
                BesuMetricCategory.ETHEREUM,
                "bal_blocks_total",
                "Total number of BAL-enabled blocks processed",
                "chain_id")
            .labels("");

    balPrefetchAccounts =
        metricsSystem
            .createLabelledCounter(
                BesuMetricCategory.ETHEREUM,
                "bal_prefetch_accounts",
                "Total number of accounts prefetched via BAL",
                "chain_id")
            .labels("");

    balPrefetchSlots =
        metricsSystem
            .createLabelledCounter(
                BesuMetricCategory.ETHEREUM,
                "bal_prefetch_slots",
                "Total number of storage slots prefetched via BAL",
                "chain_id")
            .labels("");

    balPrefetchCacheHits =
        metricsSystem
            .createLabelledCounter(
                BesuMetricCategory.ETHEREUM,
                "bal_prefetch_cache_hits",
                "Total number of BAL prefetch cache hits",
                "chain_id")
            .labels("");

    balPrefetchCacheMisses =
        metricsSystem
            .createLabelledCounter(
                BesuMetricCategory.ETHEREUM,
                "bal_prefetch_cache_misses",
                "Total number of BAL prefetch cache misses",
                "chain_id")
            .labels("");

    // Histogram metrics
    blockDuration =
        metricsSystem.createLabelledTimer(
            BesuMetricCategory.ETHEREUM,
            "block_duration",
            "Block processing time in seconds",
            "chain_id");

    txDuration =
        metricsSystem.createLabelledTimer(
            BesuMetricCategory.ETHEREUM,
            "tx_duration",
            "Transaction processing time in seconds",
            "chain_id");

    stateRootDuration =
        metricsSystem.createLabelledTimer(
            BesuMetricCategory.ETHEREUM,
            "stateroot_duration",
            "State root calculation time in seconds",
            "chain_id");

    throughputMgasPerSec =
        metricsSystem.createLabelledTimer(
            BesuMetricCategory.ETHEREUM,
            "throughput_mgas_per_sec",
            "Gas throughput in millions of gas per second",
            "chain_id");

    balPrefetchDuration =
        metricsSystem.createLabelledTimer(
            BesuMetricCategory.ETHEREUM,
            "bal_prefetch_duration",
            "BAL prefetch phase time in seconds",
            "chain_id");

    balSize =
        metricsSystem.createLabelledTimer(
            BesuMetricCategory.ETHEREUM,
            "bal_size",
            "BAL size in bytes",
            "chain_id");
  }

  /** Increments the total blocks processed counter */
  public void incrementBlocksTotal() {
    blocksTotal.inc();
  }

  /** Increments the total transactions processed counter */
  public void incrementTxTotal() {
    txTotal.inc();
  }

  /** Increments the total BAL-enabled blocks processed counter */
  public void incrementBalBlocksTotal() {
    balBlocksTotal.inc();
  }

  /**
   * Increments the BAL prefetch accounts counter.
   *
   * @param count Number of accounts prefetched
   */
  public void incrementBalPrefetchAccounts(final long count) {
    balPrefetchAccounts.inc(count);
  }

  /**
   * Increments the BAL prefetch slots counter.
   *
   * @param count Number of storage slots prefetched
   */
  public void incrementBalPrefetchSlots(final long count) {
    balPrefetchSlots.inc(count);
  }

  /**
   * Increments the BAL prefetch cache hits counter.
   *
   * @param count Number of cache hits
   */
  public void incrementBalPrefetchCacheHits(final long count) {
    balPrefetchCacheHits.inc(count);
  }

  /**
   * Increments the BAL prefetch cache misses counter.
   *
   * @param count Number of cache misses
   */
  public void incrementBalPrefetchCacheMisses(final long count) {
    balPrefetchCacheMisses.inc(count);
  }

  /**
   * Creates a timer for block duration measurement.
   *
   * @param chainId Chain ID for labeling
   * @return Operation timer for block processing
   */
  public OperationTimer.TimingContext startBlockTimer(final String chainId) {
    return blockDuration.labels(chainId).startTimer();
  }

  /**
   * Creates a timer for transaction duration measurement.
   *
   * @param chainId Chain ID for labeling
   * @return Operation timer for transaction processing
   */
  public OperationTimer.TimingContext startTxTimer(final String chainId) {
    return txDuration.labels(chainId).startTimer();
  }

  /**
   * Creates a timer for state root calculation duration measurement.
   *
   * @param chainId Chain ID for labeling
   * @return Operation timer for state root calculation
   */
  public OperationTimer.TimingContext startStateRootTimer(final String chainId) {
    return stateRootDuration.labels(chainId).startTimer();
  }

  /**
   * Records gas throughput in Mgas/sec.
   *
   * @param chainId Chain ID for labeling
   * @param mgasPerSec Throughput in millions of gas per second
   */
  public void recordThroughput(final String chainId, final double mgasPerSec) {
    throughputMgasPerSec.labels(chainId).observeDuration(mgasPerSec * 1000.0, "ms");
  }

  /**
   * Creates a timer for BAL prefetch duration measurement.
   *
   * @param chainId Chain ID for labeling
   * @return Operation timer for BAL prefetch
   */
  public OperationTimer.TimingContext startBalPrefetchTimer(final String chainId) {
    return balPrefetchDuration.labels(chainId).startTimer();
  }

  /**
   * Records BAL size in bytes.
   *
   * @param chainId Chain ID for labeling
   * @param sizeBytes BAL size in bytes
   */
  public void recordBalSize(final String chainId, final long sizeBytes) {
    balSize.labels(chainId).observeDuration(sizeBytes / 1000.0, "kb");
  }
}