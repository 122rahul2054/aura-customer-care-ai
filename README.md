# Aura Customer Care AI

AI customer support assistant built with Java Spring Boot and React.

A browser-based AI voice customer support assistant for the fictional brand **Aura Skincare**, built using **Java Spring Boot, React, and Gemini API**.

The assistant, **Aria**, helps customers check order details, understand brand policies, and receive spoken responses. After each call, the application generates a transcript and a structured JSON summary.

## Features

- Browser-based voice conversations.
- Text chat as an alternative to voice input.
- Start Call and End Call controls.
- Listening, Thinking, and Speaking indicators.
- AI function calling for order lookup.
- Backend checks for cancellation and return-window eligibility.
- Conversation context maintained during each call.
- Visible sample orders for testing.
- Post-call transcript and JSON summary.
- Downloadable call results.
- Error handling for missing orders and unavailable AI services.

> This application checks order information and explains eligibility. It does not actually cancel orders, issue refunds, or arrange replacements.

## Technology Stack

| Component | Technology |
|-----------|------------|
| Backend | Java 17+, Spring Boot |
| Frontend | React, Vite, CSS |
| AI | Gemini API |
| HTTP Client | Java HttpClient |
| JSON Processing | Jackson |
| Speech Input | Browser Speech Recognition |
| Speech Output | Browser Speech Synthesis |
| Order Storage | In-memory Java map |
| Build Tools | Maven, npm |
| Testing | JUnit, Mockito |
| Deployment Packaging | Docker |

## How It Works

1. The customer starts a call and speaks into the microphone.
2. The browser converts speech into text.
3. React sends the message to the Spring Boot backend.
4. Gemini processes the conversation.
5. For order-related questions, Gemini requests the `get_order_details` tool.
6. Java executes the lookup and returns the order information to Gemini.
7. The generated response appears in the transcript and is spoken by the browser.
8. When the call ends, the backend generates a structured summary.

The AI requests tool execution; the Java backend performs the actual lookup.

## Project Structure

```text
aura-customer-care-ai/
├── frontend/
│   ├── src/
│   │   ├── main.jsx
│   │   └── style.css
│   ├── index.html
│   ├── package.json
│   ├── package-lock.json
│   └── vite.config.js
├── src/
│   ├── main/
│   │   ├── java/com/rahul/aura/
│   │   │   ├── AuraApplication.java
│   │   │   ├── ApiController.java
│   │   │   ├── ApiErrors.java
│   │   │   ├── AiService.java
│   │   │   ├── CallService.java
│   │   │   └── OrderService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── .env.example
├── .gitignore
├── Dockerfile
├── pom.xml
└── README.md
```

The frontend build generates `src/main/resources/static/`, which Spring Boot serves.

## Requirements

- Java 17 or newer
- Maven 3.9 or newer
- Node.js 22 or newer
- A Gemini API key
- Access to a Gemini model supporting:
  - `generateContent`
  - Function calling
  - Structured JSON output
- A browser supporting speech recognition and speech synthesis
- Internet access

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/122rahul2054/aura-customer-care-ai.git
cd aura-customer-care-ai
```

### 2. Build the Frontend

```bash
cd frontend
npm ci
npm run build
cd ..
```

### 3. Configure the AI Provider

Set the environment variables before starting the backend.

**Windows PowerShell:**

```powershell
$env:GEMINI_API_KEY="YOUR_API_KEY"
$env:GEMINI_MODEL="YOUR_SUPPORTED_MODEL_ID"
```

**macOS/Linux:**

```bash
export GEMINI_API_KEY="YOUR_API_KEY"
export GEMINI_MODEL="YOUR_SUPPORTED_MODEL_ID"
```

Replace both placeholders with your actual configuration.

Use only the model ID in `GEMINI_MODEL`, without a `models/` prefix.

The original code defaults to `gemini-2.5-flash`. Availability must be verified for your account; override it when necessary.

> Keep API keys private. Do not commit them to GitHub. Editing `.env.example` alone does not configure Spring Boot.

### 4. Start the Backend

```bash
mvn spring-boot:run
```

### 5. Open the Application

Open:

```text
http://localhost:8080
```

Click **Start Call**, allow microphone access, and ask a question.

Once the frontend is built, Spring Boot serves both the frontend and backend. A separate React development server is unnecessary for this setup.

## Running in IntelliJ IDEA

1. Open the project folder containing `pom.xml`.
2. Allow IntelliJ to import Maven dependencies.
3. Select Java 17 or newer as the Project SDK.
4. Build the frontend using the commands above.
5. Open `AuraApplication.java`.
6. Open **Run → Edit Configurations**.
7. Add `GEMINI_API_KEY` and `GEMINI_MODEL` under **Environment variables**.
8. Run the application.
9. Open `http://localhost:8080`.

Keep this configuration in `application.properties`:

```properties
aura.api-key=${GEMINI_API_KEY:}
```

Do not replace it with a real API key.

## Frontend Development

For frontend development with automatic refresh, keep Spring Boot running on port 8080 and run:

```bash
cd frontend
npm run dev
```

Open:

```text
http://localhost:5173
```

Vite forwards `/api` requests to the Spring Boot backend.

## Sample Orders

| Order ID | Customer | Product | Value | Status |
|----------|----------|---------|-------|--------|
| ORD-101 | Priya Sharma | Vitamin C Serum (30ml) | ₹699 | Out for Delivery |
| ORD-102 | Rahul Verma | Hydrating Sunscreen SPF 50 | ₹499 | Delivered |
| ORD-103 | Ananya Patel | Green Tea Face Wash + Toner | ₹850 | Processing |

### Order Details

- **ORD-101:** BlueDart, tracking number `BD-982103`, expected by 6 PM today.
- **ORD-102:** Delhivery, tracking number `DL-441029`, delivered 14 days ago.
- **ORD-103:** Ordered 3 hours ago and eligible for cancellation.

These are fixed fictional records. Relative dates are retained from the assessment data.

## Brand Policies

| Topic | Policy |
|-------|--------|
| Shipping | Free above ₹499; ₹50 below ₹499 |
| Delivery Time | 3–5 business days |
| Returns | Within 7 days of delivery; unopened, unused, and in original packaging |
| Damaged Products | Report within 48 hours with photos for replacement |
| Cancellation | Allowed only while the order is Processing |
| Shipped / Out for Delivery | Cannot be cancelled; customers may refuse delivery at the doorstep |
| Cash on Delivery | Available up to ₹2,500; cash or UPI at the doorstep |

The shipping charge at exactly ₹499 is unspecified in the supplied policy. The assistant is instructed to acknowledge this uncertainty.

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/health` | Check application status and whether an API key is configured |
| GET | `/api/orders/samples` | Retrieve sample orders |
| POST | `/api/calls` | Start a new call |
| POST | `/api/chat` | Process a customer message |
| POST | `/api/calls/{id}/end` | End the call and generate its summary |

### Chat Request Example

```json
{
  "callId": "CALL_ID_RETURNED_BY_START_CALL",
  "message": "Where is my order ORD-101?"
}
```

### Example Call Summary

```json
{
  "customer_intent": "ORDER_TRACKING",
  "order_id": "ORD-101",
  "resolution_status": "RESOLVED",
  "call_summary": "The customer asked about ORD-101. The agent confirmed that it is out for delivery and expected by 6 PM today."
}
```

This is an illustrative response. Actual summaries are generated from each call.

## Example Questions

- “Where is my order ORD-101?”
- “What is its tracking number?”
- “Can I return ORD-102?”
- “Can I cancel ORD-103?”
- “What is your return policy?”
- “Is cash on delivery available?”
- “Can I pay through UPI at the doorstep?”
- “My product arrived damaged. What should I do?”
- “Where is order ORD-999?”

## Testing

Run the Java tests:

```bash
mvn test
```

The initial implementation passed eight automated tests covering:

- Order ID normalization.
- Missing and unknown orders.
- Cancellation and return-window eligibility.
- Tool execution and preservation of model response metadata.
- Conversation isolation.
- Empty calls and summary failures.
- Missing AI configuration.
- The HTTP request contract using a local simulated provider.

These tests do not verify live Gemini responses or actual microphone behaviour.

### Manual Test Checklist

| Test | Expected Behaviour |
|------|--------------------|
| Track ORD-101 | Return its courier, tracking number and delivery estimate |
| Return ORD-102 | Explain that it is outside the 7-day return window |
| Cancel ORD-103 | Explain eligibility without claiming cancellation occurred |
| Cancel ORD-101 | Explain that cancellation is unavailable |
| Ask without an order ID | Request the order ID |
| Ask about ORD-999 | Ask the customer to verify the ID |
| Ask an unrelated question | Redirect to Aura Skincare support |
| Deny microphone permission | Show an understandable error |
| End a call | Display transcript and JSON summary |

## Build a Runnable JAR

Build the frontend first, then run:

```bash
mvn clean package
java -jar target/aura-voice-1.0.0.jar
```

The JAR includes the generated frontend assets.

## Docker

Create a local `.env` file using `.env.example`, then enter your configuration.

```bash
docker build -t aura-customer-care-ai .
docker run --rm -p 8080:8080 --env-file .env aura-customer-care-ai
```

Do not commit `.env`.

The Dockerfile builds the frontend, runs Java tests and packages the application.

## Deployment

Deploy the Docker container to a hosting service that supports Java applications.

Configure:

- `GEMINI_API_KEY`
- `GEMINI_MODEL`
- `PORT`, if required by the host

Use `/api/health` for a health check.

Public microphone access requires HTTPS. Test the deployed application with real voice input before sharing it for evaluation.

## Engineering Decisions

### Why this architecture?

Spring Boot provides a clear structure for REST endpoints, conversation management and tool execution. React handles browser audio and the interface.

The modular speech-to-text, AI reasoning and text-to-speech approach keeps the implementation understandable. An in-memory map is sufficient for the three supplied mock orders.

### What was the most challenging implementation concern?

Coordinating microphone recognition, AI requests, speech playback and ending a call.

The implementation uses explicit interface states, pauses recognition during playback, and serializes backend operations within each call. Live browser testing is still needed to validate the full experience.

### What would improve with one more week?

- Streaming audio and lower response latency.
- Interruption handling.
- More policy and adversarial conversation tests.
- Persistent call storage.
- Better provider diagnostics and recovery.

### What would change for 1,000 conversations per day?

- Shared session storage for multiple backend instances.
- Customer authentication before accessing real orders.
- Rate limits and API spending controls.
- Provider quota and latency monitoring.
- Centralized logging and error tracking.
- Defined transcript retention and deletion policies.
- Load testing based on peak concurrency and call duration.

## Limitations

- Voice support and available voices depend on the browser and operating system.
- Indian-English speech is preferred when an appropriate voice is available.
- Calls use turn-taking; barge-in is not implemented.
- Sessions are stored in memory and are lost on restart.
- Inactive sessions expire after approximately one hour.
- The assistant cannot perform cancellations, refunds or replacements.
- Policy wording and summaries are model-generated and require evaluation.
- The transcript records generated responses, including text whose audio may be interrupted.
- Live AI/audio behaviour and public deployment must be verified separately.

## Troubleshooting

| Problem | Solution |
|---------|----------|
| AI is not configured | Set `GEMINI_API_KEY`, restart the backend and refresh |
| Provider HTTP 404 | Verify the model ID, account availability and API endpoint |
| Microphone denied | Enable permission in browser settings |
| Speech recognition unavailable | Use a supported browser; use typed chat to diagnose the backend |
| Page missing | Build the frontend and restart Spring Boot |
| Port 8080 already used | Set another `PORT` and open the corresponding address |
| `mvn` or `npm` not recognized | Install the required tools and configure PATH |

The health endpoint checks whether a key is configured; it does not verify that the key or model works.

## Author

**Rahul Singh**

GitHub: [122rahul2054](https://github.com/122rahul2054)

---

Built as an AI voice customer support assessment project using Java Spring Boot and React.
