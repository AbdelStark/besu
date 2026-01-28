# CLAUDE.md - Hyperledger Besu Agentic Context

## <identity>
**Project:** Hyperledger Besu - Enterprise Ethereum client in Java (Fork)  
**Type:** Production blockchain client (MainNet compatible)  
**Scale:** 47 modules, ~500k LoC, mission-critical financial infrastructure  
**Complexity:** High - consensus, P2P networking, EVM, cryptography, enterprise features  
**Fork Status:** Based on Besu 21.1.7-SNAPSHOT, may have build configuration differences
</identity>

## <stack>
| Component | Technology | Version | Purpose |
|-----------|------------|---------|----------|
| **Language** | Java | 11+ | Core implementation |
| **Build** | Gradle | 6.8.3 | Build automation, multi-module |
| **Testing** | JUnit 5 | Latest | Unit tests |
| **Testing** | AcceptanceTests | Custom | Integration/E2E |
| **Crypto** | Bouncy Castle | Latest | Cryptographic operations |
| **Storage** | RocksDB | Plugin | State/chain storage |
| **Networking** | Netty | Latest | P2P networking |
| **JSON-RPC** | JSON-RPC 2.0 | Standard | Ethereum API |
| **CLI** | PicoCLI | Latest | Command-line interface |
| **Metrics** | Micrometer | Latest | Observability |
| **Consensus** | IBFT/QBFT/Clique | Custom | Byzantine fault tolerance |
</stack>

## <structure>
```
besu/                          # Main CLI application
├── src/main/java/.../cli/     # Command-line interface, config
ethereum/                      # Core Ethereum implementation
├── api/                       # JSON-RPC, GraphQL, Web3 APIs
├── blockcreation/             # Block production logic
├── core/                      # State, chain, transactions, EVM
├── eth/                       # Ethereum protocol handlers
├── p2p/                       # Peer-to-peer networking
├── permissioning/             # Access control (enterprise)
├── trie/                      # Merkle Patricia Trie
consensus/                     # Consensus mechanisms
├── clique/                    # Proof-of-Authority
├── ibft/                      # Istanbul BFT (legacy)
├── qbft/                      # QBFT (current BFT)
├── common/                    # Shared consensus logic
crypto/                        # Cryptographic primitives
config/                        # Genesis, network configurations
plugin-api/                    # Plugin system interface
plugins/                       # Plugin implementations
services/                      # Infrastructure services
├── kvstore/                   # Key-value storage abstraction
├── pipeline/                  # Processing pipelines
├── tasks/                     # Background tasks
```

**Module Dependencies:** Layered architecture, `ethereum/core` is foundation
</structure>

## <conventions>
### Code Style
- **Formatting:** Google Java Style (Spotless enforced)
- **Structure:** Package-private for internal APIs, public for plugin interfaces
- **Naming:** `BesuController`, `EthProtocolManager` - descriptive, consistent prefixes
- **Immutables:** Prefer immutable objects, builder patterns for complex config

### Architecture Patterns
| Pattern | Usage | Examples |
|---------|-------|----------|
| **Plugin System** | Extensibility | `BesuPlugin`, service discovery via `ServiceLoader` |
| **Builder Pattern** | Complex objects | `BesuController.Builder`, configuration objects |
| **Event System** | Decoupling | `BesuEvents`, block/transaction notifications |
| **Command Pattern** | CLI operations | `BesuCommand`, subcommands for different operations |
| **Strategy Pattern** | Consensus | Different consensus mechanisms as strategies |

### Error Handling
- **Runtime exceptions:** For programming errors, invalid state
- **Checked exceptions:** For recoverable I/O, network errors  
- **Result types:** For operations that commonly fail
- **Graceful degradation:** Continue operating with degraded functionality when possible
</conventions>

## <commands>
### Build System
```bash
# Environment check
java -version                  # Must be Java 11+
./gradlew --version           # Verify Gradle wrapper

# Build & Test
./gradlew build               # Full build with tests
./gradlew compileJava        # Compile only (faster iteration)
./gradlew test               # Unit tests only
./gradlew acceptanceTest     # Integration tests (slow)
./gradlew spotlessApply      # Fix code formatting
./gradlew spotlessCheck      # Check formatting

# Development
./gradlew installDist        # Create distribution
./gradlew run --args="--help" # Run from source
./gradlew :besu:run --args="--network=dev --miner-enabled" # Dev network

# Docker
docker build -t besu .       # Build container
docker run besu --help      # Run containerized

# Quality
./gradlew checkLicenses      # Verify Apache 2.0 headers
./gradlew javadoc           # Generate documentation
```

### Runtime Operations
```bash
# Network configs (built-in)
--network=mainnet           # Ethereum mainnet
--network=goerli            # Testnet
--network=dev               # Local dev chain

# Common flags
--data-path=/data/besu      # Data directory
--rpc-http-enabled          # Enable JSON-RPC
--rpc-ws-enabled           # Enable WebSocket
--metrics-enabled          # Enable Prometheus metrics
--logging=DEBUG            # Verbose logging

# Consensus (for private networks)
--consensus-mechanism=ibft2 # Istanbul BFT
--consensus-mechanism=qbft  # QBFT (recommended)
--consensus-mechanism=clique # Proof-of-Authority
```

**⚠️ Commands verified on Ubuntu with OpenJDK 11**  
**🔧 Note:** Some build commands may fail due to JFrog repository issues in this fork. See troubleshooting section for workarounds.
</commands>

## <workflows>
### Development Workflow
1. **Setup:** `./gradlew compileJava` to verify build works
2. **Changes:** Make targeted changes in specific modules
3. **Test:** `./gradlew :module:test` for affected module
4. **Format:** `./gradlew spotlessApply` before commit
5. **Integration:** `./gradlew acceptanceTest` for major changes

### Testing Strategy
| Test Type | Location | Purpose | Speed |
|-----------|----------|---------|-------|
| **Unit** | `src/test/` | Class/method isolation | Fast |
| **Integration** | `acceptance-tests/` | Component interaction | Medium |
| **Reference** | `ethereum/referencetests/` | Ethereum spec compliance | Slow |
| **Container** | `container-tests/` | Docker integration | Slow |

### Release Process
1. **Version:** Update `gradle.properties`
2. **Build:** `./gradlew build checkLicenses javadoc`
3. **Distribution:** `./gradlew installDist`
4. **Artifacts:** Binaries generated in `build/distributions/`
</workflows>

## <boundaries>
### Safe Operations ✅
- **Configuration changes:** Network configs, feature flags
- **API extensions:** New JSON-RPC methods, metrics
- **Plugin development:** Implementing `BesuPlugin` interface
- **Documentation:** README, code comments, Javadoc
- **Testing:** New test cases, test utilities
- **Logging:** Add logging statements, adjust levels
- **Monitoring:** New metrics, dashboards

### Dangerous Operations ⚠️
- **Consensus changes:** Core consensus logic, block validation
- **Cryptography:** Key management, signature validation
- **State management:** Database schemas, state transitions
- **P2P networking:** Protocol changes, message formats
- **EVM implementation:** Bytecode execution, gas calculations

### Forbidden Files 🚫
```
ethereum/core/src/main/java/.../mainnet/       # MainNet protocol specs
consensus/*/src/main/java/.../protocol/        # Consensus protocols  
crypto/src/main/java/.../crypto/               # Cryptographic primitives
ethereum/core/src/main/java/.../worldstate/   # State management
ethereum/p2p/src/main/java/.../rlpx/          # Network protocol
```

### Enterprise Considerations
- **Permissioning:** Changes affect production access control
- **Privacy:** GoQuorum compatibility, private transactions
- **Performance:** Database tuning affects sync speed
- **Security:** Any crypto/consensus changes need extensive review
</boundaries>

## <troubleshooting>
### Build Issues
```bash
# Java version mismatch
java -version                # Verify Java 11+
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64

# JFrog Artifactory dependency issues (common in forks)
# Error: "Could not find org.jfrog.buildinfo:build-info-extractor:2.22.0"
# Workaround: Comment out artifactory plugin in build.gradle temporarily
sed -i "s/id 'com.jfrog.artifactory'/\/\/id 'com.jfrog.artifactory'/" build.gradle

# Gradle daemon issues  
./gradlew --stop            # Kill daemon
./gradlew clean build       # Fresh build

# Formatting failures
./gradlew spotlessApply     # Auto-fix formatting

# Memory issues (large codebase)
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"

# Network/repository issues
# Add alternative repositories to build.gradle repositories block:
# maven { url "https://repo1.maven.org/maven2" }
# maven { url "https://jcenter.bintray.com" }
```

### Runtime Issues
```bash
# Network connectivity
--discovery-enabled=false   # Disable peer discovery
--bootnodes=enode://...    # Explicit peers

# Sync problems  
--sync-mode=FAST           # Faster initial sync
--pruning-enabled          # Reduce disk usage

# Performance tuning
--Xmx8g                    # Heap size
--data-storage-format=forest # New storage format
```

### Plugin Development
- **ClassPath:** Ensure plugin JARs in `plugins/` directory
- **Services:** Register via `META-INF/services/org.hyperledger.besu.plugin.BesuPlugin`
- **Dependencies:** Avoid conflicts with Besu core dependencies
- **Lifecycle:** Implement proper start/stop handling

### Common Gotchas
- **Genesis config:** Must match between all nodes
- **Network IDs:** Prevent cross-network transactions
- **Key management:** Node keys vs account keys confusion
- **Port conflicts:** Default ports may clash with other services
- **Disk space:** Mainnet requires 100GB+ for full node
</troubleshooting>## Known Issues

### Build Dependency Issue (2024)
- JFrog Artifactory plugin dependency resolution fails
- Error: Could not find org.jfrog.buildinfo:build-info-extractor:2.22.0
- Workaround needed for full builds
- Simple compilation verification: Java 11+ installed, Gradle wrapper works
- Context files created without full build verification
