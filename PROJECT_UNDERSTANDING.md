# FlowStable UPI - Project Understanding & Architecture

## Project Overview

**FlowStable UPI** is an innovative Android application that bridges modern digital payments with legacy GSM infrastructure. It enables UPI (Unified Payments Interface) transactions through the *99# USSD service, making digital payments accessible even without internet connectivity.

## Core Problem Solved

### The Challenge
- **Digital Divide**: Rural areas with poor internet connectivity
- **Data Depletion**: Users without active data plans
- **Server Downtime**: API gateways unreachable while cellular signaling works
- **Financial Inclusion**: Bringing digital payments to underserved populations

### The Solution
FlowStable creates a middleware layer that:
1. **Abstracts Complexity**: Makes the complex USSD *99# service user-friendly
2. **Automates Navigation**: Programmatic USSD menu traversal
3. **Modern Interface**: Clean Android UI for payment initiation
4. **Offline Capability**: Works purely through cellular signaling

## Technical Architecture

### 1. **Finite State Machine (FSM) Core**
```
IDLE → SELECT_SIM → MENU_MAIN → ENTER_UPI → ENTER_AMOUNT → CONFIRM → SUCCESS/FAILED
```

**Location**: `USSDController.kt`
- **Why**: Deterministic state management ensures reliable USSD navigation
- **How**: Enum-based states with transition logic
- **When**: Every transaction follows this exact sequence

### 2. **Accessibility Service Injection**
**Location**: `USSDService.kt`
- **Why**: Android's only sanctioned method for programmatic USSD interaction
- **How**: Monitors `TYPE_WINDOW_STATE_CHANGED` events, scrapes `AccessibilityNodeInfo` trees
- **When**: Active whenever USSD dialogs appear
- **Security**: Zero-trust PIN entry - automation stops at PIN input

### 3. **QR Code Vision Processing**
**Location**: `ScannerActivity.kt`
- **Why**: Seamless payment initiation from physical QR codes
- **How**: Google ML Kit Barcode Scanning with CameraX
- **When**: User chooses to scan QR codes
- **Privacy**: All processing happens on-device (edge computing)

### 4. **Multi-Operator Compatibility**
**Why**: Different carriers have different USSD dialects
- **Airtel**: Specific menu structures
- **Jio**: Different response patterns  
- **Vi/BSNL**: Variant implementations
- **How**: Heuristic parsing with regex matching

## Component Breakdown

### Main Activities

#### `MainActivity.kt` - Entry Point
- **Purpose**: Central hub for all payment operations
- **Features**: QR scanning, manual UPI entry, balance check, voice pay
- **Permissions**: Camera, Phone calls, Accessibility service

#### `ScannerActivity.kt` - QR Code Reader
- **Purpose**: Capture and parse UPI QR codes
- **Tech Stack**: CameraX + ML Kit Vision API
- **Flow**: QR → UPI string → PaymentActivity

#### `PaymentActivity.kt` - Payment Composition
- **Purpose**: Manual payment entry and QR result handling
- **Validation**: UPI ID format, amount validation
- **Integration**: Connects to USSD system

#### `ProcessingActivity.kt` - Transaction Monitor
- **Purpose**: Real-time USSD state monitoring
- **Features**: Status updates, PIN entry interface
- **Polling**: 500ms intervals for state changes

#### `ResultActivity.kt` - Transaction Completion
- **Purpose**: Success/failure display
- **Navigation**: Returns to main or retry options

### Core Services

#### `USSDService.kt` - The Engine
- **Purpose**: Accessibility service for USSD automation
- **Key Methods**:
  - `onAccessibilityEvent()`: Event interception
  - `extractUSSDText()`: Text parsing from dialogs
  - `fillUSSDInput()`: Automated form filling
  - `clickSendButton()`: UI interaction

#### `USSDController.kt` - State Machine
- **Purpose**: Central state management
- **States**: IDLE, SELECT_SIM, MENU_MAIN, ENTER_UPI, ENTER_AMOUNT, CONFIRM, SUCCESS, FAILED
- **Flows**: PAYMENT, BALANCE

#### `UPIData.kt` - Data Model
- **Purpose**: Payment information structure
- **Fields**: UPI ID, Name, Amount

## Security Architecture

### 1. **PIN Isolation**
- **Design**: Automation stops at PIN entry
- **Reason**: Zero-trust credential handling
- **Implementation**: Manual PIN input required

### 2. **On-Device Processing**
- **QR Scanning**: No cloud processing
- **Privacy**: Payment data never leaves device via IP
- **Security**: Edge computing paradigm

### 3. **Minimal Permissions**
- **Camera**: Only for QR scanning
- **Phone**: Required for USSD dialing
- **Accessibility**: Core functionality requirement

## Build & Deployment

### CI/CD Pipeline
**Location**: `.github/workflows/android.yml`
- **Trigger**: Push to main branch
- **Environment**: Ubuntu latest, JDK 17
- **Build Tool**: Gradle 8.2
- **Distribution**: Telegram Bot API for APK delivery

### Dependencies
- **Android SDK**: minSdk 26, targetSdk 34
- **CameraX**: 1.3.0 (camera handling)
- **ML Kit**: 17.2.0 (barcode scanning)
- **Kotlin**: 1.9.20
- **Material Design**: 1.10.0

## Innovation Highlights

### 1. **USSD Automation**
First-of-its-kind programmatic USSD navigation through accessibility services

### 2. **State Machine Design**
Deterministic transaction flow ensures reliability over flaky cellular connections

### 3. **Cross-Operator Support**
Unified interface for multiple carrier implementations

### 4. **Offline-First Architecture**
No dependency on IP connectivity for core functionality

## Technical Challenges Solved

### 1. **USSD Dialog Detection**
- **Challenge**: Identifying USSD dialogs across Android versions
- **Solution**: Package name and class name heuristics

### 2. **Text Extraction**
- **Challenge**: Parsing text from complex UI hierarchies
- **Solution**: Recursive tree traversal with proper memory management

### 3. **Operator Variations**
- **Challenge**: Different USSD menu structures
- **Solution**: Flexible regex-based pattern matching

### 4. **Timing Synchronization**
- **Challenge**: Coordinating automation with USSD responses
- **Solution**: Event-driven architecture with state polling

## Future Enhancement Opportunities

### 1. **Multi-Language Support**
- Current: English-only interface
- Future: Regional language support for broader adoption

### 2. **Transaction History**
- Current: No persistent storage
- Future: Local transaction logging

### 3. **Enhanced Security**
- Current: PIN isolation
- Future: Biometric authentication options

### 4. **Analytics Dashboard**
- Current: No usage tracking
- Future: Anonymous usage metrics for improvement

## Conclusion

FlowStable UPI represents a significant innovation in financial technology by:
- **Democratizing Access**: Bringing digital payments to offline populations
- **Technical Excellence**: Clean architecture with robust state management
- **Security First**: Zero-trust approach to sensitive operations
- **Practical Impact**: Real-world solution to digital inclusion challenges

The project demonstrates sophisticated Android development with accessibility services, computer vision, state machines, and cross-platform compatibility - all while maintaining a focus on security and user privacy.
