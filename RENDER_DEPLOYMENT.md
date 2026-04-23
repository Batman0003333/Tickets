# Render Deployment Guide

## Prerequisites

1. **GitHub Account** - Push your code to GitHub (Render pulls from GitHub)
2. **Render Account** - Sign up at [render.com](https://render.com)
3. **MySQL Database** - You'll need either:
   - Aiven MySQL (recommended free tier)
   - Or use Render's Postgres (requires code changes)

---

## Step 1: Prepare Your GitHub Repository

### 1.1 Initialize Git (if not already done)
```powershell
cd C:\TicketMngmt
git init
git add .
git commit -m "Initial commit: Ticket Management System"
```

### 1.2 Create a GitHub Repository
- Go to [github.com/new](https://github.com/new)
- Create a repo named `TicketMngmt`
- Push your code:
```powershell
git remote add origin https://github.com/YOUR_USERNAME/TicketMngmt.git
git branch -M main
git push -u origin main
```

---

## Step 2: Set Up MySQL Database (Aiven)

### 2.1 Create an Aiven MySQL Instance
1. Go to [aiven.io](https://aiven.io)
2. Sign up (free tier available)
3. Create a new MySQL 8.0 service
4. Wait for it to be ready
5. Copy these credentials:
   - **Host**: (e.g., `ticketmgmt-db.c.aivencloud.com`)
   - **Port**: (e.g., `21813`)
   - **Username**: `avnadmin`
   - **Password**: (auto-generated)
   - **Database**: `ticket_db`

### 2.2 Create the Database Schema
Connect to your Aiven MySQL and run:
```sql
CREATE DATABASE IF NOT EXISTS ticket_db;
USE ticket_db;
-- Hibernate will auto-create tables with ddl-auto=update
```

---

## Step 3: Create Render Web Service

### 3.1 Connect Your GitHub to Render
1. Go to [render.com/dashboard](https://render.com/dashboard)
2. Click **"New +"** → **"Web Service"**
3. Select **"Deploy an existing repository"**
4. Authorize GitHub and select your `TicketMngmt` repo

### 3.2 Configure the Web Service

**Name**: `ticketmgmt-app`

**Environment**: Docker

**Build Command**: (leave default)

**Start Command**: (leave default)

**Instance Type**: Free

**Region**: Oregon (or closest to you)

### 3.3 Add Environment Variables

Click **"Environment"** and add:

| Key | Value |
|-----|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://HOST:PORT/ticket_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` | `avnadmin` (or your Aiven user) |
| `SPRING_DATASOURCE_PASSWORD` | (your Aiven password) |
| `MAIL_USER` | (your Gmail email) |
| `MAIL_PASS` | (your Gmail app password) |

**For Gmail password**:
1. Enable 2FA on your Google account
2. Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
3. Generate an app password for "Mail"
4. Use that 16-character password

### 3.4 Set Build Settings

- **Dockerfile Path**: `./Dockerfile`
- **Docker Context**: `.`

### 3.5 Deploy

Click **"Create Web Service"**

Render will:
1. Clone your repo
2. Build the Docker image
3. Deploy the container
4. Assign a public URL (e.g., `ticketmgmt-app.onrender.com`)

---

## Step 4: Verify Deployment

1. Wait 5-10 minutes for the build and deploy
2. Check the **Logs** tab in Render dashboard
3. Once deployed, visit: `https://YOUR_RENDER_URL` 
4. The database will auto-initialize (Hibernate `ddl-auto=update`)

---

## Troubleshooting

### Build fails
- Check the **Logs** tab for errors
- Verify `Dockerfile` path is correct
- Ensure `demo/pom.xml` exists

### Database connection fails
- Verify `SPRING_DATASOURCE_URL` is correct
- Check Aiven credentials
- Ensure the Aiven IP whitelist includes Render (usually auto)

### App crashes after deploy
- Check logs for database/email errors
- Verify all environment variables are set
- Ensure the MySQL database exists

---

## Cost Notes

- **Render Free Tier**: Web service ($0, spins down after 15 min inactivity)
- **Aiven MySQL Free Tier**: First month free ($14/month after)
- To avoid costs, delete services in the Render/Aiven dashboards when done

---

## Next Steps

If you want a more automated setup, use `render.yaml` at the repo root (already created). Render will auto-detect it during setup.
