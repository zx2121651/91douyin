FROM golang:1.22-alpine AS builder

WORKDIR /app
# Install FFmpeg required for cover generation, and build dependencies
RUN apk add --no-cache gcc musl-dev libc-dev sqlite-dev ffmpeg

COPY go.mod go.sum ./
RUN go mod download

COPY . .
# CGO must be enabled for SQLite
RUN CGO_ENABLED=1 GOOS=linux go build -o douyin-backend main.go

FROM alpine:latest
WORKDIR /app

# Install runtime dependencies including FFmpeg and CGO support
RUN apk add --no-cache ffmpeg ca-certificates tzdata sqlite-libs

# Copy executable and configuration
COPY --from=builder /app/douyin-backend .
COPY --from=builder /app/config/config.yaml ./config/

# Ensure storage directories exist
RUN mkdir -p public/videos public/covers public/avatars public/backgrounds

EXPOSE 8080
ENTRYPOINT ["./douyin-backend"]
