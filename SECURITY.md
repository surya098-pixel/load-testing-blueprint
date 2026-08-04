# Security Policy

## Reporting a vulnerability

If you discover a security vulnerability, **please do not open a public issue**.

Instead, [open a private security advisory](../../security/advisories/new) with:

- A description of the issue
- Steps to reproduce
- Potential impact
- Any suggested fix

You'll receive a response within **7 days**. Confirmed vulnerabilities will be fixed as quickly as possible and disclosed responsibly.

## Scope

This repository is a template / reference project. In scope:

- Insecure defaults (leaked credentials in examples, permissive config, etc.)
- Documentation that encourages unsafe patterns
- Supply-chain concerns specific to *this* template

Out of scope (report to the upstream project):

- Vulnerabilities in third-party dependencies (Gatling, Maven, JDK, etc.)
- General web application security concerns unrelated to the template

## Responsible load testing

This template can generate significant traffic. **Only run load tests against targets you own or have explicit written permission to test.** Running the stress / spike profiles against a third party without authorization may be illegal in your jurisdiction and can trigger abuse protections.

## Supported versions

Only the `main` branch receives security updates.
