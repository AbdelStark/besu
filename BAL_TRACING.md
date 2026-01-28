# EIP-7928 Block-Level Access Lists OpenTelemetry Tracing

This document describes the OpenTelemetry tracing implementation for EIP-7928 Block-Level Access Lists (BAL) in Besu.

## Overview

The implementation provides comprehensive OpenTelemetry tracing for BAL as specified in the EIP-7928 BAL OTel specification, including:

- Structured span hierarchy for block processing
- Counter and histogram metrics
- Configurable tracing levels
- Performance-optimized implementation

## Span Hierarchy

```
ethereum.block
├── ethereum.bal.prefetch
│   ├── ethereum.bal.prefetch.account (optional)
│   └── ethereum.bal.prefetch.slot (optional)
├── ethereum.tx.execute (per transaction)
└── ethereum.stateroot
```

## Key Components

### BalOtelTracer
Main tracer class that manages the complete span hierarchy. Integrates with `AbstractBlockProcessor` to provide tracing for block processing.

### BalMetrics
Implements all counter and histogram metrics as defined in sections 6.2 and 6.3 of the specification:

**Counters:**
- `ethereum.blocks.total`
- `ethereum.tx.total`
- `ethereum.bal.blocks.total`
- `ethereum.bal.prefetch.accounts`
- `ethereum.bal.prefetch.slots`
- `ethereum.bal.prefetch.cache_hits`
- `ethereum.bal.prefetch.cache_misses`

**Histograms:**
- `ethereum.block.duration`
- `ethereum.tx.duration`
- `ethereum.stateroot.duration`
- `ethereum.throughput.mgas_per_sec`
- `ethereum.bal.prefetch.duration`
- `ethereum.bal.size`

### BalPrefetchTracer
Specialized tracer for BAL prefetch operations with optional per-account and per-slot child spans.

### BalTracingConfig
Configuration class for enabling/disabling BAL tracing and setting OTLP endpoints.

## Usage

### Basic Configuration

```java
// Enable BAL tracing with default settings
BalTracingConfig config = BalTracingConfig.defaultEnabled();

// Create tracer
BalOtelTracer balTracer = new BalOtelTracer(
    openTelemetryTracer,
    metricsSystem,
    chainId,
    config.isEnabled(),
    config.isDetailedTracingEnabled()
);
```

### Integration with Block Processing

The tracing is automatically integrated into `AbstractBlockProcessor`. To enable it, pass a `BalOtelTracer` instance to the constructor:

```java
AbstractBlockProcessor processor = new MainnetBlockProcessor(
    transactionProcessor,
    transactionReceiptFactory,
    blockReward,
    miningBeneficiaryCalculator,
    skipZeroBlockRewards,
    gasBudgetCalculator,
    balTracer  // Optional - pass null to disable BAL tracing
);
```

### Configuration Options

```java
// Disabled tracing
BalTracingConfig disabled = BalTracingConfig.disabled();

// Detailed tracing with per-account/slot spans
BalTracingConfig detailed = BalTracingConfig.detailedEnabled("production");

// Custom configuration
BalTracingConfig custom = new BalTracingConfig(
    true,           // enabled
    false,          // detailed tracing
    "localhost:4318", // OTLP endpoint
    0.1,            // sampling rate (10%)
    "staging"       // environment
);
```

## Performance Characteristics

The implementation is designed to meet the performance requirements specified in section 7:

- **Tracing disabled overhead:** < 0.1% (null checks and early returns)
- **Tracing enabled overhead:** < 2% (lazy attribute setting, minimal span creation)
- **Per-span creation:** < 1μs (efficient OpenTelemetry usage)

## Resource Attributes

All spans include the following resource attributes as per section 4:

- `service.name`: "besu"
- `service.version`: Current Besu version
- `deployment.environment`: Configured environment
- `ethereum.chain.id`: Chain ID

## BAL Data Model

The implementation includes placeholder BAL data model classes:

### BlockAccessList
Represents a complete BAL for a block with:
- Block hash
- Map of addresses to access list entries
- Size in bytes
- Utility methods for counts and lookups

### BlockAccessListEntry
Represents individual entries with:
- Storage slot keys accessed
- Code access flag
- Utility methods for access checks

## Future Integration

This tracing implementation is designed to work with the actual EIP-7928 BAL implementation when it becomes available. The `extractBlockAccessList` method in `AbstractBlockProcessor` is a placeholder that should be replaced with actual BAL extraction logic.

## Testing

Comprehensive unit tests are provided for all major components:
- `BalOtelTracerTest`: Tests tracer functionality and span management
- `BalMetricsTest`: Tests metrics recording and timers
- `BlockAccessListTest`: Tests BAL data model

## Configuration Files

The tracing can be enabled through Besu configuration. Future work should include:
- Command-line options for BAL tracing
- Configuration file settings
- Environment variable support

## Monitoring and Observability

When enabled, the tracing provides rich observability into:
- Block processing performance
- Transaction execution timing
- State root calculation performance
- BAL prefetch effectiveness
- Cache hit/miss ratios

This data can be exported to any OpenTelemetry-compatible observability platform (Jaeger, Zipkin, etc.) for analysis and monitoring.