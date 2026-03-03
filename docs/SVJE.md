

docker stop eva-postgres
docker rm eva-postgres
docker volume prune -f
docker run --name eva-postgres -e POSTGRES_USER=evauser -e POSTGRES_PASSWORD=Test123 -e POSTGRES_DB=evadb -p 5555:5432 -d postgres:17


