/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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