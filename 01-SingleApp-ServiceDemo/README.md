# Single App with Service Demo

This demo demonstrates **Binder IPC within a single application** where the service runs in a separate process (`:binderservice`).

## 🏗️ Architecture

```
BinderAndroidApp (Single APK)
├── MainActivity (Main Process)
│   ├── UI Layer (Compose)
│   └── BinderServiceClient
└── BinderTestService (:binderservice process)
    ├── AIDL Implementation
    └── Callback Management
```

## ✨ Features

- **Process Separation**: Service runs in `:binderservice` process
- **Modern UI**: Material 3 Compose interface with real-time updates
- **Comprehensive Testing**: All IPC operations with results display
- **Priority Inheritance**: Cross-process priority testing
- **Bidirectional Callbacks**: Service-to-client communication
- **Async Operations**: Background tasks with progress updates
- **Concurrent Testing**: Multiple simultaneous IPC calls

## 🚀 Quick Start

```bash
# Build and install
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app and start testing IPC operations
```

## 🧪 Test Operations

1. **Connect to Service**: Bind to the internal Binder service
2. **Basic Operations**: Add numbers, process messages
3. **Heavy Computation**: Test priority inheritance
4. **Async Operations**: Background tasks with callbacks
5. **Concurrent Calls**: Multiple simultaneous operations

## 📊 What You'll Learn

- AIDL interface design and implementation
- Service binding within app boundaries
- Cross-process communication patterns
- Priority inheritance mechanisms
- Callback-based bidirectional communication
- Modern Android development with Compose

## 🎯 Best For

- Learning Binder fundamentals
- Understanding AIDL concepts
- Testing IPC in controlled environment
- Educational demonstrations
- Interview preparation

This is the **perfect starting point** for understanding Android Binder IPC!