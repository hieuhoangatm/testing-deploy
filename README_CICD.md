# Hayden CI/CD Demo

Project nay da duoc cau hinh de demo theo huong free:
- CI: build + test bang GitHub Actions
- CD: build Docker image, push len GHCR, deploy tren self-hosted runner (Oracle free-tier hoac may Linux ca nhan)

## 1) Cac file da co san

- `.github/workflows/ci.yml`: chay `./mvnw clean verify`
- `.github/workflows/cd.yml`: build image va deploy
- `Dockerfile`: dong goi Spring Boot jar
- `deploy/docker-compose.yml`: chay container tren server

## 2) Tao GitHub Secrets (toi thieu)

Vao repo -> Settings -> Secrets and variables -> Actions, tao:

- `APP_PORT`: port public tren server (vi du `8080`)

## 3) Chuan bi server demo (free)

Can 1 may Linux chay duoc lien tuc:
- Oracle Cloud Always Free VM (khuyen nghi de demo on dinh)
- Hoac may Linux ca nhan

May nay can cai:
- Docker
- Docker Compose plugin (`docker compose`)
- GitHub Actions self-hosted runner

Thu muc deploy mac dinh: `/opt/hayden`

### Dang ky self-hosted runner

1. Vao repo -> Settings -> Actions -> Runners -> New self-hosted runner
2. Chon Linux va chay cac lenh ma GitHub cung cap tren server
3. Sau khi runner online, job `deploy` se chay truc tiep tren may nay

## 4) Luong demo

1. Push code len branch `master`
2. Workflow `CI` chay build/test
3. Workflow `CD` build image va push:
   - `ghcr.io/<owner>/<repo>:latest`
   - `ghcr.io/<owner>/<repo>:sha-<commit>`
4. Job deploy (self-hosted runner) tren server va chay:
   - `docker compose pull`
   - `docker compose up -d --remove-orphans`
   - `docker image prune -f`

## 5) Lenh kiem tra tren server

```bash
cd /opt/hayden
docker compose ps
docker logs hayden-app --tail=100
curl http://localhost:8080/actuator/health
```

Neu doi port public, cap nhat secret `APP_PORT`.

## 6) Tai sao cach nay free

- GitHub Actions (muc free): chay CI/CD co gioi han phut mien phi
- GHCR: luu Docker image mien phi cho demo
- Oracle Always Free: host app khong mat phi hang thang
