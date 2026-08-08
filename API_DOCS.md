# PayFlow API Documentation

The PayFlow application exposes a RESTful API for managing accounts and processing payments. All endpoints are prefixed with `/api/v1`.

## 1. Account Management

### 1.1. Create a New Account
- **Endpoint**: `POST /api/v1/accounts`
- **Description**: Creates a new financial account.

**Request Body**
```json
{
  "accountNumber": "1234567890",
  "balance": 1000.00,
  "status": "ACTIVE"
}
```

**Response (200 OK)**
```json
{
  "id": 1,
  "accountNumber": "1234567890",
  "balance": 1000.0,
  "status": "ACTIVE",
  "createdAt": "2023-10-01T12:00:00",
  "updatedAt": "2023-10-01T12:00:00"
}
```

### 1.2. Get All Accounts
- **Endpoint**: `GET /api/v1/accounts`
- **Description**: Retrieves a list of all existing accounts.

**Response (200 OK)**
```json
[
  {
    "id": 1,
    "accountNumber": "1234567890",
    "balance": 1000.0,
    "status": "ACTIVE",
    "createdAt": "2023-10-01T12:00:00",
    "updatedAt": "2023-10-01T12:00:00"
  }
]
```

---

## 2. Payment Processing

### 2.1. Process a Payment
- **Endpoint**: `POST /api/v1/payments`
- **Description**: Initiates a payment transfer from a source account to a target account.

**Headers**
- `Idempotency-Key` (String, **Required**): A unique key used to safely retry requests without processing the same payment multiple times.

**Request Body**
```json
{
  "sourceAccountNumber": "1234567890",
  "targetAccountNumber": "0987654321",
  "amount": 250.50
}
```

**Responses**

- **200 OK (Success)**
```json
{
  "transactionId": 1001,
  "idempotencyKey": "a1b2c3d4-e5f6-7g8h-9i0j",
  "status": "COMPLETED",
  "amount": 250.50,
  "message": "Payment processed successfully"
}
```

- **422 Unprocessable Entity (Validation / Business Logic Error)**
```json
{
  "transactionId": null,
  "idempotencyKey": "a1b2c3d4-e5f6-7g8h-9i0j",
  "status": "REJECTED",
  "amount": 250.50,
  "message": "Insufficient balance"
}
```

- **400 Bad Request (Missing Idempotency-Key)**
```json
{
  "message": "Idempotency-Key header is required"
}
```
