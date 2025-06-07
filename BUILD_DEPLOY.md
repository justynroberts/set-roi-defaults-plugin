# 🏗️ Build & Deployment Guide

Complete guide for building and deploying the Rundeck Set ROI Defaults Plugin on Linux systems.

## ⚠️ CRITICAL WARNINGS

**🚨 READ BEFORE PROCEEDING**
- This plugin **MODIFIES JOB DEFINITIONS** permanently
- **ALWAYS** test in non-production environments first
- **BACKUP** your job definitions before deployment
- Use **DRY-RUN MODE** extensively before applying changes
- This plugin is provided **AS-IS** with **NO WARRANTIES**

---

## 🐧 Linux Build Environment Setup

### Prerequisites Installation

#### Ubuntu/Debian Systems
```bash
# Update package list
sudo apt update

# Install Java 11 (OpenJDK)
sudo apt install openjdk-11-jdk

# Install Gradle
sudo apt install gradle

# Verify installations
java -version
gradle -version
```

#### RHEL/CentOS/Rocky Linux Systems
```bash
# Install Java 11
sudo dnf install java-11-openjdk-devel

# Install Gradle (via SDKMAN - recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle

# Verify installations
java -version
gradle -version
```

#### Alpine Linux (Minimal)
```bash
# Install build dependencies
apk add --no-cache openjdk11 gradle git

# Verify installations
java -version
gradle -version
```

## 🔨 Building the Plugin

### Standard Build Process

```bash
# 1. Clone the repository
git clone https://github.com/your-org/rundeck-setroidefaults.git
cd rundeck-setroidefaults

# 2. Set Java environment (if multiple versions installed)
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# 3. Clean and build
gradle clean build

# 4. Verify build artifacts
ls -la build/libs/
# Should show: set-roi-defaults-0.1.0.jar

# 5. Check build reports
ls -la build/reports/tests/test/
```

### Build with Tests
```bash
# Run full test suite
gradle clean test build

# View test results
firefox build/reports/tests/test/index.html
# or
cat build/reports/tests/test/index.html
```

### Build for Production
```bash
# Production build with optimizations
gradle clean build -Pproduction=true

# Verify JAR contents
jar -tf build/libs/set-roi-defaults-0.1.0.jar | head -20
```

## 📦 Deployment Methods

### Method 1: Web UI Upload (Recommended)

#### Option A: Using Release Download
```bash
# 1. Download the plugin
wget https://github.com/your-org/rundeck-setroidefaults/releases/download/v0.1.0/set-roi-defaults-0.1.0.jar

# 2. Upload via Rundeck Web UI
# - Navigate to System → Plugins → Upload Plugin
# - Select set-roi-defaults-0.1.0.jar
# - Click Upload Plugin
# - Verify installation in Installed Plugins section
```

#### Option B: Using Built Plugin
```bash
# 1. Build the plugin
gradle clean build

# 2. Upload via Rundeck Web UI
# - Navigate to System → Plugins → Upload Plugin
# - Select build/libs/set-roi-defaults-0.1.0.jar
# - Click Upload Plugin
# - Verify installation in Installed Plugins section
```

### Method 2: Direct File Copy (Traditional)

```bash
# For standard Rundeck installation
# Use appropriate source path:
# If downloaded from releases:
sudo cp set-roi-defaults-0.1.0.jar /var/lib/rundeck/libext/
# If built from source:
# sudo cp build/libs/set-roi-defaults-0.1.0.jar /var/lib/rundeck/libext/

# Set proper ownership
sudo chown rundeck:rundeck /var/lib/rundeck/libext/set-roi-defaults-0.1.0.jar

# Restart Rundeck
sudo systemctl restart rundeckd

# Verify plugin loading
sudo tail -f /var/log/rundeck/service.log | grep -i "set-roi-defaults"
```

## 🔧 Environment-Specific Configurations

### Development Environment
```bash
# Quick development cycle
gradle build && \
sudo cp build/libs/set-roi-defaults-0.1.0.jar /var/lib/rundeck/libext/ && \
sudo systemctl restart rundeckd && \
sudo tail -f /var/log/rundeck/service.log
```

### Staging Environment
```bash
# Staging deployment with backup
sudo cp /var/lib/rundeck/libext/set-roi-defaults-*.jar /var/lib/rundeck/libext/backup/ 2>/dev/null || true
sudo cp build/libs/set-roi-defaults-0.1.0.jar /var/lib/rundeck/libext/
sudo systemctl restart rundeckd
```

### Production Environment
```bash
# ⚠️ PRODUCTION DEPLOYMENT WITH ROLLBACK CAPABILITY
PLUGIN_DIR="/var/lib/rundeck/libext"
BACKUP_DIR="/var/lib/rundeck/libext/backup/$(date +%Y%m%d_%H%M%S)"
NEW_PLUGIN="build/libs/set-roi-defaults-0.1.0.jar"

echo "⚠️  PRODUCTION DEPLOYMENT - PROCEED WITH CAUTION"
echo "This will deploy a plugin that MODIFIES JOB DEFINITIONS"
read -p "Have you tested this extensively? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Deployment cancelled - test first!"
    exit 1
fi

# Create backup
sudo mkdir -p "$BACKUP_DIR"
sudo cp "$PLUGIN_DIR"/set-roi-defaults-*.jar "$BACKUP_DIR/" 2>/dev/null || true

# Deploy new version
sudo cp "$NEW_PLUGIN" "$PLUGIN_DIR/"
sudo chown rundeck:rundeck "$PLUGIN_DIR/set-roi-defaults-0.1.0.jar"

# Restart service
sudo systemctl restart rundeckd

# Verify deployment
sleep 30
if sudo systemctl is-active --quiet rundeckd; then
    echo "✅ Deployment successful"
    sudo tail -20 /var/log/rundeck/service.log | grep -i "set-roi-defaults"
    echo "⚠️  REMEMBER: Test with DRY-RUN mode before applying changes!"
else
    echo "❌ Deployment failed - rolling back"
    sudo cp "$BACKUP_DIR"/* "$PLUGIN_DIR/"
    sudo systemctl restart rundeckd
fi
```

## 🚀 Automated Deployment Scripts

### Build and Deploy Script
```bash
#!/bin/bash
# deploy-roi-plugin.sh

set -e

RUNDECK_HOME="/var/lib/rundeck"
PLUGIN_DIR="$RUNDECK_HOME/libext"
LOG_FILE="/var/log/rundeck/service.log"

echo "⚠️  WARNING: This plugin MODIFIES JOB DEFINITIONS"
echo "⚠️  Ensure you have tested extensively before production use"
echo ""

read -p "Continue with deployment? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "❌ Deployment cancelled"
    exit 1
fi

echo "🏗️ Building plugin..."
gradle clean build

echo "📦 Deploying plugin..."
sudo cp build/libs/set-roi-defaults-0.1.0.jar "$PLUGIN_DIR/"
sudo chown rundeck:rundeck "$PLUGIN_DIR/set-roi-defaults-0.1.0.jar"

echo "🔄 Restarting Rundeck..."
sudo systemctl restart rundeckd

echo "⏳ Waiting for Rundeck to start..."
sleep 30

echo "✅ Checking deployment..."
if sudo systemctl is-active --quiet rundeckd; then
    echo "✅ Rundeck is running"
    echo "📋 Recent logs:"
    sudo tail -10 "$LOG_FILE" | grep -i "plugin\|set-roi-defaults" || echo "No plugin-related logs found"
    echo ""
    echo "⚠️  IMPORTANT: Always test with DRY-RUN mode first!"
    echo "⚠️  This plugin modifies job definitions permanently!"
else
    echo "❌ Rundeck failed to start"
    exit 1
fi

echo "🎉 Deployment complete!"
```

### Make script executable and run
```bash
chmod +x deploy-roi-plugin.sh
./deploy-roi-plugin.sh
```

## 🔍 Verification and Testing

### Post-Deployment Verification
```bash
# Check plugin file exists
ls -la /var/lib/rundeck/libext/set-roi-defaults-*

# Check Rundeck service status
sudo systemctl status rundeckd

# Check for plugin loading in logs
sudo grep -i "set-roi-defaults" /var/log/rundeck/service.log

# Test API connectivity
curl -H "Accept: application/json" http://localhost:4440/api/46/system/info
```

### Plugin Functionality Test
```bash
# ⚠️ ALWAYS START WITH DRY-RUN TESTING
echo "⚠️  Testing plugin functionality with DRY-RUN mode"
echo "Create a test job with the Set ROI Defaults step"
echo "Configure with Dry Run Mode: true"
echo "Verify output shows 'WOULD ADD' messages without actual changes"
```

## 🐛 Troubleshooting Build Issues

### Common Build Problems

#### Java Version Issues
```bash
# Check Java version
java -version

# Set correct JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
echo $JAVA_HOME

# Update alternatives (Ubuntu/Debian)
sudo update-alternatives --config java
```

#### Gradle Issues
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Use Gradle wrapper instead
./gradlew clean build

# Check Gradle daemon
gradle --status
gradle --stop
```

#### Permission Issues
```bash
# Fix file permissions
chmod +x gradlew
sudo chown -R $USER:$USER .

# Fix Rundeck directory permissions
sudo chown -R rundeck:rundeck /var/lib/rundeck/libext/
```

### Build Environment Debugging
```bash
# Debug build with verbose output
gradle clean build --info --stacktrace

# Check system resources
free -h
df -h
```

## 📊 Performance Considerations

### Build Optimization
```bash
# Parallel builds
gradle clean build --parallel

# Increase memory for large builds
export GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
gradle clean build

# Use build cache
gradle clean build --build-cache
```

### Deployment Optimization
```bash
# Minimize downtime with staged deployment
sudo cp build/libs/set-roi-defaults-0.1.0.jar /tmp/
sudo systemctl stop rundeckd
sudo cp /tmp/set-roi-defaults-0.1.0.jar /var/lib/rundeck/libext/
sudo systemctl start rundeckd
```

## 🛡️ Security Considerations

### Build Security
```bash
# Verify JAR integrity
sha256sum build/libs/set-roi-defaults-0.1.0.jar

# Check for vulnerabilities in dependencies
gradle dependencyCheckAnalyze
```

### Deployment Security
```bash
# Set restrictive permissions
sudo chmod 644 /var/lib/rundeck/libext/set-roi-defaults-0.1.0.jar
sudo chown rundeck:rundeck /var/lib/rundeck/libext/set-roi-defaults-0.1.0.jar

# Verify no world-writable permissions
ls -la /var/lib/rundeck/libext/set-roi-defaults-*
```

---

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] Java 11+ installed and configured
- [ ] Gradle installed and working
- [ ] Source code cloned and accessible
- [ ] Build completes successfully
- [ ] Tests pass (if running test suite)
- [ ] **BACKUP of existing job definitions created**
- [ ] **Tested extensively in non-production environment**

### Deployment
- [ ] Rundeck service is running
- [ ] Backup of existing plugins created
- [ ] Plugin deployed to correct directory
- [ ] Proper file ownership set
- [ ] Rundeck service restarted successfully
- [ ] Plugin loading verified in logs
- [ ] Plugin appears in workflow step types

### Post-Deployment Testing
- [ ] Test job created with plugin step
- [ ] **DRY-RUN mode tested first**
- [ ] Plugin executes without errors
- [ ] Output logs are as expected
- [ ] **No actual job modifications until thoroughly tested**

### Production Readiness
- [ ] **Extensive testing completed in staging**
- [ ] **Rollback procedure documented and tested**
- [ ] **Team trained on plugin usage and risks**
- [ ] **Monitoring in place for job definition changes**

---

## ⚠️ Final Warnings

**BEFORE PRODUCTION USE:**
1. **TEST EXTENSIVELY** - This plugin modifies job definitions permanently
2. **USE DRY-RUN MODE** - Always preview changes before applying
3. **BACKUP EVERYTHING** - Have rollback procedures ready
4. **UNDERSTAND RISKS** - You are responsible for any issues
5. **NO SUPPORT** - This is provided AS-IS with no warranties

**🎉 Ready for Deployment!** Your plugin is now built and ready for careful, tested deployment.