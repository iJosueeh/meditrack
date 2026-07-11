**Project Name:** MediTrack
**Description:** A specialized Inventory Management System designed for Health Clinics (Postas de Salud). It ensures the constant availability of critical medical supplies, optimizes batch traceability, and facilitates decision-making through a high-fidelity visual interface.

**Tech Stack Context:** 
- Core & UI: Java 21 (LTS) with JavaFX 21.
- UI Framework: AtlantaFX (Primer Theme) for a modern, Fluent/Minimalist design, utilizing Ikonli for iconography.
- Database: SQL Server running in a Docker Container.
- Build & Testing: Maven, JUnit 5 for logic/integration testing, and GitHub Actions for CI.

**Core Domain Rules & Constraints:**
- Inventory Algorithm: All dispensing logic MUST strictly implement the FEFO (First Expire, First Out) algorithm to ensure efficient batch usage.
- Data Integrity: Inventory movements (Inputs, Outputs, Attentions) must be handled via strict ACID transactions.
- Traceability: The system must enforce sequential, descriptive identifiers per venue (e.g., CAT-001, PAC-001-000001).
- Security: Access must be strictly controlled via Role-Based Access Control (RBAC) with dynamic management of venue managers.
- Integration: Patient clinical records and attention history must be deeply integrated into the dispensing flow, not treated as an isolated module.
