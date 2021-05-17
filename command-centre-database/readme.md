
### Start latest postgress
Prerequisite: install Docker and make sure Docker daemon is running
1. `cd C:/command-centre/postgress` & `docker-compose up -d`
### Stop postgress
1. `docker-compose down`

Note:

Using docker-compose down will not store any state.
Hence will need mount volumes
### Mount volumes
mounting of volume to to preserve data database-data:/var/lib/postgresql/data/ 

### Operation of Database
$ docker-compose run database bash 

# drop into the container shell
database# psql --host=database --username=<username> --dbname=<db_name>
refer to database.project for more info
