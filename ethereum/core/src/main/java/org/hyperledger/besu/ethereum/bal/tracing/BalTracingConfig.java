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

/**
 * Configuration for Block-level Access List (BAL) OpenTelemetry tracing.
 * Defines settings for enabling/disabling tracing and configuring OTLP endpoint.
 */
public class BalTracingConfig {

  /** Default OTLP endpoint as per specification */
  public static final String DEFAULT_OTLP_ENDPOINT = "localhost:4317";

  /** Default sampling rate (1.0 = sample all) */
  public static final double DEFAULT_SAMPLING_RATE = 1.0;

  private final boolean enabled;
  private final boolean detailedTracingEnabled;
  private final String otlpEndpoint;
  private final double samplingRate;
  private final String deploymentEnvironment;

  /**
   * Creates a new BalTracingConfig.
   *
   * @param enabled Whether BAL tracing is enabled
   * @param detailedTracingEnabled Whether to enable optional per-account/slot child spans
   * @param otlpEndpoint OTLP endpoint for sending traces
   * @param samplingRate Sampling rate (0.0 to 1.0)
   * @param deploymentEnvironment Deployment environment name (e.g., "production", "dev")
   */
  public BalTracingConfig(
      final boolean enabled,
      final boolean detailedTracingEnabled,
      final String otlpEndpoint,
      final double samplingRate,
      final String deploymentEnvironment) {
    this.enabled = enabled;
    this.detailedTracingEnabled = detailedTracingEnabled;
    this.otlpEndpoint = otlpEndpoint;
    this.samplingRate = Math.max(0.0, Math.min(1.0, samplingRate)); // Clamp to [0.0, 1.0]
    this.deploymentEnvironment = deploymentEnvironment;
  }

  /** Creates a disabled BAL tracing configuration. */
  public static BalTracingConfig disabled() {
    return new BalTracingConfig(false, false, DEFAULT_OTLP_ENDPOINT, 0.0, "unknown");
  }

  /** Creates a default enabled BAL tracing configuration. */
  public static BalTracingConfig defaultEnabled() {
    return new BalTracingConfig(
        true, false, DEFAULT_OTLP_ENDPOINT, DEFAULT_SAMPLING_RATE, "development");
  }

  /**
   * Creates a BAL tracing configuration with detailed tracing enabled.
   *
   * @param deploymentEnvironment Deployment environment name
   * @return Configuration with detailed tracing enabled
   */
  public static BalTracingConfig detailedEnabled(final String deploymentEnvironment) {
    return new BalTracingConfig(
        true, true, DEFAULT_OTLP_ENDPOINT, DEFAULT_SAMPLING_RATE, deploymentEnvironment);
  }

  /** @return Whether BAL tracing is enabled */
  public boolean isEnabled() {
    return enabled;
  }

  /** @return Whether detailed per-account/slot tracing is enabled */
  public boolean isDetailedTracingEnabled() {
    return detailedTracingEnabled;
  }

  /** @return OTLP endpoint for sending traces */
  public String getOtlpEndpoint() {
    return otlpEndpoint;
  }

  /** @return Sampling rate (0.0 to 1.0) */
  public double getSamplingRate() {
    return samplingRate;
  }

  /** @return Deployment environment name */
  public String getDeploymentEnvironment() {
    return deploymentEnvironment;
  }

  /**
   * Creates a new configuration with different enabled status.
   *
   * @param enabled New enabled status
   * @return New configuration with updated enabled status
   */
  public BalTracingConfig withEnabled(final boolean enabled) {
    return new BalTracingConfig(
        enabled, detailedTracingEnabled, otlpEndpoint, samplingRate, deploymentEnvironment);
  }

  /**
   * Creates a new configuration with different OTLP endpoint.
   *
   * @param otlpEndpoint New OTLP endpoint
   * @return New configuration with updated endpoint
   */
  public BalTracingConfig withOtlpEndpoint(final String otlpEndpoint) {
    return new BalTracingConfig(
        enabled, detailedTracingEnabled, otlpEndpoint, samplingRate, deploymentEnvironment);
  }

  /**
   * Creates a new configuration with different sampling rate.
   *
   * @param samplingRate New sampling rate (0.0 to 1.0)
   * @return New configuration with updated sampling rate
   */
  public BalTracingConfig withSamplingRate(final double samplingRate) {
    return new BalTracingConfig(
        enabled, detailedTracingEnabled, otlpEndpoint, samplingRate, deploymentEnvironment);
  }

  @Override
  public String toString() {
    return String.format(
        "BalTracingConfig{enabled=%s, detailedTracing=%s, otlpEndpoint='%s', samplingRate=%.2f, environment='%s'}",
        enabled, detailedTracingEnabled, otlpEndpoint, samplingRate, deploymentEnvironment);
  }
}