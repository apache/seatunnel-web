#!/usr/bin/env node

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('Starting npm install with error handling...');

try {
  // Remove node_modules if it exists
  const nodeModulesPath = path.join(__dirname, 'node_modules');
  if (fs.existsSync(nodeModulesPath)) {
    console.log('Removing existing node_modules...');
    fs.rmSync(nodeModulesPath, { recursive: true, force: true });
  }

  // Remove package-lock.json if it exists
  const packageLockPath = path.join(__dirname, 'package-lock.json');
  if (fs.existsSync(packageLockPath)) {
    console.log('Removing existing package-lock.json...');
    fs.unlinkSync(packageLockPath);
  }

  // Run npm install with specific flags
  console.log('Running npm install...');
  execSync('npm install --ignore-scripts --legacy-peer-deps --no-audit --no-fund --no-package-lock --force', {
    stdio: 'inherit',
    cwd: __dirname
  });

  console.log('npm install completed successfully!');
} catch (error) {
  console.error('npm install failed:', error.message);
  
  // Try alternative approach
  console.log('Trying alternative npm install...');
  try {
    execSync('npm install --ignore-scripts --force', {
      stdio: 'inherit',
      cwd: __dirname
    });
    console.log('Alternative npm install completed successfully!');
  } catch (altError) {
    console.error('Alternative npm install also failed:', altError.message);
    process.exit(1);
  }
}