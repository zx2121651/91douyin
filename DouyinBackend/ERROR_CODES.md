# Douyin Backend Error Code System

This document defines the unified error code system for the Douyin Backend. All API responses follow a standard structure with `status_code` and `status_msg`.

## Standard Response Structure

```json
{
    "status_code": 0,
    "status_msg": "Success",
    "data": { ... } // Optional data fields merged at the top level
}
```

## Error Codes

| Code | Message | Description |
| :--- | :--- | :--- |
| **0** | **Success** | Request processed successfully. |
| **10001** | **Service Error** | Internal server error. Check logs for details. |
| **10002** | **Parameter Error** | Invalid request parameters. |
| **10003** | **Authentication Error** | Unauthorized or invalid token. |
| **10004** | **Forbidden Error** | Permission denied for the requested resource. |
| **10005** | **Resource Not Found** | The requested resource does not exist. |
| **20001** | **User Not Found** | Specified user could not be found. |
| **20002** | **User Already Exist** | Username is already taken during registration. |
| **20003** | **Password Error** | Incorrect username or password. |
| **30001** | **Video Not Found** | Specified video could not be found. |
| **40001** | **Favorite Action Error** | Failed to perform favorite/unfavorite action. |
| **50001** | **Comment Action Error** | Failed to post or delete a comment. |
| **60001** | **Relation Action Error** | Failed to follow/unfollow user. |
| **70001** | **Message Action Error** | Failed to send a message. |

## Usage in Handlers

Handlers should use the `utils.SendResponse` helper to ensure consistency:

```go
utils.SendResponse(c, errno.Success, map[string]interface{}{
    "user_id": user.ID,
})
```

If an error occurs:

```go
utils.SendResponse(c, errno.ParamErr.WithMessage("invalid user id"), nil)
```

## Multi-terminal Coordination

The Android client (Douyin Lite) and other potential clients should use these `status_code` values to determine the success of a request and handle specific error scenarios (e.g., token expiration on 10003).
