// poc-party's project.ts. install.sh creates .sandcastle/project.ts from the example once;
// replace its contents with this. Only this file differs from the fork's setup.
export const project = {
  repo: "frehen-ai/poc-party-gpm-ai-2", // GH_REPO for gh, on the Mac and in the VM
  name: "loop-poc-party", // VM name prefix: loop-poc-party-<8 hex>
  stack: "jdk25" as const, // frehen/sbx-jdk25:stable: Temurin 25, the VM's own dockerd for Testcontainers
  ghKeychainItem: "gh-frehen-ai-poc-party-gpm-ai-2", // the project's fine-grained PAT
  // Once per VM, no timeout. A cold run downloads Gradle, the dependencies and postgres:15.3
  // (~80 s), and proves the base is green before any agent starts. Afterwards the agent's
  // own test runs take ~5 s instead of brushing against its 2-minute shell timeout.
  setup: ["./gradlew clean test"],
  check: "./gradlew clean test", // must pass after every agent step
  models: {
    implement: "github-copilot/claude-opus-5.5#medium", // OpenCode v2 wants model#variant
    review: "github-copilot/gpt-6.1-sol",
    cheap: "github-copilot/claude-haiku-5.5", // smoke and drill
  },
};
