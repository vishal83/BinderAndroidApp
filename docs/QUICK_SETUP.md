# Quick Setup Guide

Get started with Android Binder IPC demos in minutes!

## 🚀 Prerequisites

- **Android Studio**: Latest stable version
- **Android SDK**: API level 24+ (Android 7.0)
- **Device/Emulator**: Android 7.0+ for testing
- **Java/Kotlin**: Basic knowledge recommended

## ⚡ Option 1: Single App Demo (Recommended for beginners)

### 1. Build and Install
```bash
cd "01-SingleApp-ServiceDemo"
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Test the App
1. Launch **"Binder IPC Test"** app
2. Tap **"Connect"** to bind to service
3. Try **"Add Numbers"** → See result: 100
4. Try **"Heavy Computation"** → Test priority inheritance
5. Try **"Async Operation"** → Watch progress updates

### 3. What You'll See
- Real-time IPC operation results
- Process information (different PIDs)
- Priority inheritance in action
- Bidirectional callbacks working

---

## 🔥 Option 2: Inter-App Demo (Advanced)

### 1. Build Both Apps
```bash
# Server App
cd "02-InterApp-Demo/ServerApp"
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# Client App  
cd "../ClientApp"
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Test Inter-App Communication
1. Launch **"Binder Server"** app
2. Tap **"Start Service"** → See service status: 🟢 RUNNING
3. Launch **"Binder Client"** app  
4. Tap **"Connect to Server"** → See connection status: 🟢 CONNECTED
5. Try any test operation → Watch **both** apps!

### 3. What You'll See

**In Server App:**
```
📊 Live IPC Interactions

⚙️ ADD_NUMBERS                    10:15:32.123
   Adding 42 + 58
   Client PID: 12345

📞 PROGRESS_UPDATE                10:15:33.456
   Notifying 1 clients: 50%
```

**In Client App:**
```
📊 Test Results

[1634567890] Add Numbers (42 + 58): 100
[1634567891] Process Message: Server processed...
[1634567892] Heavy Computation: 123456789
```

## 🎯 Quick Test Scenarios

### Scenario 1: Basic IPC (Both Demos)
1. Connect to service
2. **Add Numbers**: 42 + 58 = 100
3. **Process Message**: "Hello Binder!" → "Processed: Hello Binder!"

### Scenario 2: Priority Inheritance (Both Demos)
1. **Heavy Computation** → Watch logs for priority changes
2. **Get Priority** → See current thread priority
3. Compare performance with/without high priority client

### Scenario 3: Async Operations (Both Demos)
1. **Async Operation** → Watch progress: 0% → 100%
2. **Callbacks** → See bidirectional communication
3. **Concurrent Calls** → Multiple operations simultaneously

### Scenario 4: Inter-App Monitoring (Inter-App Demo Only)
1. **Server App**: Watch live logs while client operates
2. **Color Coding**: See different operation types
3. **Client Metrics**: Monitor connected clients count
4. **Real-time Debugging**: Professional IPC monitoring

## 🔧 Troubleshooting

### Common Issues

**Single App Demo:**
```
Issue: Service not connecting
Solution: Check if app has proper permissions
```

**Inter-App Demo:**
```
Issue: "Server App Required" message
Solution: Install server app first, then client app

Issue: Client can't find server
Solution: Ensure server service is started (green status)

Issue: Permission denied
Solution: Both apps should be installed from same source
```

### Debug Tips

1. **Check Logcat**: Filter by "Binder" or "IPC"
2. **Verify PIDs**: Different processes should have different PIDs
3. **Monitor Performance**: Watch CPU usage during heavy computation
4. **Test Callbacks**: Ensure bidirectional communication works

## 📱 Testing Matrix

| Feature | Single App | Inter-App | Expected Result |
|---------|------------|-----------|-----------------|
| Service Connection | ✅ | ✅ | Different PIDs shown |
| Basic Operations | ✅ | ✅ | Correct calculations |
| Priority Inheritance | ✅ | ✅ | Priority changes logged |
| Async Callbacks | ✅ | ✅ | Progress 0→100% |
| Real-time Logging | ❌ | ✅ | Live interaction display |
| Multi-App Testing | ❌ | ✅ | Two separate apps |

## 🎓 Next Steps

### After Single App Demo
1. Understand AIDL interface design
2. Learn service lifecycle management  
3. Master callback patterns
4. Move to inter-app demo

### After Inter-App Demo
1. Implement custom permissions
2. Add signature-level security
3. Create your own IPC services
4. Build plugin architectures

## 💡 Pro Tips

- **Use ADB Logcat**: `adb logcat | grep -E "(Binder|IPC)"`
- **Monitor Memory**: Watch memory usage in Android Studio
- **Test on Real Device**: Better performance analysis than emulator
- **Compare Approaches**: Run both demos to see differences

Ready to explore Android Binder IPC? Start with the approach that matches your experience level! 🚀
