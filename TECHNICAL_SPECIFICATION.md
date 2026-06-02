# Tea & Coffee Business CRM - Technical Specification

## 1. Project Overview

| Field | Value |
|-------|-------|
| **Application Name** | TeaBiz CRM |
| **Package** | `com.teabiz.crm` |
| **Platform** | Android (Kotlin / Jetpack Compose) |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 (Android 14) |
| **Architecture** | MVVM + Clean Architecture |
| **Language** | Kotlin 1.9.22 |
| **UI** | Jetpack Compose + Material 3 |
| **DI** | Hilt |
| **Target Market** | Tea, Coffee, and Related Product Businesses |

---

## 2. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                           │
│  Compose UI │ Navigation │ ViewModels │ State Flows │ Theme         │
├─────────────────────────────────────────────────────────────────────┤
│                          DOMAIN LAYER                               │
│  Use Cases │ Repository Interfaces │ AI Services │ Validators       │
├─────────────────────────────────────────────────────────────────────┤
│                           DATA LAYER                                │
│  Room DB │ Retrofit │ Gmail API │ WhatsApp │ GMB API │ OpenAI API  │
├─────────────────────────────────────────────────────────────────────┤
│                        INFRASTRUCTURE                               │
│  Hilt DI │ WorkManager │ DataStore │ Notifications │ Security       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Module Breakdown

### 3.1 Gmail Email Lead Extraction Module
**Purpose**: Fetch, parse, and extract structured lead data from Gmail accounts.

**Components**:
- `GmailService` - OAuth2 authentication + email fetching via Gmail API v1
- `EmailParser` - Regex + NLP extraction of lead fields from email bodies
- `LeadExtractor` - Transform parsed email data into Lead entities
- `GmailAuthManager` - Handle OAuth2 token lifecycle

**Data Extraction Logic**:
- Contact number: Regex patterns for Indian (`+91`, `0`) and international formats
- Email ID: RFC 5322 compliant parsing
- Product interest: Keyword matching against `ProductCategory` enum
- Inquiry details: Intent classification (inquiry, order, complaint, feedback)
- Lead name: From sender display name or parsed from body

**API Configuration**:
- Gmail API v1 REST endpoint
- OAuth2 scope: `https://www.googleapis.com/auth/gmail.readonly`
- Rate limit: 250 quota units/second
- Batch size: 100 messages per request

---

### 3.2 Excel/CSV Import Module
**Purpose**: Import leads from spreadsheet files with smart column detection.

**Supported Formats**: `.xlsx` (Apache POI XSSF), `.xls` (Apache POI HSSF), `.csv` (OpenCSV)

**Features**:
- Smart column alias detection (case-insensitive, fuzzy matching)
- Duplicate detection by email OR phone
- Batch processing with WorkManager
- Import history tracking with error logs
- Rollback support for failed imports
- Downloadable sample template

---

### 3.3 Lead Segregation & Categorization Module
**Purpose**: Organize leads into actionable segments for targeted marketing.

**Category Hierarchy**:
```
├── Product Type: Tea Premix, Coffee Premix, Nescafe Premix, Other
├── Equipment: Tea Machine, Coffee Machine, Nescafe Machine, Accessories
├── Client Type: Society, Cafe, Restaurant, Office, Retail, Manufacturing, Wholesale
└── Geographic: Local, National, International
```

**Features**: Auto-categorization via keyword matching, manual override, batch categorization, saved filter presets, export capability.

---

### 3.4 AI-Driven Follow-Up Module
**Purpose**: Generate personalized, sales-oriented follow-up messages using OpenAI.

**Pipeline**: Lead Data → Context Builder → Prompt Template → OpenAI API → Message → Review → Send

**Message Types**: Initial inquiry, stale lead re-engagement, post-purchase thank you, promotional offer, feedback request.

**Features**: Multi-language (Hindi, English, Marathi), tone adjustment, A/B variants, scheduled follow-ups via WorkManager.

---

### 3.5 Bulk WhatsApp Messaging Module
**Purpose**: High-volume, category-segmented WhatsApp messaging campaigns.

**Integration**: Baileys (open-source WhatsApp Web library) via REST API bridge server.

**Compliance**: Rate limiting (20 msg/min), opt-out management, delivery tracking, blacklist, business hours enforcement.

---

### 3.6 WhatsApp Catalog Module
**Purpose**: Fetch and display WhatsApp Business catalog for product showcase.

**Features**: Product browsing, direct sharing, price comparison, inventory status, quick order via WhatsApp.

---

### 3.7 SEO & Marketing Tools Module
**Purpose**: Keyword research, competitor analysis, marketing insights.

**Data Sources**: Google Trends API, Ubersuggest (free tier), Google Search Console API, AnswerThePublic (scraping).

**Features**: Keyword research with volume data, competitor analysis, content optimization, local SEO insights.

---

### 3.8 Google My Business Integration Module
**Purpose**: Manage GMB profile and automate customer responses.

**Components**: Account verification, post management, review monitoring, Q&A auto-response, insights analytics.

**AI Autoresponder**: Generates professional responses to GMB reviews and questions using OpenAI.

---

### 3.9 Keyword Research & Competitor Analysis Module
**Purpose**: AI-powered market intelligence for tea/coffee businesses.

**Analysis**: Local (city), national (state/country), international (global) dimensions.

**AI Insights**: Market gap identification, pricing recommendations, content strategy, expansion opportunities.

---

### 3.10 Content Automation for Social Media Module
**Purpose**: AI-powered content creation for Instagram, Facebook, LinkedIn.

**Content Types**: Image posts, carousel posts, reels/scripts, text posts with hashtag generation.

**Target Audience**: Purchase managers, cafeteria personnel, café owners, stall owners, tea manufacturers.

---

## 4. Database Schema

### Core Tables
```sql
-- Leads (primary entity)
CREATE TABLE leads (
    id TEXT PRIMARY KEY, name TEXT NOT NULL, email TEXT, phone TEXT,
    company TEXT, productInterest TEXT, message TEXT, city TEXT,
    source TEXT DEFAULT 'MANUAL', status TEXT DEFAULT 'NEW',
    tags TEXT, createdAt INTEGER, updatedAt INTEGER, lastFollowUpAt INTEGER
);

-- Follow-ups
CREATE TABLE follow_ups (
    id TEXT PRIMARY KEY, leadId TEXT REFERENCES leads(id) ON DELETE CASCADE,
    message TEXT, channel TEXT DEFAULT 'WHATSAPP', status TEXT DEFAULT 'PENDING',
    scheduledAt INTEGER, sentAt INTEGER, isAiGenerated INTEGER DEFAULT 0,
    createdAt INTEGER
);

-- Campaigns
CREATE TABLE campaigns (
    id TEXT PRIMARY KEY, name TEXT, targetCategory TEXT,
    messageTemplate TEXT, scheduledAt INTEGER, status TEXT DEFAULT 'DRAFT',
    sentCount INTEGER DEFAULT 0, failedCount INTEGER DEFAULT 0,
    createdAt INTEGER, completedAt INTEGER
);

-- Import sessions
CREATE TABLE import_sessions (
    id TEXT PRIMARY KEY, fileName TEXT, fileType TEXT,
    totalRows INTEGER DEFAULT 0, importedRows INTEGER DEFAULT 0,
    failedRows INTEGER DEFAULT 0, duplicateRows INTEGER DEFAULT 0,
    status TEXT DEFAULT 'PENDING', startedAt INTEGER,
    completedAt INTEGER, errorMessage TEXT
);

-- Import errors
CREATE TABLE import_errors (
    id INTEGER PRIMARY KEY AUTOINCREMENT, sessionId TEXT,
    rowNumber INTEGER, columnName TEXT, rawValue TEXT,
    errorReason TEXT, createdAt INTEGER
);

-- SEO keywords
CREATE TABLE seo_keywords (
    id INTEGER PRIMARY KEY AUTOINCREMENT, keyword TEXT NOT NULL,
    searchVolume INTEGER, competition TEXT, trend TEXT,
    lastUpdated INTEGER
);

-- Competitors
CREATE TABLE competitors (
    id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
    website TEXT, keywords TEXT, estimatedTraffic INTEGER,
    topProducts TEXT, lastAnalyzed INTEGER
);

-- Content calendar
CREATE TABLE content_calendar (
    id INTEGER PRIMARY KEY AUTOINCREMENT, platform TEXT,
    contentType TEXT, caption TEXT, mediaUrl TEXT,
    hashtags TEXT, scheduledAt INTEGER, postedAt INTEGER,
    status TEXT DEFAULT 'DRAFT'
);

-- Saved filters
CREATE TABLE saved_filters (
    id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
    filterJson TEXT, createdAt INTEGER
);

-- GMB posts
CREATE TABLE gmb_posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT, postId TEXT,
    summary TEXT, topicType TEXT, language TEXT,
    mediaUrls TEXT, callToAction TEXT, status TEXT,
    createdAt INTEGER
);

-- Settings (key-value)
CREATE TABLE app_settings (
    key TEXT PRIMARY KEY, value TEXT, updatedAt INTEGER
);
```

---

## 5. Security & Privacy

- AES-256 encryption for API keys (EncryptedSharedPreferences)
- Biometric authentication for app access
- Certificate pinning for network requests
- OAuth2 for Gmail/Google services
- Token refresh mechanism
- GDPR compliance (data export/deletion)
- WhatsApp opt-in/opt-out management

---

## 6. Development Phases

| Phase | Scope | Duration |
|-------|-------|----------|
| Phase 1 | Core CRM (DB, Leads, Import, Dashboard) | Weeks 1-4 |
| Phase 2 | Gmail Integration + Email Parsing | Weeks 5-6 |
| Phase 3 | AI Follow-up (OpenAI) | Weeks 7-8 |
| Phase 4 | WhatsApp Bulk Messaging | Weeks 9-10 |
| Phase 5 | SEO Tools + Competitor Analysis | Weeks 11-12 |
| Phase 6 | GMB Integration + Autoresponder | Weeks 13-14 |
| Phase 7 | Content Automation + Social Media | Weeks 15-16 |
| Phase 8 | Polish, Security Audit, Launch | Weeks 17-20 |

---

## 7. Success Metrics

- Lead conversion rate: 15% improvement
- Follow-up response time: < 24 hours
- Campaign reach: 1000+ contacts/month
- App size: < 30MB
- Cold start: < 2 seconds
- Play Store rating: 4.5+ stars

---

*Document Version: 2.0 | Last Updated: 2026-05-30*
