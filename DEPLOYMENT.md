# Deployment Notes

This project now expects user identity to come from a signed JWT token, not from a client-controlled user id.

## Required production environment variables

Set these before deploying:

```bash
DB_URL=jdbc:mysql://your-host:3306/sekai_friend?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=your_database_user
DB_PASSWORD=your_database_password
JWT_SECRET=replace-with-a-long-random-secret
JWT_EXPIRATION_MINUTES=1440
CORS_ALLOWED_ORIGINS=https://your-domain.com
ADMIN_SECRET=replace-with-a-private-admin-secret
SERVER_PORT=8088
```

Do not use the default `JWT_SECRET` in production.

## Monetization flow

Run this migration on an existing database:

```bash
mysql -uroot -p sekai_friend < database/monetization.sql
```

Create a Pro activation code after you receive payment from the admin page:

```text
https://your-domain.com/admin-monetization
```

Use the `ADMIN_SECRET` value when the page asks for the admin secret.

You can also create a code through the API:

```bash
curl -X POST http://localhost:8088/api/admin/activation-codes \
  -H "Authorization: Bearer YOUR_LOGIN_TOKEN" \
  -H "X-Admin-Secret: $ADMIN_SECRET" \
  -H "Content-Type: application/json" \
  -d "{\"durationDays\":30,\"maxUses\":1,\"priceYuan\":19.90,\"note\":\"manual sale\"}"
```

Give the returned `code` to the customer. They can redeem it on `/membership`.

The admin page shows total codes, active subscriptions, redeemed uses, remaining uses, and estimated revenue based on `priceYuan * usedCount`.

## Build

```bash
cd frontend
npm install
npm run build
```

Copy `frontend/dist` into `src/main/resources/static`, then build the backend:

```bash
mvn clean package
```

## Commercialization checklist

- Use a real domain with HTTPS.
- Confirm the word list license before selling access.
- Add privacy policy and terms of service.
- Add database backup and restore procedures.
- Add logs and error monitoring.
- Add payment only after authentication, deployment, and data ownership are stable.
