# Etapa 1: compila o projeto
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Primeiro copia apenas o pom para aproveitar o cache das dependências
COPY pom.xml .

RUN mvn -B dependency:go-offline

# Depois copia o código
COPY src ./src

# Gera o arquivo .jar
RUN mvn -B clean package -DskipTests


# Etapa 2: imagem menor usada para executar a aplicação
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Cria usuário sem privilégios de administrador
RUN useradd --system --uid 1001 spring

COPY --from=build /app/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]