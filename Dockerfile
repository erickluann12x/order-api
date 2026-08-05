# Etapa 1: compila o projeto
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# O pom.xml está dentro de order-api
COPY order-api/pom.xml ./pom.xml

# Baixa as dependências e aproveita o cache do Docker
RUN mvn -B dependency:go-offline

# O código-fonte também está dentro de order-api
COPY order-api/src ./src

# Gera o arquivo .jar
RUN mvn -B clean package -DskipTests


# Etapa 2: executa a aplicação
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Usuário sem privilégios administrativos
RUN useradd --system --uid 1001 spring

# Copia o jar gerado na primeira etapa
COPY --from=build /app/target/*.jar ./app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]