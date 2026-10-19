# Step-by-Step AWS & Vercel Free Tier Deployment Runbook

A production deployment guide for the **Student Support Chat Platform** (`chat-asistencia-estudiante`). This guide walks through deploying the entire monorepo architecture to AWS Free Tier and Vercel at **$0.00 total cost**.

---

## 1. Architecture Overview & Free Tier Cost Guarantee ($0.00)

The infrastructure distributes components across cloud providers to maximize reliability, maintain zero operating costs, and isolate security boundaries:

```
                      +---------------------------------------+
                      |          Vercel Edge Network          |
                      |   React 19 SPA (Vite + Tailwind CSS)   |
                      |          Free Hobby Plan ($0)         |
                      +-------------------+-------------------+
                                          |
                        HTTPS REST & WSS  |  CORS Allowed Origin
                                          v
+-----------------------------------------------------------------------------------+
| AWS Cloud (Default VPC)                                                            |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  | EC2 Instance (`t3.micro` - Ubuntu 24.04 LTS)                                |  |
|  | Free Tier: 750 hrs/month for 12 months                                      |  |
|  |                                                                             |  |
|  |  +---------------------+        +----------------------------------------+  |  |
|  |  | DuckDNS Dynamic DNS |        | Nginx Reverse Proxy (Port 80 / 443)    |  |  |
|  |  | Subdomain + Cron    |        | Let's Encrypt SSL (Certbot)            |  |  |
|  |  +---------------------+        +-------------------+--------------------+  |  |
|  |                                                     | Proxy                  |  |
|  |                                                     v                        |  |
|  |                                 +----------------------------------------+  |  |
|  |                                 | Docker Container (`chat-server:latest`)|  |  |
|  |                                 | Java 25 Native Preview / Spring Boot 3 |  |  |
|  |                                 | Port 8080 (512MB RAM Limit + 2GB Swap) |  |  |
|  |                                 +-------------------+--------------------+  |  |
|  +-----------------------------------------------------|------------------------+  |
|                                                        | JDBC Connection           |
|                                                        | Port 5432 (Internal VPC)  |
|                                                        v                           |
|  +-----------------------------------------------------------------------------+  |
|  | RDS PostgreSQL 16 (`db.t3.micro` - Single-AZ)                                |  |
|  | Free Tier: 750 hrs/month for 12 months, 20 GiB gp3 Storage                 |  |
|  | Private Access (Restricted to `chat-ec2-sg` only)                           |  |
|  +-----------------------------------------------------------------------------+  |
+-----------------------------------------------------------------------------------+
```

### Cost Breakdown

| Component | Provider / Service | Sizing / Tier | Monthly Cost | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend** | Vercel | Hobby Tier | **$0.00** | Permanent free tier, unlimited deployments, custom domains, automated HTTPS. |
| **Backend Server** | AWS EC2 | `t3.micro` (1 vCPU, 1 GB RAM) | **$0.00** | 750 hours/month free for 12 months under AWS Free Tier. |
| **Server Storage** | AWS EBS | 25 GiB gp3 | **$0.00** | AWS Free Tier covers up to 30 GiB total EBS per month. |
| **Database** | AWS RDS PostgreSQL | `db.t3.micro` (Single-AZ, 20 GiB gp3) | **$0.00** | 750 hours/month free for 12 months under AWS Free Tier. |
| **DNS & SSL** | DuckDNS & Let's Encrypt | Subdomain & Automated TLS Cert | **$0.00** | Open-source public CA + free dynamic DNS service. |
| **Total Cost** | | | **$0.00 / mo** | Guaranteed $0.00 provided you follow the configuration guards below. |

> [!WARNING]
> **Free Tier Guardrails:**
> 1. Never enable RDS **Multi-AZ** deployment (doubles instance hours and incurs charges).
> 2. Disable RDS **Storage Autoscaling** (prevents unexpected volume growth beyond 20 GiB).
> 3. Total EBS volume across all running EC2 instances must not exceed 30 GiB.
> 4. Run only **one** `t3.micro` EC2 instance and **one** `db.t3.micro` RDS instance in your AWS account.

---

## 2. Step 0: Safety Net ($0.01 AWS Budget Alert)

Before provisioning resources, establish a cloud spending alarm to alert you immediately if unexpected charges occur.

1. Open the [AWS Management Console](https://console.aws.amazon.com/).
2. In the top search bar, type `Budgets` and select **AWS Budgets** (or navigate to **Billing and Cost Management** -> **Budgets**).
3. Click **Create budget**.
4. In **Budget setup**, choose **Use a template (simplified)**.
5. Under **Templates**, select **Zero spend budget**.
   - *Budget name:* `zero-spend-alert`
   - *Email recipients:* Enter your personal email address.
6. Alternatively, choose **Customize (advanced)** -> **Cost budget**:
   - *Period:* Monthly.
   - *Budget renewal type:* Recurring budget.
   - *Budget method:* Fixed.
   - *Budgeted amount:* `$0.01`.
   - *Alert threshold:* Set alert at `100% of budgeted amount` ($0.01).
   - *Notification email:* Enter your email address.
7. Click **Confirm budget** (or **Create budget**).

---

## 3. Step 1: Security Groups Setup

Security groups act as virtual firewalls. To safeguard your database, RDS must accept traffic **only** from the EC2 instance, with zero public internet exposure.

### 3.1 Create `chat-ec2-sg` (EC2 Virtual Firewall)

1. Open **EC2 Console** -> Navigate to **Network & Security** -> **Security Groups**.
2. Click **Create security group**.
3. Configure Basic Details:
   - **Security group name:** `chat-ec2-sg`
   - **Description:** `Security group for chat application EC2 web server`
   - **VPC:** Select your **Default VPC** (e.g., `vpc-xxxxxxx (default)`).
4. Configure **Inbound rules**:
   - Click **Add rule**:
     - *Type:* `SSH` (Port 22)
     - *Source:* `My IP` (Recommended for administrative safety) or `Anywhere-IPv4` (`0.0.0.0/0`) if dynamic IP.
     - *Description:* `SSH administration`
   - Click **Add rule**:
     - *Type:* `HTTP` (Port 80)
     - *Source:* `Anywhere-IPv4` (`0.0.0.0/0`)
     - *Description:* `HTTP traffic for Certbot verification and Nginx redirect`
   - Click **Add rule**:
     - *Type:* `HTTPS` (Port 443)
     - *Source:* `Anywhere-IPv4` (`0.0.0.0/0`)
     - *Description:* `Secure HTTPS traffic for REST API and WebSockets`
5. Leave **Outbound rules** default (*All traffic*, `0.0.0.0/0`).
6. Click **Create security group**.
7. Note down the resulting **Security Group ID** (e.g., `sg-0a1b2c3d4e5f67890`).

### 3.2 Create `chat-rds-sg` (Database Virtual Firewall)

1. In **Security Groups**, click **Create security group**.
2. Configure Basic Details:
   - **Security group name:** `chat-rds-sg`
   - **Description:** `Security group for chat application RDS PostgreSQL`
   - **VPC:** Select the same **Default VPC**.
3. Configure **Inbound rules**:
   - Click **Add rule**:
     - *Type:* `PostgreSQL` (Port 5432)
     - *Source:* Choose **Custom**, then type and select `chat-ec2-sg` (the Security Group ID created in step 3.1, e.g., `sg-0a1b2c3d4e5f67890`).
     - *Description:* `Allow PostgreSQL traffic strictly from EC2 instance`
4. Leave **Outbound rules** default.
5. Click **Create security group**.

> [!IMPORTANT]
> Never set the RDS PostgreSQL source to `0.0.0.0/0`. Restricting inbound port 5432 directly to the `chat-ec2-sg` ID ensures your database is inaccessible from the public internet even if port 5432 is probed.

---

## 4. Step 2: Free Tier RDS PostgreSQL 16 Provisioning

1. Navigate to **RDS Console** -> Click **Databases** -> Click **Create database**.
2. **Choose a database creation method:** Select **Standard create**.
3. **Engine options:**
   - *Engine type:* **PostgreSQL**
   - *Engine Version:* **PostgreSQL 16.x** (e.g., PostgreSQL 16.6 or latest 16 minor release).
4. **Templates:**
   - Select **Free tier**. (This automatically limits multi-AZ and certain costly configurations).
5. **Settings:**
   - *DB instance identifier:* `chat-postgres-db`
   - *Master username:* `postgres`
   - *Master password:* Choose a strong password (e.g., `ChatSecurePostgres2026!`). Store this securely.
6. **Instance configuration:**
   - *DB instance class:* **Burstable classes (includes t classes)** -> `db.t3.micro` (1 vCPU, 1 GiB RAM).
7. **Storage:**
   - *Storage type:* **General Purpose SSD (gp3)**
   - *Allocated storage:* `20` GiB (Free tier limit is 20 GiB for RDS).
   - *Storage autoscaling:* **Uncheck** "Enable storage autoscaling" (prevents automatic scaling beyond the free tier).
8. **Connectivity:**
   - *Virtual private cloud (VPC):* Select your **Default VPC**.
   - *DB Subnet group:* `default`
   - *Public access:* Select **No** (Database is strictly internal).
   - *VPC security group (firewall):*
     - Choose **Select existing**.
     - Select `chat-rds-sg`.
     - Remove `default` security group if selected.
   - *Availability Zone:* `No preference`.
   - *Database port:* `5432`.
9. **Database authentication:**
   - Select **Password authentication**.
10. **Additional configuration:**
    - *Initial database name:* `proyecto_dswii`
    - *Backup retention period:* `1 day` (or disable automated backups if you do not need snapshots).
    - *Encryption:* Enable encryption (AWS KMS Default key).
11. Scroll down to review estimated costs (**$0.00** within Free Tier limits). Click **Create database**.
12. Status will show *Creating* (takes 5-10 minutes). Once status is *Available*, click the DB identifier and copy the **Endpoint** (e.g., `chat-postgres-db.cxxxxxxx.us-east-1.rds.amazonaws.com`).

---

## 5. Step 3: Free Tier EC2 `t3.micro` Launch

1. Navigate to **EC2 Console** -> Click **Instances** -> Click **Launch instances**.
2. **Name and tags:**
   - *Name:* `chat-asistencia-server`
3. **Application and OS Images (Amazon Machine Image):**
   - Select **Ubuntu**.
   - *AMI:* **Ubuntu Server 24.04 LTS (HVM), SSD Volume Type** (64-bit x86 architecture). Verify the **Free tier eligible** badge is visible.
4. **Instance type:**
   - Select `t3.micro` (1 vCPU, 1 GiB Memory, Free tier eligible).
5. **Key pair (login):**
   - Click **Create new key pair**.
   - *Key pair name:* `chat-ec2-key`
   - *Key pair type:* `RSA`
   - *Private key file format:* `.pem` (for OpenSSH on Linux/macOS/PowerShell).
   - Click **Create key pair** and save the downloaded file `chat-ec2-key.pem` to your computer.
6. **Network settings:**
   - Click **Edit**.
   - *VPC:* Select your **Default VPC**.
   - *Auto-assign public IP:* **Enable**.
   - *Firewall (security groups):* Choose **Select existing security group**.
   - *Common security groups:* Select `chat-ec2-sg`.
7. **Configure storage:**
   - Set `25 GiB` gp3 root volume. (Free tier allows up to 30 GiB total EBS across all volumes).
8. Click **Launch instance**.
9. In the EC2 Instances list, wait for the state to transition to **Running** and **2/2 checks passed**.
10. Select the instance and note down its **Public IPv4 address** (e.g., `54.210.12.34`).

---

## 6. Step 4: DuckDNS Domain Setup

DuckDNS provides free dynamic DNS subdomains that integrate seamlessly with Let's Encrypt SSL.

1. Navigate to [https://www.duckdns.org/](https://www.duckdns.org/).
2. Log in using your GitHub or Google account.
3. In the **domains** section:
   - Enter your desired subdomain name (e.g., `chat-asistencia`).
   - Click **add domain**.
   - Resulting full domain name: `<subdomain>.duckdns.org` (e.g., `chat-asistencia.duckdns.org`).
4. In the `current ip` input box next to your subdomain, enter your **EC2 Public IPv4 address** (e.g., `54.210.12.34`).
5. Click **update ip**.
6. Copy your DuckDNS account **token** displayed at the top of the DuckDNS dashboard (a UUID string).

---

## 7. Step 5: EC2 Host Bootstrap

Connect to your EC2 instance, execute the repository bootstrap script to configure swap memory, install Docker, configure Nginx, configure the DuckDNS auto-updater, and obtain Let's Encrypt SSL.

### 7.1 SSH Connection

Open a terminal on your local machine and set permissions on the downloaded key pair:

```bash
chmod 400 path/to/chat-ec2-key.pem
ssh -i path/to/chat-ec2-key.pem ubuntu@<EC2_PUBLIC_IP>
```

### 7.2 Clone or Transfer the Repository

On the EC2 instance, install Git (if not already installed) and clone the repository:

```bash
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/<your-username>/chat-asistencia-estudiante.git
cd chat-asistencia-estudiante
```

*(Alternatively, use `scp` or `rsync` from your local machine to upload project files if using a private repository without GitHub SSH keys configured).*

### 7.3 Run the Bootstrap Script

Make the setup script executable and run it with root permissions:

```bash
chmod +x scripts/setup-ec2.sh
sudo ./scripts/setup-ec2.sh
```

The script will:
- Create a 2GB swap file (`/swapfile`) with `vm.swappiness=10` to protect the 1GB RAM `t3.micro` instance from Out-Of-Memory (OOM) kills during Docker builds.
- Install Docker Engine, Docker Compose plugin, Nginx, and Certbot.
- Add the `ubuntu` user to the `docker` group.
- Deploy the `/etc/nginx/sites-available/chat-support.conf` reverse proxy configuration routing `/api/` and `/chat-websocket/` to port `8080`.

Log out and back in to apply Docker group permissions for `ubuntu`:

```bash
exit
ssh -i path/to/chat-ec2-key.pem ubuntu@<EC2_PUBLIC_IP>
cd chat-asistencia-estudiante
```

### 7.4 Configure DuckDNS Dynamic IP Updater

Run the setup script with the `duckdns` sub-command to install a 5-minute cron job updating your IP:

```bash
sudo ./scripts/setup-ec2.sh duckdns <subdomain> <your-duckdns-token>
```
*Example:* `sudo ./scripts/setup-ec2.sh duckdns chat-asistencia a1b2c3d4-e5f6-7890-abcd-1234567890ef`

Verify the cron task and log file:
```bash
cat /var/log/duckdns.log
# Expected output: OK
```

### 7.5 Obtain Let's Encrypt SSL Certificate

Execute Certbot with the Nginx plugin:

```bash
sudo certbot --nginx -d <subdomain>.duckdns.org
```

- When prompted, provide your email for renewal notices.
- Agree to the Terms of Service.
- Certbot will automatically validate domain control via Nginx, generate certificates, and configure HTTPS redirection in `/etc/nginx/sites-available/chat-support.conf`.

Test the certificate auto-renewal:
```bash
sudo certbot renew --dry-run
```

---

## 8. Step 6: Backend Deployment & Seed Execution

### 8.1 Configure Server Production Environment

Navigate to the server directory and create the `.env` file:

```bash
cd ~/chat-asistencia-estudiante/apps/server
cp .env.example .env
nano .env
```

Set the production variables:

```properties
PORT=8080

# Database Credentials (RDS Endpoint from Step 2)
DB_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/proyecto_dswii
DB_USERNAME=postgres
DB_PASSWORD=<YourRDSPassword>

# JPA Settings
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

# Security
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000

# CORS Configuration (Vercel Frontend URL)
# If your Vercel deployment URL is not yet created, you can set a wildcard or update it after Step 7
CORS_ALLOWED_ORIGINS=https://<your-vercel-app>.vercel.app,http://localhost:5173
```

Save and exit (`Ctrl+O`, `Enter`, `Ctrl+X`).

### 8.2 Launch the Dockerized Server

Start the multi-stage Java 25 Native Preview container using Docker Compose:

```bash
docker compose up -d --build
```

Monitor container build and startup logs:

```bash
docker compose logs -f chat-server
```

Wait until you see:
`Started ChatAsistenciaEstudianteApplication in X.XXX seconds (process running for X.XXX)`

Press `Ctrl+C` to exit log streaming.

### 8.3 Seed the Database

Populate the RDS PostgreSQL database with the demo accounts, students, advisors, sample tickets, assignment history, activities, and messages using `scripts/seed-postgres.sql`.

You can execute the seed script directly from the EC2 instance using a temporary Docker PostgreSQL client container connected to the RDS endpoint:

```bash
cd ~/chat-asistencia-estudiante
docker run --rm -i \
  -e PGPASSWORD="<YourRDSPassword>" \
  postgres:16-alpine \
  psql -h <RDS_ENDPOINT> -U postgres -d proyecto_dswii < scripts/seed-postgres.sql
```

Verify the seeded accounts count:

```bash
docker run --rm -i \
  -e PGPASSWORD="<YourRDSPassword>" \
  postgres:16-alpine \
  psql -h <RDS_ENDPOINT> -U postgres -d proyecto_dswii -c "SELECT email, account_type FROM accounts;"
```

Output should show:
```text
         email          | account_type 
------------------------+--------------
 asesor@demo.com        | ADVISOR
 estudiante1@demo.com   | STUDENT
 estudiante2@demo.com   | STUDENT
 admin@demo.com         | ADMIN
(4 rows)
```

---

## 9. Step 7: Frontend Vercel Deployment

Deploy the React 19 SPA to Vercel's global edge network.

### 9.1 Push Code to GitHub

Ensure all your latest changes are pushed to your GitHub repository:

```bash
git push origin main
```

### 9.2 Import Repository into Vercel

1. Log in to your [Vercel Dashboard](https://vercel.com/).
2. Click **Add New...** -> **Project**.
3. Select your GitHub Git repository (`chat-asistencia-estudiante`) and click **Import**.

### 9.3 Configure Project Settings

1. **Project Name:** `chat-asistencia-estudiante` (or your preferred name).
2. **Framework Preset:** **Vite**.
3. **Root Directory:**
   - Click **Edit** next to Root Directory.
   - Select `apps/client`.
   - Click **Continue**.
4. **Build and Output Settings:**
   - *Build Command:* Leave default or `npx vite build` (Vercel detects Vite automatically).
   - *Output Directory:* `dist` (resolves inside `apps/client/dist`).
   - *Install Command:* `npm install`.
5. **Environment Variables:**
   Add the following production environment variables:
   - `VITE_API_URL`: `https://<subdomain>.duckdns.org/api`
   - `VITE_WS_URL`: `wss://<subdomain>.duckdns.org/chat-websocket`

> [!NOTE]
> Client routing is fully supported by `apps/client/vercel.json` (`"rewrites": [{"source": "/(.*)", "destination": "/index.html"}]`), ensuring deep links and page refreshes work seamlessly without 404 errors.

### 9.4 Deploy and Sync CORS

1. Click **Deploy**.
2. Wait 1-2 minutes for the build to finish. Vercel will present the production domain (e.g., `https://chat-asistencia-estudiante.vercel.app`).
3. Return to your EC2 instance and update the backend CORS origin to match your exact Vercel URL:
   ```bash
   cd ~/chat-asistencia-estudiante/apps/server
   nano .env
   ```
   Update `CORS_ALLOWED_ORIGINS`:
   ```properties
   CORS_ALLOWED_ORIGINS=https://chat-asistencia-estudiante.vercel.app
   ```
4. Restart the backend container:
   ```bash
   docker compose restart chat-server
   ```

---

## 10. Recruiter / Portfolio Demo Checklist & Credentials

Use these verified demo accounts to present the full capabilities of the platform to evaluators, technical recruiters, or stakeholders.

### 10.1 Pre-seeded Demo Credentials

All seeded accounts use the password: `password123`

| Role | Email | Password | Pre-seeded Context / State |
| :--- | :--- | :--- | :--- |
| **Advisor** | `asesor@demo.com` | `password123` | Carlos Mendoza. Assigned to active ticket `c1111111...` with student Ana García. Capacity: 3 tickets. |
| **Student 1** | `estudiante1@demo.com` | `password123` | Ana García (U20211001). Has an active `IN_PROGRESS` billing ticket with live chat messages. |
| **Student 2** | `estudiante2@demo.com` | `password123` | Luis Torres (U20211002). Has an unassigned `OPEN` course validation ticket. |
| **Administrator** | `admin@demo.com` | `password123` | Platform administrator account with full visibility. |

### 10.2 Portfolio Demonstration Checklist

1. **Student Sign In & Ticket Inspection:**
   - Open your Vercel deployment URL in Browser Window 1.
   - Sign in with `estudiante1@demo.com` / `password123`.
   - Verify redirect to `/student/dashboard`.
   - Click on the active ticket (*"Problemas con el pago de la pensión"*).
   - Observe previous chat history loaded from RDS PostgreSQL.

2. **Advisor Real-Time Chat (WebSocket Verification):**
   - Open an Incognito / Private Browser Window 2.
   - Navigate to the Vercel URL and sign in with `asesor@demo.com` / `password123`.
   - Verify redirect to `/advisor/dashboard`.
   - Select the same ticket in progress (*"Problemas con el pago de la pensión"*).
   - Send a message from the Advisor window: *"Confirmamos la recepción del voucher, estamos validando."*
   - Verify that the message instantly arrives in the Student window via WebSocket without refreshing the page.

3. **SPA Refresh Resilience:**
   - Refresh the page (`F5`) on any deep route (e.g., `/student/dashboard` or `/advisor/dashboard`).
   - Confirm that the page reloads cleanly without 404 Not Found errors (guaranteed by `vercel.json` rewrites).

4. **HTTPS / SSL Inspection:**
   - Inspect the padlock icon in the browser address bar for both the Vercel frontend and the DuckDNS backend API calls in DevTools Network tab.
   - Confirm TLS 1.3 encryption end-to-end.

---

## 11. Maintenance, Logs, and Troubleshooting

### 11.1 Viewing Real-Time Logs

```bash
# Backend Spring Boot application logs
cd ~/chat-asistencia-estudiante/apps/server
docker compose logs -f --tail=100 chat-server

# Nginx access and error logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# DuckDNS updater cron logs
cat /var/log/duckdns.log
```

### 11.2 Stopping or Restarting the Stack

```bash
# Restart backend
docker compose restart chat-server

# Rebuild backend after code changes
docker compose up -d --build

# Reload Nginx configuration without downtime
sudo nginx -t && sudo systemctl reload nginx
```
