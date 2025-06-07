# 📚 Rundeck Set ROI Defaults Plugin - Documentation Index

## ⚠️ CRITICAL WARNINGS

**🚨 READ BEFORE USING ANY DOCUMENTATION**
- This plugin **MODIFIES JOB DEFINITIONS** permanently
- **NO WARRANTIES** or support from PagerDuty/Rundeck
- **ALWAYS** test extensively before production use
- **BACKUP** job definitions before any testing
- Use **DRY-RUN MODE** for all initial testing

---

## 📋 Documentation Overview

Comprehensive documentation for the Rundeck Set ROI Defaults Plugin - a tool that manages ROI metric defaults in job definitions.

### 🎯 Main Documentation Files

| Document | Purpose | Target Audience | Risk Level |
|----------|---------|-----------------|------------|
| **[📖 PLUGIN_README.md](PLUGIN_README.md)** | Complete plugin documentation | All users - comprehensive reference | ⚠️ High |
| **[🚀 GETTING_STARTED.md](GETTING_STARTED.md)** | Quick setup guide (10 minutes) | New users - first-time installation | ⚠️ High |
| **[🏗️ BUILD_DEPLOY.md](BUILD_DEPLOY.md)** | Build and deployment guide | Developers and DevOps teams | ⚠️ High |
| **[📚 DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** | This overview document | All users - navigation guide | ℹ️ Info |

## 🚀 Quick Navigation

### 👋 New to the Plugin?
**⚠️ START HERE**: [`GETTING_STARTED.md`](GETTING_STARTED.md)
- **CRITICAL WARNINGS** and safety guidelines
- 10-minute setup guide with extensive safety checks
- Prerequisites checklist
- Step-by-step installation with dry-run testing

### 🔍 Need Complete Reference?
**⚠️ COMPREHENSIVE GUIDE**: [`PLUGIN_README.md`](PLUGIN_README.md)
- **RISK WARNINGS** and disclaimers
- Full feature documentation
- Configuration options and security considerations
- Usage examples with safety guidelines
- Troubleshooting guide

### 🏗️ Building or Deploying?
**⚠️ DEPLOYMENT GUIDE**: [`BUILD_DEPLOY.md`](BUILD_DEPLOY.md)
- **PRODUCTION WARNINGS** and safety procedures
- Linux build environment setup
- Traditional deployment methods (no Docker/Kubernetes)
- Production deployment scripts with rollback capability

## 📋 What This Plugin Does

The **Rundeck Set ROI Defaults Plugin** is a Workflow Step Plugin that:

- ⚠️ **MODIFIES JOB DEFINITIONS** to add ROI metric defaults
- ✅ **Detects existing ROI configurations** and adds missing fields
- ✅ **Supports dry-run mode** for safe testing (STRONGLY RECOMMENDED)
- ✅ **Processes single projects or all projects** accessible to the user
- ✅ **Integrates with Rundeck workflows** as a standard step
- ✅ **Uses secure Key Storage** for API token management

## 🎯 Key Features Covered in Documentation

### 🔧 Installation & Setup
- **Plugin building** with Gradle on Linux
- **Upload methods** (Web UI recommended, file system alternative)
- **API token setup** and Key Storage configuration
- **Permission requirements** and security setup
- **⚠️ EXTENSIVE SAFETY WARNINGS** throughout

### ⚙️ Configuration Options
- **API Token Path** (required) - Key Storage path
- **Rundeck URL** (optional) - Target Rundeck instance
- **API Version** (optional) - Rundeck API version
- **Default Hours Saved** (optional) - ROI metric value
- **Project Filter** (optional) - Target specific projects
- **Dry Run Mode** (optional) - **ALWAYS START WITH TRUE**

### 🎮 Usage Scenarios
- **Current project processing** - Default behavior (safest)
- **All projects processing** - Bulk operations (EXTREME CAUTION)
- **Specific project targeting** - Filtered operations
- **Dry-run testing** - **MANDATORY FIRST STEP**
- **Production deployment** - Only after extensive testing

### 🛡️ Security & Best Practices
- **Key Storage integration** for secure token management
- **Permission requirements** for API access
- **Error handling** and comprehensive logging
- **Rollback procedures** for production deployments
- **⚠️ RISK MITIGATION** strategies

## 🚨 Common Use Cases

### 1. First-Time Setup (SAFEST APPROACH)
```
📖 Read: GETTING_STARTED.md
🎯 Goal: Get plugin working safely in 10 minutes
⚠️ Requirements: Non-production environment, dry-run mode
✅ Result: Plugin installed and tested WITHOUT modifying jobs
```

### 2. Production Deployment (HIGH RISK)
```
📖 Read: BUILD_DEPLOY.md → Production Environment section
🎯 Goal: Deploy safely to production Rundeck
⚠️ Requirements: Extensive testing, backup procedures, rollback plan
✅ Result: Plugin deployed with full safety measures
```

### 3. Troubleshooting Issues
```
📖 Read: PLUGIN_README.md → Troubleshooting section
🎯 Goal: Resolve plugin or configuration issues
⚠️ Requirements: Understanding of risks and implications
✅ Result: Plugin working correctly with safety maintained
```

### 4. Understanding All Features (EXPERT LEVEL)
```
📖 Read: PLUGIN_README.md (complete)
🎯 Goal: Master all plugin capabilities safely
⚠️ Requirements: Full understanding of risks and responsibilities
✅ Result: Expert-level plugin usage with risk awareness
```

## 🔧 Technical Architecture

The plugin is built as a **Rundeck Workflow Step Plugin** with these components:

### Core Classes
- **[`Setroidefaults.groovy`](src/main/groovy/com/plugin/setroidefaults/Setroidefaults.groovy)** - Main plugin class
- **[`RoiManager.groovy`](src/main/groovy/com/plugin/setroidefaults/RoiManager.groovy)** - ROI management logic
- **[`Util.groovy`](src/main/groovy/com/plugin/setroidefaults/Util.groovy)** - Utility functions
- **[`FailureReason.groovy`](src/main/groovy/com/plugin/setroidefaults/FailureReason.groovy)** - Error handling

### Key Features
- **Auto-detection** of existing ROI plugin names
- **Secure API communication** with timeout handling
- **Comprehensive logging** and progress reporting
- **Workflow integration** with output context variables
- **⚠️ JOB MODIFICATION CAPABILITIES** (use with extreme caution)

## 📊 Documentation Quality & Safety

Each documentation file includes:

- ✅ **PROMINENT RISK WARNINGS** at the beginning
- ✅ **Clear prerequisites** and safety requirements
- ✅ **Step-by-step instructions** with safety checks
- ✅ **Troubleshooting sections** for common issues
- ✅ **Security considerations** and best practices
- ✅ **Linux-specific deployment** options (no Docker/Kubernetes)
- ✅ **Visual aids** (tables, diagrams, badges)
- ✅ **Cross-references** between documents
- ✅ **Disclaimer sections** about lack of support/warranties

## 🎉 Getting Started Safely

**⚠️ CHOOSE YOUR PATH CAREFULLY:**

1. **🚀 First Time User** → [`GETTING_STARTED.md`](GETTING_STARTED.md)
   - **START HERE** if you're new to the plugin
   - Includes all critical warnings and safety procedures

2. **📖 Need Full Details** → [`PLUGIN_README.md`](PLUGIN_README.md)
   - Complete reference with comprehensive risk information
   - Detailed configuration and troubleshooting

3. **🏗️ Building/Deploying** → [`BUILD_DEPLOY.md`](BUILD_DEPLOY.md)
   - Production-ready deployment with safety measures
   - Linux-specific build and deployment procedures

## ⚠️ Support & Responsibility

### 🚨 CRITICAL DISCLAIMERS

**NO SUPPORT OR WARRANTIES:**
- This plugin is provided **AS-IS** with **NO WARRANTIES**
- **NO SUPPORT** from PagerDuty or Rundeck
- **NO LIABILITY** for any damages or issues
- **YOU ARE RESPONSIBLE** for testing and any consequences

**COMMUNITY SUPPORT ONLY:**
- GitHub issues for community help (best effort only)
- Documentation provided as-is
- Source code available for review

### 🐛 Your Responsibility

**YOU MUST:**
- Test extensively before production use
- Understand all risks and implications
- Have backup and rollback procedures
- Monitor and maintain the plugin yourself
- Accept full responsibility for any issues

---

## 📞 Final Guidance

**⚠️ BEFORE USING THIS PLUGIN:**

1. **READ ALL WARNINGS** in every document
2. **UNDERSTAND THE RISKS** - this modifies job definitions permanently
3. **TEST EXTENSIVELY** in non-production environments
4. **BACKUP EVERYTHING** before any testing
5. **USE DRY-RUN MODE** for all initial testing
6. **ACCEPT RESPONSIBILITY** - no support or warranties provided

**🎯 All documentation emphasizes safety, testing, and risk awareness for this job-modifying plugin.**