import os
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748b"))
        
        # Header (pages after cover)
        if self._pageNumber > 1:
            self.drawString(54, 750, "PROJECT REPORT: Distributed E-Commerce Microservices")
            self.drawRightString(612 - 54, 750, "Java 17 / Spring Boot 3 / Kafka")
            self.setStrokeColor(colors.HexColor("#cbd5e1"))
            self.setLineWidth(0.5)
            self.line(54, 742, 612 - 54, 742)
            
        # Footer
        page_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(612 - 54, 36, page_text)
        self.drawString(54, 36, "Confidential - Academic & Technical Project Report")
        self.setStrokeColor(colors.HexColor("#cbd5e1"))
        self.setLineWidth(0.5)
        self.line(54, 46, 612 - 54, 46)
        self.restoreState()

def create_report():
    pdf_filename = r"C:\Users\Pranav\eureka_project\docs\PROJECT_REPORT.pdf"
    os.makedirs(os.path.dirname(pdf_filename), exist_ok=True)
    
    doc = SimpleDocTemplate(
        pdf_filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )
    
    styles = getSampleStyleSheet()
    
    # Custom styles
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=28,
        textColor=colors.HexColor('#0f172a'),
        alignment=1, # Center
        spaceAfter=8
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=16,
        textColor=colors.HexColor('#2563eb'),
        alignment=1,
        spaceAfter=15
    )
    
    meta_style = ParagraphStyle(
        'MetaStyle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=colors.HexColor('#334155'),
        alignment=1
    )
    
    h1_style = ParagraphStyle(
        'SectionH1',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=17,
        textColor=colors.HexColor('#1e293b'),
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )
    
    h2_style = ParagraphStyle(
        'SectionH2',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10.5,
        leading=14,
        textColor=colors.HexColor('#2563eb'),
        spaceBefore=10,
        spaceAfter=4,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'ReportBody',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=13.5,
        textColor=colors.HexColor('#1e293b'),
        spaceAfter=7
    )
    
    bullet_style = ParagraphStyle(
        'ReportBullet',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=colors.HexColor('#1e293b'),
        leftIndent=15,
        spaceAfter=3
    )

    tbl_header_style = ParagraphStyle(
        'TblHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9,
        leading=12,
        textColor=colors.white
    )
    
    tbl_cell_style = ParagraphStyle(
        'TblCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=11.5,
        textColor=colors.HexColor('#1e293b')
    )

    tbl_cell_bold = ParagraphStyle(
        'TblCellBold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=11.5,
        textColor=colors.HexColor('#0f172a')
    )

    story = []
    
    # Title Block
    story.append(Spacer(1, 15))
    story.append(Paragraph("PROJECT REPORT", title_style))
    story.append(Paragraph("Distributed E-Commerce Microservices Platform", subtitle_style))
    
    meta_text = (
        "<b>Project Name:</b> Distributed E-Commerce Microservices (Event-Driven Architecture)<br/>"
        "<b>Technology:</b> Java 17+, Spring Boot 3, Spring Cloud, Apache Kafka, Apache Avro, PostgreSQL<br/>"
        "<b>Project Type:</b> Java Full Stack Cloud Microservices Internship Project<br/>"
        "<b>Repository:</b> <u>https://github.com/samyukthasai28/eureka_project</u>"
    )
    story.append(Paragraph(meta_text, meta_style))
    story.append(Spacer(1, 10))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor('#2563eb'), spaceBefore=4, spaceAfter=14))
    
    # 1. Abstract
    story.append(Paragraph("1. Abstract", h1_style))
    abstract_text = (
        "The <b>Distributed E-Commerce Microservices Platform</b> is an enterprise-grade, event-driven "
        "distributed backend built with <b>Java 17+</b>, <b>Spring Boot 3</b>, and <b>Spring Cloud (2023.x)</b>. "
        "The primary purpose of this project is to eliminate monolithic bottlenecks by decomposing the commerce "
        "ecosystem into decoupled, independently deployable microservices coordinated via high-throughput asynchronous messaging.<br/><br/>"
        "The system utilizes <b>Apache Kafka</b> with strictly typed <b>Apache Avro schemas</b> to manage event streaming "
        "across Order, Inventory, and Payment services with guaranteed partition ordering. Distributed transaction consistency "
        "is preserved across isolated PostgreSQL databases using the <b>Choreography-based Saga Pattern</b> with automated compensating "
        "transactions, backed by the <b>Transactional Outbox Pattern</b>.<br/><br/>"
        "Dynamic discovery is managed by <b>Netflix Eureka Server</b>, externalized settings via <b>Spring Cloud Config Server</b>, "
        "and edge routing by a non-blocking <b>Spring Cloud Gateway</b> enforcing cryptographic <b>HMAC-SHA256 JWT authentication</b>. "
        "Fault tolerance and distributed observability are achieved via <b>Resilience4j</b> circuit breakers, <b>Micrometer Tracing "
        "with Zipkin</b>, and <b>Prometheus metrics</b>. The system is fully containerized using <b>Docker Compose</b> and <b>Kubernetes</b>."
    )
    story.append(Paragraph(abstract_text, body_style))
    
    # 2. Introduction
    story.append(Paragraph("2. Introduction", h1_style))
    intro_text = (
        "In modern cloud applications, high availability, fault isolation, and horizontal scalability are essential requirements. "
        "Monolithic architectures suffer from shared database lock contention, tight coupling, and high blast radiuses during deployment.<br/><br/>"
        "The Distributed E-Commerce Microservices Platform provides a modern solution by decoupling core business domains into "
        "isolated services that communicate asynchronously through events. Each service owns its dedicated PostgreSQL database, "
        "strictly adhering to the <b>Database-per-Service</b> pattern.<br/><br/>"
        "Instead of brittle point-to-point HTTP couplings, business flows like order fulfillment and stock reservation are choreographed "
        "over Kafka topics. An API Gateway with JWT verification serves as a single secure entry point, while circuit breakers prevent cascading failures."
    )
    story.append(Paragraph(intro_text, body_style))
    
    # 3. Objectives
    story.append(Paragraph("3. Objectives", h1_style))
    objectives = [
        "1. To develop a robust, cloud-native distributed microservices architecture using Spring Boot 3.",
        "2. To implement dynamic service discovery and heartbeat monitoring via Netflix Eureka Server.",
        "3. To centralize externalized environment configuration using Spring Cloud Config Server.",
        "4. To enforce reactive, non-blocking routing and cryptographic HMAC-SHA256 JWT validation at the API Gateway.",
        "5. To implement asynchronous event-driven communication using Apache Kafka and Apache Avro schemas.",
        "6. To guarantee partition-level message ordering using aggregate identifiers (Order ID) as Kafka partition keys.",
        "7. To implement Choreography-based Saga distributed transactions with automated compensating rollbacks.",
        "8. To enforce database isolation using PostgreSQL with dedicated databases per service.",
        "9. To build fault tolerance using Resilience4j circuit breakers, retries, rate limiters, and fallback endpoints.",
        "10. To achieve end-to-end distributed observability using Micrometer, Zipkin tracing, and Prometheus metrics.",
        "11. To containerize all services using multi-stage Dockerfiles, Docker Compose, and Kubernetes manifests.",
        "12. To provide an accessible Executive Dashboard UI for real-time topology inspection and transaction tracing."
    ]
    for obj in objectives:
        story.append(Paragraph(obj, bullet_style))
    story.append(Spacer(1, 4))
    
    # 4. Technologies Used
    story.append(Paragraph("4. Technologies Used", h1_style))
    tech_data = [
        [Paragraph("Technology", tbl_header_style), Paragraph("Purpose in Platform", tbl_header_style)],
        [Paragraph("Java 17 (OpenJDK 17)", tbl_cell_bold), Paragraph("Core programming language utilizing records, pattern matching, and sealed types", tbl_cell_style)],
        [Paragraph("Spring Boot 3.3.x", tbl_cell_bold), Paragraph("Underlying enterprise backend application framework", tbl_cell_style)],
        [Paragraph("Spring Cloud (2023.x)", tbl_cell_bold), Paragraph("Distributed microservice coordination (Discovery, Config, Gateway)", tbl_cell_style)],
        [Paragraph("Netflix Eureka Server", tbl_cell_bold), Paragraph("Centralized dynamic service registry and heartbeat health tracking (:8761)", tbl_cell_style)],
        [Paragraph("Spring Cloud Config", tbl_cell_bold), Paragraph("Centralized externalized configuration repository (:8888)", tbl_cell_style)],
        [Paragraph("Spring Cloud Gateway", tbl_cell_bold), Paragraph("Reactive, non-blocking edge reverse proxy and route filter (:8080)", tbl_cell_style)],
        [Paragraph("Apache Kafka", tbl_cell_bold), Paragraph("Distributed high-throughput, partitioned event streaming message backbone", tbl_cell_style)],
        [Paragraph("Apache Avro", tbl_cell_bold), Paragraph("Compact, strictly-typed binary schema serialization across services", tbl_cell_style)],
        [Paragraph("PostgreSQL", tbl_cell_bold), Paragraph("Relational persistence with isolated databases (order_db, inventory_db, payment_db)", tbl_cell_style)],
        [Paragraph("Spring Security / JWT", tbl_cell_bold), Paragraph("Cryptographic HMAC-SHA256 token verification and stateless API protection", tbl_cell_style)],
        [Paragraph("Resilience4j", tbl_cell_bold), Paragraph("Fault isolation via circuit breakers, retries, rate limiters, and fallbacks", tbl_cell_style)],
        [Paragraph("Micrometer & Zipkin", tbl_cell_bold), Paragraph("Distributed tracing, context propagation (B3/W3C), and trace collection (:9411)", tbl_cell_style)],
        [Paragraph("Prometheus & Grafana", tbl_cell_bold), Paragraph("Time-series metrics scraping and operational telemetry dashboards (:9090 / :3000)", tbl_cell_style)],
        [Paragraph("Docker & Docker Compose", tbl_cell_bold), Paragraph("Multi-stage container builds and 11-container infrastructure orchestration", tbl_cell_style)],
        [Paragraph("Kubernetes (K8s)", tbl_cell_bold), Paragraph("Production container deployment, ingress, config maps, and service manifests", tbl_cell_style)],
        [Paragraph("Swagger / OpenAPI 3", tbl_cell_bold), Paragraph("Interactive REST API documentation and live endpoint testing across all services", tbl_cell_style)]
    ]
    t_tech = Table(tech_data, colWidths=[150, 354])
    t_tech.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#1e293b')),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor('#cbd5e1')),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#f8fafc')]),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
    ]))
    story.append(t_tech)
    story.append(Spacer(1, 8))
    
    # 5. System Modules
    story.append(Paragraph("5. System Modules", h1_style))
    
    story.append(Paragraph("5.1 Service Registry (Eureka Server)", h2_style))
    story.append(Paragraph("Runs on port <b>8761</b> as a dedicated Netflix Eureka Server. Downstream services automatically register their IP, host, and port on startup and send periodic heartbeats. The API Gateway queries Eureka dynamically, routing traffic via <code>lb://service-name</code> without hardcoded URLs.", body_style))
    
    story.append(Paragraph("5.2 Centralized Configuration Server (Config Server)", h2_style))
    story.append(Paragraph("Runs on port <b>8888</b> providing externalized configuration via native filesystem or Git profiles. Microservices fetch their datasource, Kafka, and resilience settings during bootstrap, enabling centralized configuration management without code redeployment.", body_style))

    story.append(Paragraph("5.3 API Gateway & JWT Security", h2_style))
    story.append(Paragraph("Built on Spring Cloud Gateway (port <b>8080</b>) utilizing non-blocking Project Reactor. Validates HMAC-SHA256 signatures, expiration claims, and issuer headers on every incoming request. Rejects unauthorized requests with HTTP 401 and routes authorized traffic to microservices through Resilience4j circuit breakers.", body_style))

    story.append(Paragraph("5.4 Common Events Module (common-events)", h2_style))
    story.append(Paragraph("A shared Gradle library containing Apache Avro schemas (<code>OrderPlacedEvent</code>, <code>InventoryReservedEvent</code>, <code>PaymentProcessedEvent</code>, <code>InventoryReleaseEvent</code>, <code>OrderCancelledEvent</code>) and custom binary serializers/deserializers for type-safe Kafka messaging.", body_style))

    story.append(Paragraph("5.5 Order Service", h2_style))
    story.append(Paragraph("Runs on port <b>8081</b> handling order placement, state transitions, and PostgreSQL persistence. Implements the Transactional Outbox Pattern and publishes partition-keyed <code>OrderPlacedEvent</code> messages to Kafka. Serves as the primary Saga evaluator for downstream events.", body_style))

    story.append(Paragraph("5.6 Inventory Service", h2_style))
    story.append(Paragraph("Runs on port <b>8082</b> managing product inventory and stock allocations. Consumes order events via a dedicated consumer group (<code>inventory-service</code>), performs atomic deductions in <code>inventory_db</code>, emits reservation events, and compensates reserved stock upon receiving <code>InventoryReleaseEvent</code>.", body_style))

    story.append(Paragraph("5.7 Payment Service", h2_style))
    story.append(Paragraph("Runs on port <b>8083</b> processing customer transactions. Consumes order events in a dedicated consumer group (<code>payment-service</code>), records payment results in <code>payment_db</code>, and publishes <code>PaymentProcessedEvent</code> or <code>PaymentFailedEvent</code> to Kafka.", body_style))

    story.append(Paragraph("5.8 Executive Dashboard UI", h2_style))
    story.append(Paragraph("Hosted directly on the Gateway (<b>http://localhost:8080/</b>), providing an interactive architecture topology map, an instant buy storefront, a live 4-stage Saga Stepper with Kafka payload inspector, and a 4-tab Data Explorer for Orders, Inventory, Payments, and Kafka topics.", body_style))

    # 6. System Architecture
    story.append(Paragraph("6. System Architecture", h1_style))
    arch_intro = (
        "The system follows a decentralized, event-driven microservices architecture where services communicate "
        "asynchronously through Kafka topics rather than synchronous point-to-point HTTP calls.<br/><br/>"
        "<b>Distributed Event Choreography Flow:</b><br/>"
        "1. The client sends a JWT-authenticated <code>POST /api/orders</code> request to the API Gateway (:8080).<br/>"
        "2. The Gateway validates the JWT token and routes the request to Order Service (:8081) via Eureka load balancing.<br/>"
        "3. Order Service records the order as <code>PENDING</code> in <code>order_db</code> and publishes an Avro <code>OrderPlacedEvent</code> to Kafka using the Order ID as the partition key.<br/>"
        "4. Inventory Service (:8082) and Payment Service (:8083) consume the event independently in parallel.<br/>"
        "5. Inventory Service reserves the requested quantity and emits <code>InventoryReservedEvent</code>.<br/>"
        "6. Payment Service captures payment against <code>payment_db</code> and emits <code>PaymentProcessedEvent</code>.<br/>"
        "7. Order Service consumes both events; upon dual success, marks the order status as <code>CONFIRMED</code>.<br/>"
        "8. If payment fails, Order Service emits an <code>InventoryReleaseEvent</code> to Kafka, triggering Inventory Service to automatically roll back the reserved stock (Automated Saga Compensation)."
    )
    story.append(Paragraph(arch_intro, body_style))

    # 7. Reliability & Observability Features
    story.append(Paragraph("7. Reliability & Observability Features", h1_style))
    rel_features = [
        "<b>Choreography-Based Saga Pattern:</b> Maintains distributed consistency across isolated microservice databases without requiring distributed 2PC locks.",
        "<b>Transactional Outbox Pattern:</b> Guarantees atomicity between local PostgreSQL commits and Kafka message dispatches, preventing phantom events.",
        "<b>Partition Key Ordering:</b> Consistent hashing on Order ID ensures that all state changes for a specific order sequence strictly in order within its Kafka partition.",
        "<b>Resilience4j Circuit Breakers:</b> Gateway routes automatically trip into OPEN state if downstream error rates exceed 50%, serving instant HTTP 503 fallback responses.",
        "<b>Exponential Backoff Retries:</b> Automatically absorbs transient network blips and database reconnection attempts without dropping user requests.",
        "<b>Distributed Tracing (Brave & Zipkin):</b> Injects B3 trace and span headers across HTTP requests and Kafka record metadata, offering end-to-end trace latency visualization.",
        "<b>Prometheus & Actuator Telemetry:</b> Actuator endpoints expose JVM metrics, HTTP request rates, and Kafka producer/consumer lag for Prometheus scraping."
    ]
    for feat in rel_features:
        story.append(Paragraph(f"• {feat}", bullet_style))
    story.append(Spacer(1, 4))

    # 8. Database and Storage
    story.append(Paragraph("8. Database and Storage", h1_style))
    db_text = (
        "<b>PostgreSQL (order_db):</b> Stores order aggregates, item line entities, outbox event queues, and Saga audit records.<br/>"
        "<b>PostgreSQL (inventory_db):</b> Stores product SKU master records, warehouse stock levels, and reservation logs.<br/>"
        "<b>PostgreSQL (payment_db):</b> Stores payment transaction records, payment statuses, and customer billing details.<br/>"
        "<b>Apache Kafka:</b> Multi-partition persistent message log (<code>order-placed-topic</code>, <code>inventory-events-topic</code>, <code>payment-events-topic</code>, <code>order-compensation-topic</code>) with offset tracking per consumer group."
    )
    story.append(Paragraph(db_text, body_style))

    # 9. API Documentation
    story.append(Paragraph("9. API Documentation", h1_style))
    story.append(Paragraph("SpringDoc OpenAPI 3 / Swagger UI is embedded in each microservice, providing interactive exploration and live API invocation:<br/>"
                           "• <b>API Gateway:</b> <code>http://localhost:8080/swagger-ui.html</code><br/>"
                           "• <b>Order Service:</b> <code>http://localhost:8081/swagger-ui.html</code><br/>"
                           "• <b>Inventory Service:</b> <code>http://localhost:8082/swagger-ui.html</code><br/>"
                           "• <b>Payment Service:</b> <code>http://localhost:8083/swagger-ui.html</code><br/>"
                           "OpenAPI JSON specifications are accessible at <code>/v3/api-docs</code> on each service port.", body_style))

    # 10. Development Progress & Milestone Breakdown
    story.append(Paragraph("10. Development Progress & Milestone Breakdown", h1_style))
    progress_data = [
        [Paragraph("Development Phase", tbl_header_style), Paragraph("Delivered Milestones & Scope", tbl_header_style), Paragraph("Status", tbl_header_style)],
        [Paragraph("Week 1", tbl_cell_bold), Paragraph("Eureka Server, Config Server, API Gateway with reactive JWT validation, PostgreSQL schemas, and Payment microservice foundation.", tbl_cell_style), Paragraph("COMPLETE", tbl_cell_bold)],
        [Paragraph("Week 2", tbl_cell_bold), Paragraph("Apache Kafka integration, Apache Avro schemas (common-events), Order producer, Inventory/Payment consumers, dedicated consumer groups, partition ordering, and Outbox event store.", tbl_cell_style), Paragraph("COMPLETE", tbl_cell_bold)],
        [Paragraph("Week 3", tbl_cell_bold), Paragraph("Choreography-based Saga pattern, automated compensating transactions, Resilience4j circuit breakers, retries, rate limiters, fallback routes, Micrometer/Zipkin tracing, and Prometheus metrics.", tbl_cell_style), Paragraph("COMPLETE", tbl_cell_bold)],
        [Paragraph("Week 4", tbl_cell_bold), Paragraph("OpenAPI 3 / Swagger documentation, multi-stage production Dockerfiles, 11-container docker-compose.yml, 13 Kubernetes manifests (k8s/), and GitHub Actions CI/CD workflow.", tbl_cell_style), Paragraph("COMPLETE", tbl_cell_bold)]
    ]
    t_progress = Table(progress_data, colWidths=[90, 334, 80])
    t_progress.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#1e293b')),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor('#cbd5e1')),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#f8fafc')]),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
    ]))
    story.append(t_progress)
    story.append(Spacer(1, 8))

    # 11. Advantages
    story.append(Paragraph("11. Advantages", h1_style))
    advantages = [
        "<b>High Scalability:</b> Decoupled microservices scale independently based on real-time traffic demands.",
        "<b>Fault Isolation:</b> Failures in inventory or payment services are isolated by circuit breakers without impacting the Gateway.",
        "<b>Data Consistency:</b> Choreographed Saga and Outbox patterns guarantee eventual consistency across distributed databases.",
        "<b>Strict Ordering:</b> Kafka partition keys preserve exact sequencing of business transactions per order.",
        "<b>Contract Safety:</b> Apache Avro schemas enforce binary contract validation and prevent schema drift.",
        "<b>Stateless Edge Security:</b> Cryptographic HMAC-SHA256 JWTs eliminate server-side session memory at the gateway.",
        "<b>Operational Visibility:</b> Zipkin and Prometheus provide deep insight into distributed trace latencies and bottlenecks."
    ]
    for adv in advantages:
        story.append(Paragraph(f"• {adv}", bullet_style))
    story.append(Spacer(1, 4))

    # 12. Future Scope
    story.append(Paragraph("12. Future Scope", h1_style))
    future_items = [
        "Integration with HashiCorp Vault for enterprise secret lifecycle management and automated key rotation.",
        "Implementation of CQRS (Command Query Responsibility Segregation) with read-optimized Elasticsearch indices.",
        "Debezium Change Data Capture (CDC) streaming directly from PostgreSQL write-ahead logs to Kafka.",
        "Real-time WebSocket / Server-Sent Events (SSE) notification streaming to client frontend applications.",
        "Canary and Blue-Green deployment automation via ArgoCD within a production Kubernetes cluster.",
        "Multi-region active-active Kafka cluster replication (MirrorMaker 2) for disaster recovery."
    ]
    for item in future_items:
        story.append(Paragraph(f"• {item}", bullet_style))
    story.append(Spacer(1, 4))

    # 13. Conclusion
    story.append(Paragraph("13. Conclusion", h1_style))
    conclusion_text = (
        "The <b>Distributed E-Commerce Microservices Platform</b> provides an enterprise-ready reference implementation "
        "of modern cloud-native architecture. By combining <b>Java 17</b>, <b>Spring Boot 3</b>, <b>Spring Cloud</b>, "
        "<b>Apache Kafka</b>, <b>Apache Avro</b>, <b>PostgreSQL</b>, <b>Resilience4j</b>, <b>Micrometer/Zipkin</b>, "
        "<b>Docker</b>, and <b>Kubernetes</b>, the project effectively addresses key distributed computing challenges—including "
        "eventual consistency without distributed locks, fault isolation, asynchronous event streaming, and full-stack observability.<br/><br/>"
        "The project equips engineering teams with practical expertise in microservice decomposition, event-driven architectures, "
        "choreographed transactions, and enterprise container deployment."
    )
    story.append(Paragraph(conclusion_text, body_style))
    story.append(Spacer(1, 10))
    story.append(HRFlowable(width="100%", thickness=1, color=colors.HexColor('#cbd5e1'), spaceBefore=4, spaceAfter=8))
    
    footer_text = (
        "<b>Project:</b> Distributed E-Commerce Microservices Platform<br/>"
        "<b>Repository:</b> https://github.com/samyukthasai28/eureka_project<br/>"
        "<b>Development Stack:</b> Java 17+, Spring Boot 3, Spring Cloud, Apache Kafka, PostgreSQL"
    )
    story.append(Paragraph(footer_text, meta_style))
    
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"SUCCESS: PDF created at {pdf_filename}")

if __name__ == "__main__":
    create_report()
