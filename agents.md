# agents.md - Multi-Agent Coordination for Hyperledger Besu

## <roles>
### Primary Agent Roles

| Role | Scope | Responsibilities |
|------|-------|-----------------|
| **🎯 Coordinator** | Cross-cutting | Task delegation, progress tracking, decision escalation |
| **⚙️ Builder** | Build/CI | Gradle builds, dependency management, releases |
| **🔌 API Developer** | ethereum/api | JSON-RPC, GraphQL, Web3 API implementations |
| **⛏️ Core Developer** | ethereum/core | EVM, state, transactions, chain logic |
| **🤝 Consensus Expert** | consensus/ | IBFT/QBFT/Clique implementations |
| **🌐 Network Specialist** | ethereum/p2p | Networking, protocol handlers, discovery |
| **🔒 Security Reviewer** | crypto/, security | Cryptography, access control, vulnerabilities |
| **📊 DevOps Engineer** | CI/CD, metrics | Testing infrastructure, monitoring, deployment |
| **📚 Documentation** | All modules | README, API docs, troubleshooting guides |
</roles>

## <delegation>
### Task Routing Matrix

| Task Type | Primary Agent | Secondary | Escalate If |
|-----------|---------------|-----------|-------------|
| **API endpoint** | API Developer | Core Developer | EVM integration needed |
| **Consensus bug** | Consensus Expert | Security Reviewer | Protocol change required |
| **Build failure** | Builder | DevOps Engineer | Infrastructure issue |
| **Performance issue** | Core Developer | Network Specialist | P2P bottleneck |
| **Security audit** | Security Reviewer | Consensus Expert | Consensus vulnerability |
| **Plugin system** | API Developer | Core Developer | Core API changes |
| **Network protocol** | Network Specialist | Consensus Expert | Consensus networking |

### Delegation Protocol
1. **Assess complexity:** Simple (single agent) vs Complex (multi-agent)
2. **Check boundaries:** Forbidden zones require Security Reviewer approval
3. **Parallel work:** Independent modules can work concurrently
4. **Dependencies:** Chain tasks with clear handoff points
5. **Review gates:** All consensus/crypto changes need security review

### Communication Patterns
```
Coordinator → TaskAgent: {task, context, success_criteria}
TaskAgent → Coordinator: {progress_update, blockers, completion}
TaskAgent → ExpertAgent: {consultation, code_review}
ExpertAgent → TaskAgent: {feedback, approval, concerns}
```
</delegation>

## <lifecycle>
### Task Execution Phases

#### Phase 1: Planning
- **Coordinator** analyzes request, identifies modules affected
- **Risk assessment:** Safe/Dangerous/Forbidden zone classification  
- **Agent assignment:** Primary + Secondary based on expertise
- **Parallel opportunities:** Identify independent work streams

#### Phase 2: Execution
- **Primary agent** leads implementation
- **Progress updates** every 20 minutes for complex tasks
- **Blocker escalation** immediately to Coordinator
- **Cross-consultation** with secondary agents as needed

#### Phase 3: Review
- **Security gate:** All crypto/consensus changes → Security Reviewer
- **Code quality:** Spotless formatting, test coverage
- **Integration test:** AcceptanceTests for feature changes
- **Documentation:** Update relevant docs if APIs changed

#### Phase 4: Completion
- **Delivery:** Working code with tests
- **Handoff:** Summary of changes, risks, next steps
- **Cleanup:** Remove temporary files, update context
</lifecycle>

## <parallelization>
### Safe Parallel Work

| Module Group | Can Work Concurrently | Shared Dependencies |
|--------------|----------------------|-------------------|
| **API modules** | ✅ JSON-RPC, GraphQL, metrics | `ethereum/core` interfaces |
| **Consensus algorithms** | ✅ IBFT, QBFT, Clique | `consensus/common` |
| **Services** | ✅ kvstore, pipeline, tasks | Minimal overlap |
| **Testing** | ✅ Unit, acceptance, reference | Test utilities |
| **Documentation** | ✅ All docs | None |

### Coordination Required
- **Core + API:** Core interface changes affect all API modules
- **Consensus + P2P:** Networking changes affect consensus protocols  
- **Crypto + Everything:** Cryptographic changes ripple everywhere

### Merge Conflicts
- **High risk:** `build.gradle`, core interfaces, shared utilities
- **Medium risk:** Common configuration, shared constants
- **Low risk:** Module-specific implementations, tests, docs

### Work Isolation Strategy
```bash
# Feature branches for major changes
git checkout -b feature/agent-coordinator/new-api
git checkout -b feature/agent-builder/gradle-update

# Module-specific work
./gradlew :ethereum:api:test          # API agent tests
./gradlew :consensus:qbft:test        # Consensus agent tests

# Parallel builds (if resources allow)
./gradlew compileJava --parallel      # Gradle parallel execution
```
</parallelization>

## <escalation>
### Decision Authority

| Level | Agent Type | Authority | Examples |
|-------|------------|-----------|----------|
| **L1** | Specialist | Module changes | New API method, test case |
| **L2** | Expert | Cross-module impact | Protocol change, interface modification |
| **L3** | Security Reviewer | Security implications | Crypto algorithm, consensus rule |
| **L4** | Coordinator | Architecture decisions | Major refactoring, module boundaries |
| **L5** | Human** | Business decisions | Network upgrade, breaking changes |

### Escalation Triggers
🟢 **Continue:** Documentation, tests, safe configuration changes
🟡 **Consult Expert:** Cross-module dependencies, performance impact  
🔴 **Security Review:** Crypto, consensus, state management, networking protocol
⛔ **Human Required:** Breaking API changes, security vulnerabilities, consensus forks

### Quick Decisions (< 5 min)
- Code formatting and style
- Adding logging statements  
- Writing unit tests
- Documentation updates
- Safe configuration options

### Expert Consultation (< 15 min)  
- API interface changes
- Performance optimizations
- Build system updates
- Integration test strategies

### Security Review (< 30 min)
- Cryptographic operations
- Consensus logic changes
- State transition modifications
- Network protocol updates

### Human Escalation (immediate)
- Security vulnerabilities
- Breaking changes to public APIs
- Consensus protocol modifications
- Legal/compliance concerns
</escalation>

## <context-sharing>
### Shared Knowledge Base
- **CLAUDE.md:** Architecture overview, build commands, boundaries
- **Module README files:** Specific implementation details
- **Test results:** Recent build status, failing tests
- **Work log:** Current tasks, completed work, blockers

### Information Flow
```
Coordinator maintains: Global task status, agent assignments, blockers
Specialists maintain: Module-specific knowledge, implementation details  
Security maintains: Threat model, vulnerability assessments
DevOps maintains: Build health, performance metrics, deployment status
```

### Knowledge Transfer
- **Handoff documentation:** When switching agents mid-task
- **Decision logs:** Why certain approaches were chosen/rejected
- **Gotcha lists:** Common pitfalls and their solutions
- **Contact points:** When human expertise is needed

### Coordination Tools
```bash
# Task status tracking
echo "Status: API agent working on JSON-RPC module" > .agents/status
echo "Blocker: Need consensus expert review" >> .agents/status

# Module ownership
echo "ethereum/api: agent-api-dev" > .agents/ownership
echo "consensus/qbft: agent-consensus-expert" >> .agents/ownership

# Progress logging  
git log --oneline > .agents/progress
echo "$(date): Completed API endpoint /eth/blockNumber" >> .agents/log
```
</context-sharing>

## <safety-protocol>
### Code Review Gates
1. **All changes:** Automated tests must pass
2. **API changes:** Secondary agent review required
3. **Core changes:** Expert + Security review required
4. **Crypto/Consensus:** Security Reviewer + Human approval required

### Rollback Strategy
```bash
# Safe rollback points
git tag agent-safe-point-$(date +%s)    # Mark safe state
git reset --hard agent-safe-point-123   # Rollback if needed

# Module isolation
./gradlew :module:clean :module:test    # Test single module
git checkout HEAD -- module/           # Rollback single module
```

### Risk Mitigation
- **Incremental changes:** Small, testable commits
- **Feature flags:** New functionality behind toggles
- **Backward compatibility:** Preserve existing APIs
- **Test coverage:** New code must have tests
- **Documentation:** Update docs with changes

### Emergency Procedures
🚨 **Build broken:** Immediately escalate to Builder + DevOps
🚨 **Security issue:** Stop work, escalate to Security Reviewer + Human  
🚨 **Data corruption:** Stop all agents, escalate to Human
🚨 **Network split:** Consensus Expert + Security Reviewer + Human
</safety-protocol>