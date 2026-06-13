#!/bin/bash
set -e

PROJECT_NAME="${PROJECT_NAME:-file}"
REGION="${AWS_REGION:-ap-northeast-2}"
EC2_STACK="${EC2_STACK:-stg-stack-name}"
SERVER_USER="${SERVER_USER:-ubuntu}"
SERVER_PATH="${SERVER_PATH:-~/transfer}"
SSH_KEY="${SSH_KEY:-${HOME}/.ssh/stg-key.pem}"
SPRING_PROFILE="${SPRING_PROFILE:-stg-compose}"
IMAGE_NAME="${IMAGE_NAME:-local-${PROJECT_NAME}}"
IMAGE_TAG="${IMAGE_TAG:-stg-latest}"
TAR_FILE="${TAR_FILE:-${HOME}/build/${PROJECT_NAME}-stg.tar}"
PROJECT_DIR="${PROJECT_DIR:-$(pwd)}"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  ${PROJECT_NAME} STG deploy${NC}"
echo -e "${GREEN}========================================${NC}"

echo -e "${YELLOW}[0/6] Checking prerequisites${NC}"
docker info >/dev/null 2>&1 || { echo -e "${RED}Docker is not running${NC}"; exit 1; }
[[ -f "${SSH_KEY}" ]] || { echo -e "${RED}SSH key not found: ${SSH_KEY}${NC}"; exit 1; }

SERVER_HOST=$(aws cloudformation describe-stacks \
  --region "${REGION}" --stack-name "${EC2_STACK}" \
  --query "Stacks[0].Outputs[?OutputKey=='PublicIp'].OutputValue | [0]" \
  --output text 2>/dev/null)
[[ -n "${SERVER_HOST}" && "${SERVER_HOST}" != "None" ]] \
  || { echo -e "${RED}PublicIp not found in stack ${EC2_STACK}${NC}"; exit 1; }
mkdir -p "$(dirname "${TAR_FILE}")"

echo -e "${YELLOW}[1/6] Gradle build${NC}"
cd "${PROJECT_DIR}"
./gradlew clean build -x test

echo -e "${YELLOW}[2/6] Docker build (profile=${SPRING_PROFILE})${NC}"
docker build --platform linux/amd64 \
  --build-arg SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" \
  --build-arg JAVA_OPTS='-XX:+UseG1GC -Xms512m -Xmx512m' \
  -t "${IMAGE_NAME}:${IMAGE_TAG}" \
  -f Dockerfile .

echo -e "${YELLOW}[3/6] Saving image${NC}"
docker save "${IMAGE_NAME}:${IMAGE_TAG}" -o "${TAR_FILE}"

echo -e "${YELLOW}[4/6] Uploading image${NC}"
SSH_OPTS=(-i "${SSH_KEY}" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null)
ssh "${SSH_OPTS[@]}" "${SERVER_USER}@${SERVER_HOST}" "mkdir -p ${SERVER_PATH}"
scp "${SSH_OPTS[@]}" "${TAR_FILE}" \
  "${SERVER_USER}@${SERVER_HOST}:${SERVER_PATH}/${PROJECT_NAME}-stg.tar"

echo -e "${YELLOW}[5/6] Remote load and restart${NC}"
ROLLBACK_TAG="stg-rollback-$(date +%Y%m%d-%H%M%S)"
ssh "${SSH_OPTS[@]}" "${SERVER_USER}@${SERVER_HOST}" bash <<ENDSSH
set -e
cd ~/docker-compose
if docker image inspect ${IMAGE_NAME}:${IMAGE_TAG} >/dev/null 2>&1; then
  docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:${ROLLBACK_TAG}
fi
docker load -i ${SERVER_PATH}/${PROJECT_NAME}-stg.tar
docker compose up -d --no-deps ${PROJECT_NAME}
docker ps --filter "name=${PROJECT_NAME}" \
  --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
ENDSSH

echo -e "${YELLOW}[6/6] Done${NC}"
echo "server: ${SERVER_HOST}"
echo "rollback tag: ${IMAGE_NAME}:${ROLLBACK_TAG}"
