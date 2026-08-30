import { globalIgnores } from 'eslint/config'
import globals from 'globals'
import pluginVue from 'eslint-plugin-vue'
import { withVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import { fileURLToPath, URL } from 'node:url'

const webRoot = fileURLToPath(new URL('./apps/web', import.meta.url))

export default withVueTs(
  { rootDir: webRoot },
  globalIgnores([
    '.nx/**',
    'dist/**',
    'node_modules/**',
    'apps/api/target/**'
  ]),
  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,
  {
    files: ['apps/web/**/*.{ts,vue}'],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node
      }
    },
    rules: {
      '@typescript-eslint/no-unused-vars': [
        'error',
        { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }
      ]
    }
  }
)
