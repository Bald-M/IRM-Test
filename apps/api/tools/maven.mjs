import { spawn } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const projectDirectory = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const workspaceEnvironmentFile = path.resolve(projectDirectory, '../..', '.env')
const inheritedEnvironment = { ...process.env }

try {
  process.loadEnvFile(workspaceEnvironmentFile)
} catch (error) {
  if (error?.code !== 'ENOENT') {
    throw error
  }
}
Object.assign(process.env, inheritedEnvironment)

const wrapper = path.join(projectDirectory, process.platform === 'win32' ? 'mvnw.cmd' : 'mvnw')
const requestedArguments = process.argv.slice(2)

const command = process.platform === 'win32' ? 'cmd.exe' : wrapper
const commandArguments = process.platform === 'win32'
  ? ['/d', '/s', '/c', wrapper, ...requestedArguments]
  : requestedArguments

const child = spawn(command, commandArguments, {
  cwd: projectDirectory,
  env: process.env,
  stdio: 'inherit',
})

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => child.kill(signal))
}

child.once('error', (error) => {
  console.error(`Unable to start Maven Wrapper: ${error.message}`)
  process.exitCode = 1
})

child.once('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal)
  } else {
    process.exitCode = code ?? 1
  }
})
