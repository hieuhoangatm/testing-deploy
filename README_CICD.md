# Hayden CI/CD Demo

Project nay da duoc cau hinh theo 3 moi truong:
- CI: build + test cho `develop`, `staging`, `master`
- CD: build Docker image, push len GHCR va deploy theo tung moi truong tren self-hosted runner

## 1) Cac file da co san

- `.github/workflows/ci.yml`: chay `./mvnw clean verify` tren 3 nhanh
- `.github/workflows/cd.yml`: build image va deploy theo nhanh
- `Dockerfile`: dong goi Spring Boot jar
- `deploy/docker-compose.yml`: chay container tren server

## 2) Tao GitHub Secrets

Vao repo -> Settings -> Secrets and variables -> Actions, tao:

- `APP_PORT_DEVELOP`: port deploy cho develop (vi du `18080`)
- `APP_PORT_STAGING`: port deploy cho staging (vi du `28080`)
- `APP_PORT_PROD`: port deploy cho production (vi du `8080`)

## 3) Chuan bi server demo (free)

Can 1 may Linux chay duoc lien tuc:
- Oracle Cloud Always Free VM (khuyen nghi de demo on dinh)
- Hoac may Linux ca nhan

May nay can cai:
- Docker
- Docker Compose plugin (`docker compose`)
- GitHub Actions self-hosted runner

Thu muc deploy theo moi truong:
- develop: `~/hayden-develop`
- staging: `~/hayden-staging`
- production: `~/hayden-production`

### Dang ky self-hosted runner

1. Vao repo -> Settings -> Actions -> Runners -> New self-hosted runner
2. Chon Linux va chay cac lenh ma GitHub cung cap tren server
3. Sau khi runner online, job `deploy` se chay truc tiep tren may nay

## 4) Luong chuyen giao 3 moi truong

1. Dev merge code vao `develop`
2. CI chay test, CD build image tag `develop-latest` va deploy vao `~/hayden-develop`
3. Promote sang `staging`
4. CI chay lai, CD build image tag `staging-latest` va deploy vao `~/hayden-staging`
5. Promote sang `master` (production)
6. CI chay lai, CD build image tag `prod-latest` va deploy vao `~/hayden-production`

## 5) Lenh kiem tra tren server

```bash
cd ~/hayden-production
docker compose ps
docker logs hayden-app --tail=100
curl http://localhost:8080/actuator/health
```

Neu doi port moi moi truong, cap nhat cac secret `APP_PORT_*`.

## 6) Tai sao cach nay free

- GitHub Actions (muc free): chay CI/CD co gioi han phut mien phi
- GHCR: luu Docker image mien phi cho demo
- Oracle Always Free: host app khong mat phi hang thang
