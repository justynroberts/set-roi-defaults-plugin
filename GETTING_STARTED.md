# 🚀 Quick Start Guide: Rundeck Set ROI Defaults Plugin

## ⚠️ CRITICAL WARNINGS - READ FIRST

**🚨 THIS PLUGIN MODIFIES JOB DEFINITIONS**
- This plugin **PERMANENTLY CHANGES** Rundeck job configurations
- **ALWAYS** test in non-production environments first
- **ALWAYS** use dry-run mode before applying changes
- **BACKUP** your job definitions before use
- This plugin is provided **AS-IS** with **NO WARRANTIES**
- **NO SUPPORT** from PagerDuty or Rundeck

**⚠️ STRONGLY ADVISED TO TEST EXTENSIVELY**

---

## 📋 Prerequisites Checklist

Before starting, ensure you have:

- [ ] **Rundeck 5.x** (Commercial/Enterprise with ROI Metrics plugin)
- [ ] **Administrator access** to your Rundeck instance
- [ ] **Java 11+** and **Gradle** (for building from source)
- [ ] **API token** with appropriate permissions
- [ ] **NON-PRODUCTION environment** for initial testing
- [ ] **Backup** of existing job definitions

## ⚡ Quick Installation (10 minutes)

### Step 1: Get the Plugin (2 minutes)

#### Option A: Download from Releases (Recommended)
```bash
# Download the latest release
wget https://github.com/your-org/rundeck-setroidefaults/releases/download/v0.1.0/set-roi-defaults-0.1.0.jar

# Verify download
ls -la set-roi-defaults-0.1.0.jar
```

#### Option B: Build from Source
```bash
# Clone and build
git clone https://github.com/your-org/rundeck-setroidefaults.git
cd rundeck-setroidefaults
gradle clean build

# Verify build
ls -la build/libs/set-roi-defaults-0.1.0.jar
```

### Step 2: Upload to Rundeck (1 minute)

1. **Access Plugin Management**:
   - Log into Rundeck as admin
   - Go to **System** → **Plugins**
   - Click **Upload Plugin** tab

2. **Upload**:
   - Click **Choose File**
   - Select the plugin JAR file:
     - If downloaded: `set-roi-defaults-0.1.0.jar`
     - If built from source: `build/libs/set-roi-defaults-0.1.0.jar`
   - Click **Upload Plugin**
   - Wait for "Plugin uploaded successfully" message

### Step 3: Setup API Token (2 minutes)

1. **Generate Token**:
   - Click your username → **Profile**
   - Go to **User API Tokens** tab
   - Click **Generate New Token**
   - Name: `ROI Plugin Token`
   - Click **Generate Token**
   - **Copy the token immediately!**

2. **Store in Key Storage**:
   - Go to **System** → **Key Storage**
   - Click **Add or Upload a Key**
   - Type: **Password**
   - Path: `keys/roi-plugin-token`
   - Paste your API token
   - Click **Save**

### Step 4: Test the Plugin (3 minutes)

⚠️ **CRITICAL: Start with Dry-Run Mode**

1. **Create Test Job**:
   - Go to any **NON-PRODUCTION** project
   - Click **Create Job**
   - Name: `Test ROI Plugin`
   - Add workflow step: **Set ROI Defaults**

2. **Configure Plugin (DRY-RUN FIRST)**:
   ```
   API Token Path: keys/roi-plugin-token
   Rundeck URL: (leave blank)
   API Version: 46
   Default Hours Saved: 0.1667
   Project Filter: (leave blank for current project)
   Dry Run Mode: true  ← CRITICAL: ALWAYS START WITH TRUE
   ```

3. **Run Test**:
   - Click **Save** then **Run Job Now**
   - Check execution log for output like:
   ```
   ✅ [project] job_name - Already has hours (0.25)
   🆕 [project] job_name - WOULD ADD hours [0.1667]
   
   Summary:
     Projects: 1
     Jobs Processed: 5
     Jobs Updated: 2
   ⚠️  DRY RUN MODE: No actual changes were made!
   ```

### Step 5: Apply Changes (1 minute)

⚠️ **ONLY AFTER EXTENSIVE TESTING**

Once you're satisfied with the dry-run results:

1. **Edit the job**
2. **Change setting**: `Dry Run Mode: false`
3. **Run again** to apply actual changes
4. **Verify**: Check that jobs now have ROI metrics configured

## 🎯 Safe Testing Configurations

### Configuration for Current Project Only (RECOMMENDED FIRST)
```
API Token Path: keys/roi-plugin-token
Project Filter: (blank)
Dry Run Mode: true  ← ALWAYS START HERE
```

### Configuration for Specific Test Project
```
API Token Path: keys/roi-plugin-token
Project Filter: test-project
Dry Run Mode: true  ← TEST THOROUGHLY
```

### Configuration for All Projects (ADVANCED - EXTREME CAUTION)
```
API Token Path: keys/roi-plugin-token
Project Filter: all
Dry Run Mode: true  ← EXTENSIVE TESTING REQUIRED
```

## 🚨 Quick Troubleshooting

| Issue | Quick Fix |
|-------|-----------|
| Plugin not in step list | Restart Rundeck: `sudo systemctl restart rundeckd` |
| Key storage error | Verify token path matches exactly: `keys/roi-plugin-token` |
| Permission denied | Ensure API token user has job read/write permissions |
| No jobs found | Check project filter and user project access |

## ⚠️ Safety Guidelines

### Before First Use
1. **Read all warnings** in this document
2. **Test in non-production** environment only
3. **Backup job definitions** before any testing
4. **Use dry-run mode** for all initial testing
5. **Understand the risks** - you are responsible for any issues

### During Testing
1. **Start small** - test with single project first
2. **Monitor logs** carefully for any errors
3. **Verify output** matches expectations
4. **Don't rush** - take time to understand what the plugin does

### Before Production
1. **Extensive testing** in staging environment
2. **Team review** of planned changes
3. **Rollback plan** documented and tested
4. **Monitoring** in place for job changes

## 📞 Need Help?

- 📖 **Full Documentation**: See [`PLUGIN_README.md`](PLUGIN_README.md)
- 🐛 **Issues**: Check the troubleshooting section in the main README
- 💬 **Community**: GitHub Issues for community support (NO OFFICIAL SUPPORT)

## ⚠️ Final Reminders

**BEFORE PROCEEDING:**
- This plugin **MODIFIES JOB DEFINITIONS** permanently
- Changes **CANNOT BE EASILY UNDONE** without backups
- **NO WARRANTIES** or support provided
- **YOU ARE RESPONSIBLE** for any issues or damages
- **TEST EXTENSIVELY** before production use

---

**🎉 You're Ready to Test!** The plugin is now installed and ready for **CAREFUL TESTING** with dry-run mode.

**⚠️ REMEMBER: Always start with dry-run mode and test thoroughly before applying any changes!**