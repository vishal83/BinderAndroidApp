# Inter-App Communication Demo

This demo demonstrates **true inter-app Binder IPC** with two separate applications communicating across process boundaries.

## 🏗️ Architecture

```
ServerApp (APK 1)                    ClientApp (APK 2)
├── MainActivity                     ├── MainActivity  
│   ├── Real-time Logging UI         │   ├── Client UI
│   └── Service Control               │   └── InterAppBinderClient
└── BinderTestService                 └── Service Discovery
    ├── Exported AIDL Service             ├── Package Verification
    ├── Custom Permissions               ├── Intent-based Binding
    └── Live Interaction Logging         └── Comprehensive Testing
```

## ✨ Server App Features

- 🖥️ **Exported Service**: Available to other applications
- 📊 **Real-Time Logging**: Live IPC interaction monitoring  
- 🎨 **Color-Coded Logs**: Visual distinction of operation types
- 📈 **Client Metrics**: Connected clients and operation counters
- 🔒 **Security Model**: Custom permissions and access control
- ⚙️ **Service Control**: Start/stop service with logging

### Log Types
- 🔗 **CONNECTION** (Blue): Service binding, callback registration
- ⚙️ **OPERATION** (Green): IPC method calls and results
- 📞 **CALLBACK** (Orange): Server-to-client notifications
- ❌ **ERROR** (Red): Failed operations or exceptions  
- ℹ️ **INFO** (Purple): Service lifecycle events

## ✨ Client App Features

- 📱 **Service Discovery**: Finds and connects to external service
- ✅ **Package Verification**: Checks if server app is installed
- 🔍 **Android 11+ Compatibility**: Package visibility queries
- 🧪 **Comprehensive Testing**: All inter-app IPC scenarios
- 🎯 **Real-time Results**: Live display of IPC call results

## 🚀 Quick Start

### Step 1: Install Server App
```bash
cd ServerApp
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Install Client App  
```bash
cd ../ClientApp
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Test Inter-App Communication
1. **Launch Server App** → Tap "Start Service"
2. **Launch Client App** → Tap "Connect to Server"  
3. **Run Tests** → Use various IPC test buttons
4. **Monitor Server** → Watch real-time logs in server app

## 🔒 Security Model

### Custom Permissions
```xml
<!-- Server defines permission -->
<permission android:name="com.visgupta.binder.SERVER_ACCESS" />

<!-- Client requests permission -->
<uses-permission android:name="com.visgupta.binder.SERVER_ACCESS" />
```

### Service Discovery
```kotlin
// Intent-based service discovery
val intent = Intent().apply {
    action = "com.visgupta.binder.TEST_SERVICE"
    setPackage("com.visgupta.binderserver")
}
```

### Android 11+ Package Visibility
```xml
<queries>
    <package android:name="com.visgupta.binderserver" />
    <intent>
        <action android:name="com.visgupta.binder.TEST_SERVICE" />
    </intent>
</queries>
```

## 🧪 Test Scenarios

### Basic IPC Operations
- **Add Numbers**: Simple parameter passing
- **Process Message**: String manipulation across apps
- **Get Service Info**: Process information retrieval

### Advanced Features  
- **Heavy Computation**: Cross-app priority inheritance
- **Async Operations**: Background tasks with progress callbacks
- **Concurrent Calls**: Multiple simultaneous IPC operations
- **Error Handling**: Connection failures and recovery

## 📊 Real-Time Monitoring

The server app provides live monitoring of all IPC interactions:

```
📊 Live IPC Interactions

⚙️ GET_SERVICE_INFO                    10:15:32.123
   Client requested service information
   Client PID: 12345

🔗 REGISTER_CALLBACK                   10:15:33.456
   Client registered for callbacks
   Client PID: 12345

⚙️ ADD_NUMBERS                         10:15:34.789
   Adding 42 + 58
   Client PID: 12345
```

## 🎯 What You'll Learn

- True inter-app communication patterns
- Service export and security models
- Intent-based service discovery
- Custom permission systems
- Package visibility (Android 11+)
- Real-time IPC monitoring
- Professional debugging techniques

## 🏢 Real-World Applications

- **System Services**: How Android framework services work
- **Plugin Architectures**: Apps providing services to others
- **Microservices**: Distributed app architecture
- **Security Models**: Inter-app permission systems

This demonstrates **production-level inter-app IPC** with comprehensive monitoring and debugging capabilities!
