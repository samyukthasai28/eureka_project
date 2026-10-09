# System Health & Endpoint Verification Script
# Verifies all microservices, gateways, infrastructure, and fallback routes

param (
    [string]$GatewayUrl = "http://localhost:8080",
    [string]$EurekaUrl = "http://localhost:8761",
    [string]$ConfigUrl = "http://localhost:8888",
    [string]$ZipkinUrl = "http://localhost:9411",
    [string]$PrometheusUrl = "http://localhost:9090",
    [string]$GrafanaUrl = "http://localhost:3000"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   CLOUDMART DISTRIBUTED MICROSERVICES HEALTH CHECK" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Testing system endpoints at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')...`n"

$results = @()

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Headers = @{}
    )

    $status = "DOWN"
    $code = "-"
    $latency = "-"
    $sw = [System.Diagnostics.Stopwatch]::StartNew()

    try {
        $response = Invoke-WebRequest -Uri $Url -Method $Method -Headers $Headers -TimeoutSec 4 -UseBasicParsing -ErrorAction Stop
        $sw.Stop()
        $code = $response.StatusCode
        $latency = "$($sw.ElapsedMilliseconds)ms"
        if ($code -ge 200 -and $code -lt 400) {
            $status = "HEALTHY"
        } else {
            $status = "DEGRADED"
        }
    } catch {
        $sw.Stop()
        if ($_.Exception.Response) {
            $code = [int]$_.Exception.Response.StatusCode
            if ($code -eq 401 -or $code -eq 403) {
                # Protected endpoint working properly (secured)
                $status = "SECURED (401)"
            } elseif ($code -eq 503) {
                $status = "FALLBACK (503)"
            } else {
                $status = "HTTP $code"
            }
        } else {
            $status = "UNREACHABLE"
        }
        $latency = "$($sw.ElapsedMilliseconds)ms"
    }

    return [PSCustomObject]@{
        Service     = $Name
        Endpoint    = $Url
        Status      = $status
        HTTPCode    = $code
        Latency     = $latency
    }
}

# 1. Infrastructure Services
$results += Test-Endpoint -Name "Eureka Service Registry" -Url "$EurekaUrl/eureka/apps" -Headers @{"Accept"="application/json"}
$results += Test-Endpoint -Name "Config Server (Native)" -Url "$ConfigUrl/order-service/default"
$results += Test-Endpoint -Name "API Gateway Health" -Url "$GatewayUrl/actuator/health"
$results += Test-Endpoint -Name "API Gateway Swagger" -Url "$GatewayUrl/swagger-ui.html"

# 2. Auth & Protected Endpoints
$results += Test-Endpoint -Name "Auth JWT Token Provider" -Url "$GatewayUrl/api/auth/token?username=customer@ecommerce.com&role=ROLE_USER"
$results += Test-Endpoint -Name "Order Service (Protected)" -Url "$GatewayUrl/api/orders"
$results += Test-Endpoint -Name "Inventory Service (Protected)" -Url "$GatewayUrl/api/inventory"
$results += Test-Endpoint -Name "Payment Service (Protected)" -Url "$GatewayUrl/api/payments"

# 3. Resilience4j Fallback Routes
$results += Test-Endpoint -Name "Order Fallback Route" -Url "$GatewayUrl/fallback/orders"
$results += Test-Endpoint -Name "Inventory Fallback Route" -Url "$GatewayUrl/fallback/inventory"
$results += Test-Endpoint -Name "Payment Fallback Route" -Url "$GatewayUrl/fallback/payments"

# 4. Observability & Telemetry
$results += Test-Endpoint -Name "Zipkin Tracing" -Url "$ZipkinUrl/actuator/health"
$results += Test-Endpoint -Name "Prometheus Metrics" -Url "$PrometheusUrl/-/healthy"
$results += Test-Endpoint -Name "Grafana Dashboard" -Url "$GrafanaUrl/api/health"

# Output Formatted Results
Write-Host "Service Verification Results:" -ForegroundColor Yellow
$results | Format-Table -AutoSize -Property Service, Status, HTTPCode, Latency, Endpoint

$healthyCount = ($results | Where-Object { $_.Status -eq "HEALTHY" -or $_.Status -eq "SECURED (401)" }).Count
Write-Host "`nSummary: $healthyCount of $($results.Count) services responding." -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
