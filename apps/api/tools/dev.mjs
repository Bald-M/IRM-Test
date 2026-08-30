import { spawn } from 'node:child_process'
import { existsSync, mkdirSync, watch, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const toolsDirectory = path.dirname(fileURLToPath(import.meta.url))
const apiDirectory = path.resolve(toolsDirectory, '..')
const workspaceDirectory = path.resolve(apiDirectory, '../..')
const mavenRunner = path.join(toolsDirectory, 'maven.mjs')
const reloadTriggerFile = path.join(apiDirectory, 'target/classes/.reloadtrigger')
const watchedDirectories = [
  path.join(apiDirectory, 'src/main/java'),
  path.join(apiDirectory, 'src/main/resources'),
]

const childProcesses = new Set()
const watchers = []

let applicationProcess
let compileProcess
let compileQueued = false
let compileTimer
let shuttingDown = false
let shutdownExitCode = 0

function log(message) {
  console.log(`[api-dev] ${message}`)
}

function spawnMaven(arguments_) {
  const child = spawn(process.execPath, [mavenRunner, ...arguments_], {
    cwd: workspaceDirectory,
    env: process.env,
    stdio: 'inherit',
    detached: process.platform !== 'win32',
  })

  childProcesses.add(child)
  child.once('exit', () => {
    childProcesses.delete(child)
    finishShutdownWhenReady()
  })
  child.once('error', (error) => {
    console.error(`[api-dev] Unable to start Maven: ${error.message}`)
    requestShutdown(1)
  })

  return child
}

function stopProcessTree(child, signal) {
  if (!child || child.exitCode !== null || child.signalCode !== null) return

  try {
    if (process.platform === 'win32') {
      child.kill(signal)
    } else {
      process.kill(-child.pid, signal)
    }
  } catch (error) {
    if (error?.code !== 'ESRCH') {
      console.error(`[api-dev] Unable to stop child process ${child.pid}: ${error.message}`)
    }
  }
}

function finishShutdownWhenReady() {
  if (shuttingDown && childProcesses.size === 0) {
    process.exit(shutdownExitCode)
  }
}

function requestShutdown(exitCode) {
  if (shuttingDown) return

  shuttingDown = true
  shutdownExitCode = exitCode
  clearTimeout(compileTimer)

  for (const watcher of watchers) watcher.close()
  for (const child of childProcesses) stopProcessTree(child, 'SIGTERM')

  if (childProcesses.size === 0) {
    process.exit(shutdownExitCode)
  }

  const forceShutdownTimer = setTimeout(() => {
    for (const child of childProcesses) stopProcessTree(child, 'SIGKILL')
    process.exit(shutdownExitCode)
  }, 5_000)
  forceShutdownTimer.unref()
}

function startIncrementalCompile() {
  if (shuttingDown) return

  if (compileProcess) {
    compileQueued = true
    return
  }

  log('Backend source changed; compiling updated classes...')
  compileProcess = spawnMaven(['-q', '-DskipTests', 'compile'])
  const currentCompile = compileProcess

  currentCompile.once('exit', (code, signal) => {
    if (compileProcess === currentCompile) compileProcess = undefined
    if (shuttingDown) return

    if (code === 0) {
      updateReloadTrigger()
      log('Compilation complete; Spring Boot DevTools restart triggered.')
    } else {
      const reason = signal ? `signal ${signal}` : `exit code ${code ?? 1}`
      console.error(`[api-dev] Compilation failed (${reason}); the current API process remains available.`)
    }

    if (compileQueued) {
      compileQueued = false
      startIncrementalCompile()
    }
  })
}

function updateReloadTrigger() {
  mkdirSync(path.dirname(reloadTriggerFile), { recursive: true })
  writeFileSync(reloadTriggerFile, `${Date.now()}\n`, 'utf8')
}

function scheduleCompile(relativeFilename) {
  if (shuttingDown) return

  const filename = relativeFilename?.toString() ?? ''
  const basename = path.basename(filename)
  if (basename.startsWith('.#') || basename.endsWith('~') || /\.sw[a-z]$/.test(basename)) return

  clearTimeout(compileTimer)
  compileTimer = setTimeout(startIncrementalCompile, 250)
}

function startWatchers() {
  for (const directory of watchedDirectories) {
    if (!existsSync(directory)) continue

    const watcher = watch(directory, { recursive: true }, (_eventType, filename) => {
      scheduleCompile(filename)
    })
    watcher.on('error', (error) => {
      console.error(`[api-dev] Source watcher failed: ${error.message}`)
      requestShutdown(1)
    })
    watchers.push(watcher)
  }

  if (watchers.length === 0) {
    throw new Error('No backend source directories are available to watch.')
  }
}

function waitForExit(child) {
  return new Promise((resolve) => {
    child.once('exit', (code, signal) => resolve({ code, signal }))
  })
}

for (const [signal, exitCode] of [['SIGINT', 130], ['SIGTERM', 143]]) {
  process.on(signal, () => requestShutdown(exitCode))
}

log('Preparing backend classes...')
const initialCompile = spawnMaven(['-q', '-DskipTests', 'compile'])
const initialResult = await waitForExit(initialCompile)

if (initialResult.code !== 0) {
  const reason = initialResult.signal
    ? `signal ${initialResult.signal}`
    : `exit code ${initialResult.code ?? 1}`
  console.error(`[api-dev] Initial compilation failed (${reason}).`)
  requestShutdown(initialResult.code ?? 1)
} else if (!shuttingDown) {
  updateReloadTrigger()
  startWatchers()
  log('Watching src/main/java and src/main/resources for hot reloads.')

  applicationProcess = spawnMaven(['-q', '-DskipTests', 'spring-boot:run'])
  applicationProcess.once('exit', (code, signal) => {
    if (shuttingDown) return

    const reason = signal ? `signal ${signal}` : `exit code ${code ?? 1}`
    console.error(`[api-dev] Spring Boot stopped unexpectedly (${reason}).`)
    requestShutdown(code ?? 1)
  })
}
