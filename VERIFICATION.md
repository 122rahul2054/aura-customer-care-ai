# Verification performed

- Java 17 compilation and Maven package succeeded.
- 8 automated tests passed: 3 order-policy tests, 4 call-flow tests, 1 local HTTP provider-contract test.
- React/Vite production build succeeded.
- Packaged JAR started; home page, health endpoint and sample orders returned HTTP 200.
- Starting a call without an API key correctly returned HTTP 503 with configuration guidance.

Not verified: live Gemini responses, actual microphone/speaker flow, visual browser layout, Docker build, public hosting. Browser automation could not run because the available browser binary download failed. Provider/tool tests use mock responses; these are not live AI verification.

Before submission: configure your key, run the README test matrix with real speech, verify response latency and policy wording, then test your deployed HTTPS URL. The browser speech API may require a supported desktop browser and network access.
