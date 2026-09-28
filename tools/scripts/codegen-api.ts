import { execSync } from 'node:child_process';
import { existsSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';

const ROOT_DIR: string = resolve(import.meta.dirname, '../..');
const SERVER_DIR: string = resolve(ROOT_DIR, 'apps/server');
const OFFLINE_SCHEMA: string = resolve(SERVER_DIR, 'build/openapi.json');
const OUTPUT_FILE: string = resolve(ROOT_DIR, 'apps/client/src/api/generated/api-schema.ts');

if (!existsSync(OFFLINE_SCHEMA)) {
  console.log('\n⏳ [codegen:api] Offline schema not found. Exporting via Gradle OpenApiContractTest...');
  try {
    const gradlewCmd: string = process.platform === 'win32' ? 'gradlew.bat' : './gradlew';
    execSync(`${gradlewCmd} test --tests OpenApiContractTest`, {
      cwd: SERVER_DIR,
      stdio: 'inherit',
    });
  } catch (gradleError: unknown) {
    console.error('\n❌ [codegen:api] Failed to export OpenAPI schema from backend tests.');
    console.error('   Details:', gradleError);
    process.exit(1);
  }
}

try {
  mkdirSync(dirname(OUTPUT_FILE), { recursive: true });
  console.log(`\n⏳ [codegen:api] Generating TypeScript types from: ${OFFLINE_SCHEMA}...`);
  execSync(`pnpm exec openapi-typescript ${OFFLINE_SCHEMA} -o ${OUTPUT_FILE}`, {
    cwd: ROOT_DIR,
    stdio: 'inherit',
  });
  console.log(`✅ [codegen:api] Types successfully generated at: ${OUTPUT_FILE}\n`);
} catch (execError: unknown) {
  console.error('\n❌ [codegen:api] Generation failed while running openapi-typescript CLI.');
  console.error('   Details:', execError);
  process.exit(1);
}
