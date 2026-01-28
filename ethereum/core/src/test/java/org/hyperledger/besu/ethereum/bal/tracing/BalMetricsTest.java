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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.Counter;
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric;
import org.hyperledger.besu.plugin.services.metrics.OperationTimer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BalMetricsTest {

  @Mock private MetricsSystem mockMetricsSystem;
  @Mock private LabelledMetric<Counter> mockCounterMetric;
  @Mock private LabelledMetric<OperationTimer> mockTimerMetric;
  @Mock private Counter mockCounter;
  @Mock private OperationTimer mockTimer;
  @Mock private OperationTimer.TimingContext mockTimingContext;

  private BalMetrics balMetrics;
  private static final String CHAIN_ID = "1337";

  @Before
  public void setUp() {
    when(mockMetricsSystem.createLabelledCounter(any(), anyString(), anyString(), anyString()))
        .thenReturn(mockCounterMetric);
    when(mockMetricsSystem.createLabelledTimer(any(), anyString(), anyString(), anyString()))
        .thenReturn(mockTimerMetric);
    when(mockCounterMetric.labels(anyString())).thenReturn(mockCounter);
    when(mockTimerMetric.labels(anyString())).thenReturn(mockTimer);
    when(mockTimer.startTimer()).thenReturn(mockTimingContext);

    balMetrics = new BalMetrics(mockMetricsSystem);
  }

  @Test
  public void shouldIncrementBlocksTotal() {
    balMetrics.incrementBlocksTotal();
    verify(mockCounter).inc();
  }

  @Test
  public void shouldIncrementTxTotal() {
    balMetrics.incrementTxTotal();
    verify(mockCounter).inc();
  }

  @Test
  public void shouldIncrementBalBlocksTotal() {
    balMetrics.incrementBalBlocksTotal();
    verify(mockCounter).inc();
  }

  @Test
  public void shouldIncrementBalPrefetchAccounts() {
    balMetrics.incrementBalPrefetchAccounts(5L);
    verify(mockCounter).inc(5L);
  }

  @Test
  public void shouldIncrementBalPrefetchSlots() {
    balMetrics.incrementBalPrefetchSlots(10L);
    verify(mockCounter).inc(10L);
  }

  @Test
  public void shouldIncrementBalPrefetchCacheHits() {
    balMetrics.incrementBalPrefetchCacheHits(3L);
    verify(mockCounter).inc(3L);
  }

  @Test
  public void shouldIncrementBalPrefetchCacheMisses() {
    balMetrics.incrementBalPrefetchCacheMisses(2L);
    verify(mockCounter).inc(2L);
  }

  @Test
  public void shouldStartBlockTimer() {
    final OperationTimer.TimingContext context = balMetrics.startBlockTimer(CHAIN_ID);
    
    assertThat(context).isEqualTo(mockTimingContext);
    verify(mockTimer).startTimer();
  }

  @Test
  public void shouldStartTxTimer() {
    final OperationTimer.TimingContext context = balMetrics.startTxTimer(CHAIN_ID);
    
    assertThat(context).isEqualTo(mockTimingContext);
    verify(mockTimer).startTimer();
  }

  @Test
  public void shouldStartStateRootTimer() {
    final OperationTimer.TimingContext context = balMetrics.startStateRootTimer(CHAIN_ID);
    
    assertThat(context).isEqualTo(mockTimingContext);
    verify(mockTimer).startTimer();
  }

  @Test
  public void shouldRecordThroughput() {
    balMetrics.recordThroughput(CHAIN_ID, 2.5);
    
    verify(mockTimer).observeDuration(2500.0, "ms");
  }

  @Test
  public void shouldStartBalPrefetchTimer() {
    final OperationTimer.TimingContext context = balMetrics.startBalPrefetchTimer(CHAIN_ID);
    
    assertThat(context).isEqualTo(mockTimingContext);
    verify(mockTimer).startTimer();
  }

  @Test
  public void shouldRecordBalSize() {
    balMetrics.recordBalSize(CHAIN_ID, 2048L);
    
    verify(mockTimer).observeDuration(2.048, "kb");
  }
}