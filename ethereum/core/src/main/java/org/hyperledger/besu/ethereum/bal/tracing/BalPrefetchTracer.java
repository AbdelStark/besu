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
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.plugin.services.metrics.OperationTimer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

/**
 * Specialized tracer for BAL prefetch operations.
 * Handles the ethereum.bal.prefetch span and optional per-account/slot child spans.
 */
public class BalPrefetchTracer {

  private final Tracer tracer;
  private final BalMetrics metrics;
  private final String chainId;
  private final boolean enableDetailedTracing;

  private Span prefetchSpan;
  private OperationTimer.TimingContext prefetchTimer;
  private long cacheHits = 0;
  private long cacheMisses = 0;
  private long codeBytes = 0;

  /**
   * Creates a new BalPrefetchTracer.
   *
   * @param tracer OpenTelemetry tracer
   * @param metrics BAL metrics instance
   * @param chainId Chain ID for metrics labeling
   * @param enableDetailedTracing Whether to enable optional per-account/slot tracing
   */
  public BalPrefetchTracer(
      final Tracer tracer,
      final BalMetrics metrics,
      final String chainId,
      final boolean enableDetailedTracing) {
    this.tracer = tracer;
    this.metrics = metrics;
    this.chainId = chainId;
    this.enableDetailedTracing = enableDetailedTracing;
  }

  /**
   * Starts the BAL prefetch span with the given BAL.
   *
   * @param bal The block access list being prefetched
   * @param parentSpan The parent span (ethereum.block)
   * @return The prefetch span
   */
  public Span startPrefetch(final BlockAccessList bal, final Span parentSpan) {
    prefetchSpan =
        tracer
            .spanBuilder("ethereum.bal.prefetch")
            .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute(BalSpanAttributes.ACCOUNTS_COUNT, bal.getAccountsCount())
            .setAttribute(BalSpanAttributes.STORAGE_SLOTS_COUNT, bal.getStorageSlotsCount())
            .setAttribute(BalSpanAttributes.CODE_COUNT, bal.getCodeCount())
            .startSpan();

    prefetchTimer = metrics.startBalPrefetchTimer(chainId);

    // Update metrics
    metrics.incrementBalPrefetchAccounts(bal.getAccountsCount());
    metrics.incrementBalPrefetchSlots(bal.getStorageSlotsCount());

    return prefetchSpan;
  }

  /**
   * Records cache hits during prefetch.
   *
   * @param hits Number of cache hits
   */
  public void recordCacheHits(final long hits) {
    cacheHits += hits;
  }

  /**
   * Records cache misses during prefetch.
   *
   * @param misses Number of cache misses
   */
  public void recordCacheMisses(final long misses) {
    cacheMisses += misses;
  }

  /**
   * Records code bytes prefetched.
   *
   * @param bytes Number of bytes of code prefetched
   */
  public void recordCodeBytes(final long bytes) {
    codeBytes += bytes;
  }

  /**
   * Traces prefetching for a specific account (optional detailed tracing).
   * Creates a ethereum.bal.prefetch.account child span if detailed tracing is enabled.
   *
   * @param address The account address being prefetched
   * @return The account prefetch span, or null if detailed tracing is disabled
   */
  public Span traceAccountPrefetch(final Address address) {
    if (!enableDetailedTracing || prefetchSpan == null) {
      return null;
    }

    return tracer
        .spanBuilder("ethereum.bal.prefetch.account")
        .setParent(io.opentelemetry.context.Context.current().with(prefetchSpan))
        .setSpanKind(SpanKind.INTERNAL)
        .setAttribute("account.address", address.toHexString())
        .startSpan();
  }

  /**
   * Traces prefetching for a specific storage slot (optional detailed tracing).
   * Creates a ethereum.bal.prefetch.slot child span if detailed tracing is enabled.
   *
   * @param address The account address
   * @param storageKey The storage slot key
   * @return The slot prefetch span, or null if detailed tracing is disabled
   */
  public Span traceSlotPrefetch(final Address address, final String storageKey) {
    if (!enableDetailedTracing || prefetchSpan == null) {
      return null;
    }

    return tracer
        .spanBuilder("ethereum.bal.prefetch.slot")
        .setParent(io.opentelemetry.context.Context.current().with(prefetchSpan))
        .setSpanKind(SpanKind.INTERNAL)
        .setAttribute("account.address", address.toHexString())
        .setAttribute("storage.key", storageKey)
        .startSpan();
  }

  /** Finishes the prefetch span and records final attributes. */
  public void finishPrefetch() {
    if (prefetchSpan == null) {
      return;
    }

    try {
      // Set final attributes
      prefetchSpan.setAttribute(BalSpanAttributes.CODE_BYTES, codeBytes);
      prefetchSpan.setAttribute(BalSpanAttributes.CACHE_HITS, cacheHits);
      prefetchSpan.setAttribute(BalSpanAttributes.CACHE_MISSES, cacheMisses);

      // Update metrics
      metrics.incrementBalPrefetchCacheHits(cacheHits);
      metrics.incrementBalPrefetchCacheMisses(cacheMisses);

    } finally {
      prefetchSpan.end();
      if (prefetchTimer != null) {
        prefetchTimer.stop();
      }
    }
  }

  /** @return Current number of cache hits recorded */
  public long getCacheHits() {
    return cacheHits;
  }

  /** @return Current number of cache misses recorded */
  public long getCacheMisses() {
    return cacheMisses;
  }

  /** @return Current number of code bytes recorded */
  public long getCodeBytes() {
    return codeBytes;
  }
}