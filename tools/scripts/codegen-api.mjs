import { execSync } from 'node:child_process';
import { mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';

const DOCS_URL = process.env.API_DOCS_URL || 'http://localhost:8080/v3/api-docs';
const OUTPUT_FILE = resolve(import.meta.dirname, '../../apps/client/src/api/generated/api-schema.ts');

try {
  const response = await fetch(DOCS_URL, { signal: AbortSignal.timeout(3000) });
  if (!response.ok) {
    throw new Error(`Endpoint returned HTTP ${response.status} ${response.statusText}`);
  }
} catch (error) {
  console.error('\n❌ [codegen:api] Error: Unable to reach the Backend OpenAPI endpoint.');
  console.error(`   Target URL: ${DOCS_URL}`);
  console.error('   Reason: The Spring Boot backend server is not running or not responding.');
  console.error('   Details:', error);
  console.error('\n💡 To resolve this:');
  console.error('   1. Start the server in another terminal: pnpm dev:server');
  console.error('   2. Wait until Spring Boot finishes starting (http://localhost:8080/v3/api-docs)');
  console.error('   3. Re-run: pnpm codegen:api\n');
  process.exit(1);
}

try {
  mkdirSync(dirname(OUTPUT_FILE), { recursive: true });
  console.log(`\n⏳ [codegen:api] Fetching schema from ${DOCS_URL}...`);
  execSync(`pnpm exec openapi-typescript ${DOCS_URL} -o ${OUTPUT_FILE}`, { stdio: 'inherit' });
  console.log(`✅ [codegen:api] Types successfully generated at: ${OUTPUT_FILE}\n`);
} catch (execError) {
  console.error('\n❌ [codegen:api] Generation failed while running openapi-typescript CLI.');
  console.error('   Details:', execError);
  process.exit(1);
}
