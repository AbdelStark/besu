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

import org.hyperledger.besu.ethereum.bal.BlockAccessList;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.OperationTimer;

import java.util.concurrent.atomic.AtomicBoolean;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

/**
 * Main OpenTelemetry tracer for Block-level Access Lists (BAL) as defined
 * in the EIP-7928 BAL OTel specification.
 *
 * <p>Manages the complete span hierarchy:
 * <pre>
 * ethereum.block
 * ├── ethereum.bal.prefetch
 * │   ├── ethereum.bal.prefetch.account (optional)
 * │   └── ethereum.bal.prefetch.slot (optional)
 * ├── ethereum.tx.execute (per transaction)
 * └── ethereum.stateroot
 * </pre>
 */
public class BalOtelTracer {

  private static final String SERVICE_NAME = "besu";
  private static final String SERVICE_VERSION = "21.1.7-SNAPSHOT"; // Besu version from this fork

  private final Tracer tracer;
  private final BalMetrics metrics;
  private final String chainId;
  private final boolean enabled;
  private final boolean enableDetailedTracing;

  // Current span state
  private Span blockSpan;
  private OperationTimer.TimingContext blockTimer;
  private BalPrefetchTracer prefetchTracer;
  private final AtomicBoolean blockProcessing = new AtomicBoolean(false);

  // Block processing stats
  private long blockStartTime;
  private long totalGasUsed = 0;
  private int transactionCount = 0;

  /**
   * Creates a new BalOtelTracer.
   *
   * @param tracer OpenTelemetry tracer instance
   * @param metricsSystem Metrics system for recording metrics
   * @param chainId Chain ID for resource attributes and metrics labeling
   * @param enabled Whether BAL tracing is enabled
   * @param enableDetailedTracing Whether to enable optional per-account/slot tracing
   */
  public BalOtelTracer(
      final Tracer tracer,
      final MetricsSystem metricsSystem,
      final String chainId,
      final boolean enabled,
      final boolean enableDetailedTracing) {
    this.tracer = tracer;
    this.metrics = new BalMetrics(metricsSystem);
    this.chainId = chainId;
    this.enabled = enabled;
    this.enableDetailedTracing = enableDetailedTracing;
  }

  /**
   * Starts the root ethereum.block span for block processing.
   *
   * @param blockHeader The block header
   * @param bal The block access list (may be null if not available)
   * @return The block span
   */
  public Span startBlockProcessing(final BlockHeader blockHeader, final BlockAccessList bal) {
    if (!enabled || !blockProcessing.compareAndSet(false, true)) {
      return null;
    }

    blockStartTime = System.nanoTime();
    totalGasUsed = 0;
    transactionCount = 0;

    blockSpan =
        tracer
            .spanBuilder("ethereum.block")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute(BalSpanAttributes.SERVICE_NAME, SERVICE_NAME)
            .setAttribute(BalSpanAttributes.SERVICE_VERSION, SERVICE_VERSION)
            .setAttribute(BalSpanAttributes.ETHEREUM_CHAIN_ID, chainId)
            .startSpan();

    // Add BAL-specific attributes if available
    if (bal != null) {
      blockSpan.setAttribute(BalSpanAttributes.BAL_HASH, bal.getBlockHash().toHexString());
      blockSpan.setAttribute(BalSpanAttributes.BAL_ACCOUNTS_COUNT, bal.getAccountsCount());
      blockSpan.setAttribute(
          BalSpanAttributes.BAL_STORAGE_SLOTS_COUNT, bal.getStorageSlotsCount());
      blockSpan.setAttribute(BalSpanAttributes.BAL_CODE_COUNT, bal.getCodeCount());
      blockSpan.setAttribute(BalSpanAttributes.BAL_SIZE_BYTES, bal.getSizeBytes());

      // Record BAL metrics
      metrics.incrementBalBlocksTotal();
      metrics.recordBalSize(chainId, bal.getSizeBytes());
    }

    blockTimer = metrics.startBlockTimer(chainId);
    prefetchTracer =
        new BalPrefetchTracer(tracer, metrics, chainId, enableDetailedTracing);

    return blockSpan;
  }

  /**
   * Starts BAL prefetch tracing.
   *
   * @param bal The block access list to prefetch
   * @return The prefetch tracer instance
   */
  public BalPrefetchTracer startPrefetch(final BlockAccessList bal) {
    if (!enabled || blockSpan == null || bal == null) {
      return null;
    }

    prefetchTracer.startPrefetch(bal, blockSpan);
    return prefetchTracer;
  }

  /**
   * Starts a transaction execution span.
   *
   * @param transaction The transaction being executed
   * @param transactionIndex The transaction index in the block
   * @return The transaction span
   */
  public Span startTransactionExecution(final Transaction transaction, final int transactionIndex) {
    if (!enabled || blockSpan == null) {
      return null;
    }

    transactionCount++;

    final Span txSpan =
        tracer
            .spanBuilder("ethereum.tx.execute")
            .setParent(io.opentelemetry.context.Context.current().with(blockSpan))
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute(BalSpanAttributes.TX_INDEX, transactionIndex)
            .setAttribute(BalSpanAttributes.TX_HASH, transaction.getHash().toHexString())
            .startSpan();

    metrics.startTxTimer(chainId);
    return txSpan;
  }

  /**
   * Finishes a transaction execution span.
   *
   * @param txSpan The transaction span to finish
   * @param gasUsed Gas used by the transaction
   * @param success Whether the transaction was successful
   */
  public void finishTransactionExecution(final Span txSpan, final long gasUsed, final boolean success) {
    if (txSpan == null) {
      return;
    }

    try {
      txSpan.setAttribute(BalSpanAttributes.TX_GAS_USED, gasUsed);
      if (!success) {
        txSpan.setStatus(StatusCode.ERROR, "Transaction execution failed");
      }

      totalGasUsed += gasUsed;
      metrics.incrementTxTotal();

    } finally {
      txSpan.end();
    }
  }

  /**
   * Starts a state root calculation span.
   *
   * @param accountsUpdated Number of accounts updated
   * @param storageSlotsUpdated Number of storage slots updated
   * @param parallel Whether state root calculation was done in parallel
   * @return The state root span
   */
  public Span startStateRootCalculation(
      final int accountsUpdated, final int storageSlotsUpdated, final boolean parallel) {
    if (!enabled || blockSpan == null) {
      return null;
    }

    final Span stateRootSpan =
        tracer
            .spanBuilder("ethereum.stateroot")
            .setParent(io.opentelemetry.context.Context.current().with(blockSpan))
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute(BalSpanAttributes.ACCOUNTS_UPDATED, accountsUpdated)
            .setAttribute(BalSpanAttributes.STORAGE_SLOTS_UPDATED, storageSlotsUpdated)
            .setAttribute(BalSpanAttributes.BAL_PARALLEL, parallel)
            .startSpan();

    metrics.startStateRootTimer(chainId);
    return stateRootSpan;
  }

  /**
   * Finishes a state root calculation span.
   *
   * @param stateRootSpan The state root span to finish
   * @param stateRoot The calculated state root hash
   */
  public void finishStateRootCalculation(final Span stateRootSpan, final Hash stateRoot) {
    if (stateRootSpan == null) {
      return;
    }

    try {
      stateRootSpan.setAttribute("state.root", stateRoot.toHexString());
    } finally {
      stateRootSpan.end();
    }
  }

  /** Finishes block processing and records final metrics. */
  public void finishBlockProcessing() {
    if (!enabled || blockSpan == null) {
      return;
    }

    try {
      // Finish prefetch tracing if active
      if (prefetchTracer != null) {
        prefetchTracer.finishPrefetch();
      }

      // Calculate and record throughput
      final long blockDurationNanos = System.nanoTime() - blockStartTime;
      final double blockDurationSeconds = blockDurationNanos / 1_000_000_000.0;
      final double mgasPerSec = blockDurationSeconds > 0 
          ? (totalGasUsed / 1_000_000.0) / blockDurationSeconds 
          : 0;

      metrics.recordThroughput(chainId, mgasPerSec);
      metrics.incrementBlocksTotal();

    } finally {
      blockSpan.end();
      if (blockTimer != null) {
        blockTimer.stop();
      }
      blockProcessing.set(false);
    }
  }

  /**
   * Marks block processing as failed and finishes the span with an error status.
   *
   * @param reason The failure reason
   */
  public void failBlockProcessing(final String reason) {
    if (blockSpan != null) {
      blockSpan.setStatus(StatusCode.ERROR, reason);
      finishBlockProcessing();
    }
  }

  /** @return Whether BAL tracing is currently enabled */
  public boolean isEnabled() {
    return enabled;
  }

  /** @return Whether detailed per-account/slot tracing is enabled */
  public boolean isDetailedTracingEnabled() {
    return enableDetailedTracing;
  }

  /** @return The current chain ID */
  public String getChainId() {
    return chainId;
  }

  /** @return Whether a block is currently being processed */
  public boolean isBlockProcessing() {
    return blockProcessing.get();
  }
}