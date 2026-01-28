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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.ethereum.bal.BlockAccessList;
import org.hyperledger.besu.ethereum.bal.BlockAccessListEntry;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.TransactionTestFixture;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.Counter;
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric;
import org.hyperledger.besu.plugin.services.metrics.OperationTimer;

import java.util.List;
import java.util.Map;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BalOtelTracerTest {

  @Mock private Tracer mockTracer;
  @Mock private MetricsSystem mockMetricsSystem;
  @Mock private LabelledMetric<Counter> mockCounterMetric;
  @Mock private LabelledMetric<OperationTimer> mockTimerMetric;
  @Mock private Counter mockCounter;
  @Mock private OperationTimer mockTimer;
  @Mock private OperationTimer.TimingContext mockTimingContext;
  @Mock private SpanBuilder mockSpanBuilder;
  @Mock private Span mockSpan;

  private BalOtelTracer balTracer;
  private BlockHeader blockHeader;
  private BlockAccessList blockAccessList;
  private Transaction transaction;

  private static final String CHAIN_ID = "1337";

  @Before
  public void setUp() {
    // Setup mock metrics
    when(mockMetricsSystem.createLabelledCounter(any(), anyString(), anyString(), anyString()))
        .thenReturn(mockCounterMetric);
    when(mockMetricsSystem.createLabelledTimer(any(), anyString(), anyString(), anyString()))
        .thenReturn(mockTimerMetric);
    when(mockCounterMetric.labels(anyString())).thenReturn(mockCounter);
    when(mockTimerMetric.labels(anyString())).thenReturn(mockTimer);
    when(mockTimer.startTimer()).thenReturn(mockTimingContext);

    // Setup mock tracer
    when(mockTracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
    when(mockSpanBuilder.setSpanKind(any())).thenReturn(mockSpanBuilder);
    when(mockSpanBuilder.setParent(any())).thenReturn(mockSpanBuilder);
    when(mockSpanBuilder.setAttribute(anyString(), anyString())).thenReturn(mockSpanBuilder);
    when(mockSpanBuilder.setAttribute(anyString(), any(Long.class))).thenReturn(mockSpanBuilder);
    when(mockSpanBuilder.setAttribute(anyString(), any(Integer.class))).thenReturn(mockSpanBuilder);
    when(mockSpanBuilder.setAttribute(anyString(), any(Boolean.class))).thenReturn(mockSpanBuilder);
    when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);

    balTracer = new BalOtelTracer(mockTracer, mockMetricsSystem, CHAIN_ID, true, false);

    // Setup test data
    blockHeader = new BlockHeaderTestFixture().number(1).buildHeader();
    
    final Map<Address, BlockAccessListEntry> balEntries = Map.of(
        Address.fromHexString("0x1234567890123456789012345678901234567890"),
        BlockAccessListEntry.codeOnly(true),
        Address.fromHexString("0xabcdefabcdefabcdefabcdefabcdefabcdefabcdef"),
        BlockAccessListEntry.storageOnly(List.of(Hash.ZERO))
    );
    
    blockAccessList = new BlockAccessList(blockHeader.getHash(), balEntries, 1024);
    
    transaction = new TransactionTestFixture().createTransaction();
  }

  @Test
  public void shouldStartBlockProcessingWhenEnabled() {
    final Span span = balTracer.startBlockProcessing(blockHeader, blockAccessList);

    assertThat(span).isEqualTo(mockSpan);
    assertThat(balTracer.isBlockProcessing()).isTrue();
    verify(mockTracer).spanBuilder("ethereum.block");
  }

  @Test
  public void shouldNotStartBlockProcessingWhenDisabled() {
    final BalOtelTracer disabledTracer = new BalOtelTracer(mockTracer, mockMetricsSystem, CHAIN_ID, false, false);
    
    final Span span = disabledTracer.startBlockProcessing(blockHeader, blockAccessList);

    assertThat(span).isNull();
    assertThat(disabledTracer.isBlockProcessing()).isFalse();
    verify(mockTracer, never()).spanBuilder(anyString());
  }

  @Test
  public void shouldStartTransactionExecution() {
    balTracer.startBlockProcessing(blockHeader, blockAccessList);
    
    final Span txSpan = balTracer.startTransactionExecution(transaction, 0);

    assertThat(txSpan).isEqualTo(mockSpan);
    verify(mockTracer).spanBuilder("ethereum.tx.execute");
  }

  @Test
  public void shouldFinishTransactionExecutionSuccessfully() {
    balTracer.startBlockProcessing(blockHeader, blockAccessList);
    final Span txSpan = balTracer.startTransactionExecution(transaction, 0);
    
    balTracer.finishTransactionExecution(txSpan, 21000L, true);

    verify(mockSpan).setAttribute("tx.gas_used", 21000L);
    verify(mockSpan, never()).setStatus(any(), anyString());
    verify(mockSpan).end();
  }

  @Test
  public void shouldFinishTransactionExecutionWithError() {
    balTracer.startBlockProcessing(blockHeader, blockAccessList);
    final Span txSpan = balTracer.startTransactionExecution(transaction, 0);
    
    balTracer.finishTransactionExecution(txSpan, 21000L, false);

    verify(mockSpan).setAttribute("tx.gas_used", 21000L);
    verify(mockSpan).setStatus(any(), anyString());
    verify(mockSpan).end();
  }

  @Test
  public void shouldStartStateRootCalculation() {
    balTracer.startBlockProcessing(blockHeader, blockAccessList);
    
    final Span stateRootSpan = balTracer.startStateRootCalculation(5, 10, false);

    assertThat(stateRootSpan).isEqualTo(mockSpan);
    verify(mockTracer).spanBuilder("ethereum.stateroot");
  }

  @Test
  public void shouldFinishStateRootCalculation() {
    balTracer.startBlockProcessing(blockHeader, blockAccessList);
    final Span stateRootSpan = balTracer.startStateRootCalculation(5, 10, false);
    
    balTracer.finishStateRootCalculation(stateRootSpan, blockHeader.getStateRoot());

    verify(mockSpan).setAttribute("state.root", blockHeader.getStateRoot().toHexString());
    verify(mockSpan).end();
  }

  @Test
  public void shouldFinishBlockProcessing() {
    balTracer.startBlockProcessing(blockHeader, blockAccessList);
    
    balTracer.finishBlockProcessing();

    assertThat(balTracer.isBlockProcessing()).isFalse();
    verify(mockSpan).end();
    verify(mockTimingContext).stop();
  }

  @Test
  public void shouldFailBlockProcessing() {
    balTracer.startBlockProcessing(blockHeader, blockAccessList);
    
    balTracer.failBlockProcessing("Test failure");

    assertThat(balTracer.isBlockProcessing()).isFalse();
    verify(mockSpan).setStatus(any(), "Test failure");
    verify(mockSpan).end();
  }

  @Test
  public void shouldReturnCorrectConfiguration() {
    assertThat(balTracer.isEnabled()).isTrue();
    assertThat(balTracer.isDetailedTracingEnabled()).isFalse();
    assertThat(balTracer.getChainId()).isEqualTo(CHAIN_ID);
  }

  @Test
  public void shouldNotProcessWhenAlreadyProcessing() {
    balTracer.startBlockProcessing(blockHeader, blockAccessList);
    
    final Span secondSpan = balTracer.startBlockProcessing(blockHeader, blockAccessList);

    assertThat(secondSpan).isNull();
  }
}