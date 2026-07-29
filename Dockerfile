FROM fedora:44
WORKDIR /app
COPY target/oda-tribute-service /app

CMD ["./oda-tribute-service"]
